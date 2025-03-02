package app.aiaw.capacitor.fetch

import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@CapacitorPlugin(name = "StreamFetch")
class StreamFetchPlugin : Plugin() {
    
    private val TAG = "StreamFetchPlugin"
    private val REQUEST_COUNTER = AtomicInteger(0)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @PluginMethod
    fun streamFetch(call: PluginCall) {
        val url = call.getString("url") ?: run {
            call.reject("URL is required")
            return
        }
        
        val method = call.getString("method") ?: "GET"
        val headersObj = call.getObject("headers", JSObject())
        val body = call.getString("body")
        
        // Generate a unique request ID
        val requestId = REQUEST_COUNTER.incrementAndGet()
        
        scope.launch {
            try {
                // Build request headers
                val headersBuilder = Headers.Builder()
                val headerIterator = headersObj!!.keys()
                while (headerIterator.hasNext()) {
                    val key = headerIterator.next()
                    val value = headersObj.getString(key)
                    headersBuilder.add(key, value!!)
                }
                
                // Create request body if needed
                val requestBody: RequestBody? = if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                    body.toRequestBody("application/json; charset=utf-8".toMediaType())
                } else {
                    null
                }
                
                // Build request
                val request = Request.Builder()
                    .url(url)
                    .method(method, requestBody)
                    .headers(headersBuilder.build())
                    .build()
                
                // Execute request
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                
                // Send initial response with headers
                val initialResponse = createInitialResponse(requestId, response)
                withContext(Dispatchers.Main) {
                    call.resolve(initialResponse)
                }
                
                // Start streaming response body
                streamResponseBody(requestId, response)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error executing request", e)
                withContext(Dispatchers.Main) {
                    call.reject("Request failed: ${e.localizedMessage}")
                }
            }
        }
    }
    
    private fun createInitialResponse(requestId: Int, response: Response): JSObject {
        val ret = JSObject()
        ret.put("requestId", requestId)
        ret.put("status", response.code)
        ret.put("statusText", response.message)
        
        // Convert headers to JSObject
        val headersObj = JSObject()
        for (name in response.headers.names()) {
            headersObj.put(name, response.header(name))
        }
        ret.put("headers", headersObj)
        
        return ret
    }
    
    private suspend fun streamResponseBody(requestId: Int, response: Response) {
        try {
            response.body?.let { body ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024) // 16KB buffer
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (bytesRead > 0) {
                            // Copy only the bytes read
                            val chunk = buffer.copyOfRange(0, bytesRead)
                            
                            // Emit chunk event to JavaScript
                            withContext(Dispatchers.Main) {
                                val data = JSObject()
                                data.put("requestId", requestId)
                                data.put("chunk", chunk.toList())
                                notifyListeners("streamResponse", data)
                            }
                        }
                    }
                }
            }
            
            // Send end event
            withContext(Dispatchers.Main) {
                val data = JSObject()
                data.put("requestId", requestId)
                data.put("status", 0)  // 0 indicates end of stream
                notifyListeners("streamResponse", data)
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Error streaming response", e)
            
            // Notify of error and end stream
            withContext(Dispatchers.Main) {
                val data = JSObject()
                data.put("requestId", requestId)
                data.put("status", 0)
                data.put("error", e.localizedMessage)
                notifyListeners("streamResponse", data)
            }
        }
    }
}
