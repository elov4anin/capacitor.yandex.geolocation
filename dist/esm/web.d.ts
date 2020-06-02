import { WebPlugin } from '@capacitor/core';
import { YandexLocatorPluginPlugin } from './definitions';
export declare class YandexLocatorPluginWeb extends WebPlugin implements YandexLocatorPluginPlugin {
    constructor();
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
declare const YandexLocatorPlugin: YandexLocatorPluginWeb;
export { YandexLocatorPlugin };
