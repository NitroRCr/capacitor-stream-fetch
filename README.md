# capacitor-stream-fetch

Fetch with stream support.

Use cases: AI chat completion, SSE, and other uses that require streaming response and CORS.

- Support Capacitor v7.

- Only support android.

- Written in kotlin, so you need to configure kotlin for your project before using it.

## Install

```bash
npm install capacitor-stream-fetch
npx cap sync
```

## Usage

```typescript
import { fetch } from 'capacitor-stream-fetch'
// Use it like native fetch
```

## API

<docgen-index>

* [`streamFetch(...)`](#streamfetch)
* [`addListener(string, ...)`](#addlistenerstring-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### streamFetch(...)

```typescript
streamFetch(options: { url: string; method: string; headers: Record<string, string>; body?: string; }) => any
```

Execute a HTTP request with streaming response

| Param         | Type                                                                       | Description         |
| ------------- | -------------------------------------------------------------------------- | ------------------- |
| **`options`** | <code>{ url: string; method: string; headers: any; body?: string; }</code> | The request options |

**Returns:** <code>any</code>

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (event: any) => void) => any
```

Add a listener for a specific event

| Param              | Type                                 | Description                                         |
| ------------------ | ------------------------------------ | --------------------------------------------------- |
| **`eventName`**    | <code>string</code>                  | Name of the event to listen for                     |
| **`listenerFunc`** | <code>(event: any) =&gt; void</code> | Callback function to be called when the event fires |

**Returns:** <code>any</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => any
```

Remove all listeners for this plugin

**Returns:** <code>any</code>

--------------------


### Interfaces


#### StreamResponse

| Prop             | Type                                      |
| ---------------- | ----------------------------------------- |
| **`requestId`**  | <code>number</code>                       |
| **`status`**     | <code>number</code>                       |
| **`statusText`** | <code>string</code>                       |
| **`headers`**    | <code>Record&lt;string, string&gt;</code> |


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |

</docgen-api>
