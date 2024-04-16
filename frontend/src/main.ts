import { createApp } from "vue";
import "./assets/main.scss";
import App from "./App.vue";
import { router } from "./router/main";
import Plausible from "plausible-tracker";
import { library } from "@fortawesome/fontawesome-svg-core";
import {
  faArrowRightFromBracket,
  faArrowRightToBracket,
  faArrowRotateLeft,
  faBook,
  faCaretDown,
  faCaretLeft,
  faCaretRight,
  faCaretUp,
  faChalkboard,
  faCheck,
  faClipboard,
  faGasPump,
  faHourglassHalf,
  faLocationDot,
  faPlay,
  faRobot,
  faRotate,
  faRoute,
  faSatelliteDish,
  faUserPlus,
  faUserTie,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import { faCopy, faUser } from "@fortawesome/free-regular-svg-icons";
import { useLogging } from "@/config/logging";
import { Env } from "@/Env";

const app = createApp(App);

library.add(
  faRoute,
  faUserPlus,
  faArrowRightToBracket,
  faArrowRightFromBracket,
  faUser,
  faCopy,
  faRobot,
  faGasPump,
  faLocationDot,
  faClipboard,
  faRotate,
  faCaretUp,
  faCaretRight,
  faCaretDown,
  faCaretLeft,
  faSatelliteDish,
  faPlay,
  faUserTie,
  faBook,
  faChalkboard,
  faArrowRotateLeft,
  faCheck,
  faHourglassHalf,
);
app.component("FontAwesomeIcon", FontAwesomeIcon);

app.use(router);

// Use pinia for local state storing
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

if (Env.plausibleDomain !== undefined && Env.plausibleDomain.length > 0) {
  const plausible = Plausible({
    domain: Env.plausibleDomain,
    apiHost: Env.plausibleCustomApiHost,
  });
  plausible.enableAutoPageviews();
  app.provide("plausible", plausible);
}

useLogging().setup();
app.mount("#app");
