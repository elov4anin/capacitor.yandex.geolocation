declare module "@capacitor/core" {
    interface PluginRegistry {
        YandexLocatorPlugin: YandexLocatorPluginPlugin;
    }
}
export interface YandexLocatorPluginPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
