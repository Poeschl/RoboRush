import { createRouter, createWebHashHistory } from "vue-router";

const PlayView = () => import("@/views/PlayView.vue");

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      component: PlayView,
    },
  ],
});
