<template>
  <div>
    <NavBar class="mb-5" />
    <main class="container">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import NavBar from "@/components/NavBar.vue";
import { useGameStore } from "@/stores/GameStore";
import { computed, type ComputedRef, watch } from "vue";
import { useUserStore } from "@/stores/UserStore";
import { useSystemStore } from "@/stores/SystemStore";
import { useRouter } from "vue-router";

console.info(`Swagger UI: ${window.location.origin}/api/swagger-ui`);

const router = useRouter();

const userStore = useUserStore();
const systemStore = useSystemStore();
const gameStore = useGameStore();

//Start the websocket
gameStore.updateMap();
gameStore.updateRobots();
gameStore.initWebsocket(computed(() => userStore.user));

if (userStore.loggedIn) {
  gameStore.retrieveUserRobotState();
}
watch(
  () => userStore.loggedIn,
  (current, previous, onCleanup) => {
    if (current) {
      gameStore.retrieveUserRobotState();
    }
  },
);

watch(
  () => systemStore.backendAvailable,
  (value, oldValue, onCleanup) => {
    if (!value) {
      router.push({ path: "/not-connected" });
    }
  },
);
</script>
