import { registerPlugin } from '@capacitor/core';

import type { CapacitorStreamFetchPlugin } from './definitions';

const CapacitorStreamFetch = registerPlugin<CapacitorStreamFetchPlugin>('CapacitorStreamFetch', {
  web: () => import('./web').then((m) => new m.CapacitorStreamFetchWeb()),
});

export * from './definitions';
export { CapacitorStreamFetch };
