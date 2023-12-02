import { createApp } from "vue";
import "./assets/main.scss";
import App from "./App.vue";
import router from "./router/main";
import Plausible from "plausible-tracker";
import { RuntimeConfigurationPlugin } from "./plugins/RuntimeConfigurationPlugin";
import { library } from "@fortawesome/fontawesome-svg-core";
import { faRoute } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { faUser } from "@fortawesome/free-regular-svg-icons";

const app = createApp(App);

const runtimeConfigPlugin = RuntimeConfigurationPlugin;
app.use(runtimeConfigPlugin);

library.add(faRoute, faUser);
app.component("FontAwesomeIcon", FontAwesomeIcon);

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
