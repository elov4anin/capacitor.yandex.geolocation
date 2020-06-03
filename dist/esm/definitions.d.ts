declare module "@capacitor/core" {
    interface PluginRegistry {
        YandexLocatorPlugin: YandexLocatorPluginPlugin;
    }
}
export interface YandexLocatorPluginPlugin {
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
