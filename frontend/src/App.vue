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

console.info(`Swagger UI: ${window.location.origin}/rest/swagger-ui`);

const gameStore = useGameStore();
gameStore.updateMap();
gameStore.updateRobots();
</script>
