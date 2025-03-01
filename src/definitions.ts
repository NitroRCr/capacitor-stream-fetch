export interface CapacitorStreamFetchPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
