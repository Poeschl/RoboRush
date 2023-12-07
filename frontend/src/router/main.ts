import { createRouter, createWebHashHistory } from "vue-router";

const PlayView = () => import("@/views/PlayView.vue");
const HowToView = () => import("@/views/HowToView.vue");

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      component: PlayView,
    },
    {
      path: "/howtoplay",
      component: HowToView,
    },
  ],
});
