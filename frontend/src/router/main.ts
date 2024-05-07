import { createRouter, createWebHistory } from "vue-router";
import { useSystemStore } from "@/stores/SystemStore";
import { useUserStore } from "@/stores/UserStore";

const MainView = () => import("@/views/MainView.vue");
const HowToView = () => import("@/views/HowToView.vue");
const NotConnectedView = () => import("@/views/NotConnectedView.vue");
const GameConfigView = () => import("@/views/GameConfigView.vue");

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: MainView,
    },
    {
      path: "/how-to-play",
      component: HowToView,
      meta: { requiresNonAdmin: true },
    },
    {
      path: "/not-connected",
      component: NotConnectedView,
      meta: { hideFooter: true },
    },
    {
      path: "/config",
      component: GameConfigView,
      meta: { requiresAdmin: true },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const systemStore = useSystemStore();
  const userStore = useUserStore();
  const notConnectedView = "/not-connected";
  systemStore.checkBackendAvailability();

  if (!systemStore.backendAvailable && to.path !== notConnectedView) {
    return next({ path: notConnectedView });
  } else if (to.path == notConnectedView && systemStore.backendAvailable) {
    return next({ path: "/" });
  }

  if (to.matched.some((record) => record.meta.requiresAdmin)) {
    // If a route requires admin privileges, check them first
    if (!userStore.isAdmin) {
      next({ path: from.path });
    }
  } else if (to.matched.some((record) => record.meta.requiresNonAdmin)) {
    // If a route requires admin privileges, check them first
    if (userStore.isAdmin) {
      next({ path: from.path });
    }
  }

  return next();
});
