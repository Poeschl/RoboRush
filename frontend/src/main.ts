import { createApp } from "vue";
import "./assets/main.scss";
import App from "./App.vue";
import router from "./router/main";
import Plausible from "plausible-tracker";
import { RuntimeConfigurationPlugin } from "./plugins/RuntimeConfigurationPlugin";

const app = createApp(App);

const runtimeConfigPlugin = RuntimeConfigurationPlugin;
app.use(runtimeConfigPlugin);

app.use(router);

runtimeConfigPlugin.getConfig().then((runtimeConfig) => {
  if (runtimeConfig.plausibleDomain !== undefined) {
    const plausible = Plausible({
      domain: runtimeConfig.plausibleDomain,
      hashMode: true,
      apiHost: runtimeConfig.plausibleCustomApiHost,
    });
    plausible.enableAutoPageviews();
    app.provide("plausible", plausible);
  }
});

app.mount("#app");
