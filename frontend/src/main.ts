import { createApp } from "vue";
import "./assets/main.scss";
import App from "./App.vue";
import router from "./router/main";
import Plausible from "plausible-tracker";
import { RuntimeConfigurationPlugin } from "./plugins/RuntimeConfigurationPlugin";
import { library } from "@fortawesome/fontawesome-svg-core";
import { faArrowRightFromBracket, faArrowRightToBracket, faRoute, faUserPlus } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import { faCopy, faUser } from "@fortawesome/free-regular-svg-icons";

const app = createApp(App);

const runtimeConfigPlugin = RuntimeConfigurationPlugin;
app.use(runtimeConfigPlugin);

library.add(faRoute, faUserPlus, faArrowRightToBracket, faArrowRightFromBracket, faUser, faCopy);
app.component("FontAwesomeIcon", FontAwesomeIcon);

app.use(router);

// Use pinia for local state storing
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

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
