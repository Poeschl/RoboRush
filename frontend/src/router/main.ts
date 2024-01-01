import { createRouter, createWebHashHistory } from "vue-router";
import { useSystemStore } from "@/stores/SystemStore";

const PlayView = () => import("@/views/PlayView.vue");
const HowToView = () => import("@/views/HowToView.vue");
const NotConnectedView = () => import("@/views/NotConnectedView.vue");

export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      component: PlayView,
    },
    {
      path: "/how-to-play",
      component: HowToView,
    },
    {
      path: "/not-connected",
      component: NotConnectedView,
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const systemStore = useSystemStore();
  const notConnectedView = "/not-connected";
  systemStore.checkBackendAvailability();

  if (!systemStore.backendAvailable && to.path !== notConnectedView) {
    return next({ path: notConnectedView });
  } else if (to.path == notConnectedView && systemStore.backendAvailable) {
    return next({ path: "/" });
  }

  return next();
});
