export interface StreamFetchPlugin {
  /**
   * Execute a HTTP request with streaming response
   * @param options The request options
   * @returns Response headers and status information
   */
  streamFetch(options: {
    url: string;
    method: string;
    headers: Record<string, string>;
    body?: string;
  }): Promise<StreamResponse>;

  /**
   * Add a listener for a specific event
   * @param eventName Name of the event to listen for
   * @param listenerFunc Callback function to be called when the event fires
   */
  addListener(eventName: string, listenerFunc: (event: any) => void): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners for this plugin
   */
  removeAllListeners(): Promise<void>;
}

export interface PluginListenerHandle {
  /**
   * Remove the listener from the plugin
   */
  remove: () => Promise<void>;
}

export interface StreamResponse {
  requestId: number;
  status: number;
  statusText: string;
  headers: Record<string, string>;
}

export interface ChunkEvent {
  requestId: number;
  chunk: string;
}

export interface EndEvent {
  requestId: number;
  status: number;
  error?: string;
}

export type StreamResponseEvent = ChunkEvent | EndEvent;
