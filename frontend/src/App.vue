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

console.info(`Swagger UI: ${window.location.origin}/api/swagger-ui`);

const gameStore = useGameStore();
gameStore.updateMap();
gameStore.updateRobots();

//Start the websocket
new WebsocketService(gameStore);
</script>
