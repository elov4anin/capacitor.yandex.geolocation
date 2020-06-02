import { WebPlugin } from '@capacitor/core';
import { YandexLocatorPluginPlugin } from './definitions';

export class YandexLocatorPluginWeb extends WebPlugin implements YandexLocatorPluginPlugin {
  constructor() {
    super({
      name: 'YandexLocatorPlugin',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const YandexLocatorPlugin = new YandexLocatorPluginWeb();

export { YandexLocatorPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(YandexLocatorPlugin);
