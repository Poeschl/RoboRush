import type { App, Plugin as VuePlugin } from "vue";
import axios from "axios";

const configFile = "/config.json";
let currentConfig: RuntimeConfig | undefined = undefined;

export const RuntimeConfigurationPlugin: VuePlugin & { getConfig: () => Promise<RuntimeConfig> } = {
  install(app: App) {
    axios
      .get(configFile)
      .then((response) => {
        const orgConfig = response.data;

        currentConfig = {
          plausibleDomain: nullOrSet(orgConfig.plausibleDomain),
          plausibleCustomApiHost: nullOrSet(orgConfig.plausibleCustomApiHost),
        };
      })
      .catch((error) => {
        console.info("No config found, using defaults " + error);
        currentConfig = {
          plausibleDomain: undefined,
          plausibleCustomApiHost: undefined,
        };
      });

    this.getConfig().then((config) => app.provide<RuntimeConfig>("runtimeConfiguration", config));
  },

  getConfig(): Promise<RuntimeConfig> {
    return new Promise<RuntimeConfig>(waitForConfig);
  },
};

function waitForConfig(resolve: (value: RuntimeConfig) => void, reject: (value: any) => void) {
  if (currentConfig !== undefined) {
    resolve(currentConfig);
  } else {
    console.info("Waiting");
    setTimeout(() => waitForConfig(resolve, reject), 1);
  }
}

function nullOrSet(value: string): string | undefined {
  if (value === undefined || value === null || value.length < 1) {
    return undefined;
  } else {
    return value;
  }
}

export interface RuntimeConfig {
  plausibleDomain: string | undefined;
  plausibleCustomApiHost: string | undefined;
}
