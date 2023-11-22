import { createRouter, createWebHashHistory } from "vue-router";

const SampleView = () => import("@/views/SampleView.vue");

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      component: SampleView,
    },
  ],
});
