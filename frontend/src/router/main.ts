import { createRouter, createWebHistory } from "vue-router";
import { useSystemStore } from "@/stores/SystemStore";
import { useUserStore } from "@/stores/UserStore";

const MainView = () => import("@/views/MainView.vue");
const MyRobotView = () => import("@/views/MyRobotView.vue");
const HowToView = () => import("@/views/HowToView.vue");
const NotConnectedView = () => import("@/views/NotConnectedView.vue");
const GameConfigView = () => import("@/views/GameConfigView.vue");
const FullScreenView = () => import("@/views/FullScreenView.vue");

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: MainView,
    },
    {
      path: "/myRobot",
      component: MyRobotView,
      meta: { requiresNonAdmin: true },
    },
    {
      path: "/how-to-play",
      component: HowToView,
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
    {
      path: "/fullscreen",
      component: FullScreenView,
      meta: { hideFooter: true, hideNavBar: true, noContainer: true },
    },
    {
      path: "/fullscreen/white",
      component: FullScreenView,
      meta: { hideFooter: true, hideNavBar: true, noContainer: true, lightMode: true },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const systemStore = useSystemStore();
  const userStore = useUserStore();
  const fullscreenView = "/fullscreen";
  const notConnectedView = "/not-connected";
  systemStore.checkBackendAvailability();

  if (!systemStore.backendAvailable && to.path !== notConnectedView && from.path !== fullscreenView) {
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
