import { WebPlugin } from '@capacitor/core';

import type { CapacitorStreamFetchPlugin } from './definitions';

export class CapacitorStreamFetchWeb extends WebPlugin implements CapacitorStreamFetchPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
