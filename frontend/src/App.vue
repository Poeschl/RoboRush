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
import WebsocketService from "@/services/WebsocketService";
import { watch } from "vue";
import { useUserStore } from "@/stores/UserStore";

console.info(`Swagger UI: ${window.location.origin}/api/swagger-ui`);

const gameStore = useGameStore();
gameStore.updateMap();
gameStore.updateRobots();

const userStore = useUserStore();
if (userStore.loggedIn) {
  gameStore.updateUserRobot();
}
watch(
  () => userStore.loggedIn,
  (current, previous, onCleanup) => {
    if (current) {
      gameStore.updateUserRobot();
    }
  },
);

//Start the websocket
new WebsocketService(gameStore);
</script>
