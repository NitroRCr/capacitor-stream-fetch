import { registerPlugin } from '@capacitor/core';

import type { StreamFetchPlugin, StreamResponse, StreamResponseEvent } from './definitions';

const StreamFetch = registerPlugin<StreamFetchPlugin>('StreamFetch', {});

export { StreamFetch };

/**
 * Implements a fetch API that supports streaming responses
 */
export function fetch(url: string, options?: RequestInit): Promise<Response> {
  // 如果在普通 Web 环境中，使用原生 fetch
  if (!('Capacitor' in window)) {
    return window.fetch(url, options);
  }

  const {
    signal,
    method = 'GET',
    headers: _headers = {},
    body = null
  } = options || {};

  // 创建一个 TransformStream 来处理响应数据
  const ts = new TransformStream();
  const writer = ts.writable.getWriter();

  let closed = false;
  const close = () => {
    if (closed) return;
    closed = true;
    writer.ready.then(() => {
      writer.close().catch((e) => console.error(e));
    });
  };

  if (signal) {
    signal.addEventListener('abort', () => close());
  }

  // 准备请求头
  const headers: Record<string, string> = {
    'Accept': 'application/json, text/plain, */*',
    'Accept-Language': 'en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7',
    'User-Agent': navigator.userAgent
  };

  for (const [key, value] of Object.entries(Object.fromEntries(new Headers(_headers) as any))) {
    headers[key] = value as string;
  }

  // 监听来自原生端的流式响应事件
  let removeListener: (() => void) | undefined;
  let requestId: number;

  // 设置事件监听器接收流式数据
  StreamFetch.addListener('streamResponse', (event: StreamResponseEvent) => {
    if (event.requestId !== requestId) {
      return;
    }

    if ('chunk' in event) {
      writer.ready.then(() => {
        writer.write(new Uint8Array(JSON.parse(event.chunk)));
      });
    } else if (event.status === 0) {
      event.error && console.error('Stream fetch error:', event.error);
      // 流结束
      close();
      removeListener?.();
    }
  }).then((listener) => {
    removeListener = () => {
      listener.remove().catch(e => console.error('Failed to remove listener:', e));
    };
  });

  return StreamFetch.streamFetch({
    method: method.toUpperCase(),
    url,
    headers,
    body: typeof body === 'string' ? body : null
  })
    .then((res: StreamResponse) => {
      requestId = res.requestId;

      // 创建标准 Response 对象
      const response = new Response(ts.readable, {
        status: res.status,
        statusText: res.statusText,
        headers: new Headers(res.headers)
      });

      if (res.status >= 300) {
        setTimeout(close, 100);
      }

      return response;
    })
    .catch((error) => {
      console.error('Stream fetch error:', error);
      return new Response('', { status: 599 });
    });
}

export * from './definitions';
