import { WebPlugin } from '@capacitor/core';
import { YandexLocatorPluginPlugin } from './definitions';
export declare class YandexLocatorPluginWeb extends WebPlugin implements YandexLocatorPluginPlugin {
    constructor();
    echo(options: {
        version: string;
        url: string;
        api_key: string;
    }): Promise<{
        version: string;
        url: string;
        api_key: string;
    }>;
}
declare const YandexLocatorPlugin: YandexLocatorPluginWeb;
export { YandexLocatorPlugin };
