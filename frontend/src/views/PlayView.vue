<template>
  <div class="is-flex is-justify-content-center">
    <FullMapCanvas :robot-data="robots" :map-data="heightMap" />
  </div>
</template>

<script setup lang="ts">
import FullMapCanvas from "@/components/FullMapCanvas.vue";
import type { Tile } from "@/models/Map";
import { computed } from "vue";
import type { PublicRobot } from "@/models/Robot";
import { useGameStore } from "@/stores/GameStore";

const gameStore = useGameStore();

const heightMap = computed<Tile[]>(() => gameStore.heightMap);
const robots = computed<PublicRobot[]>(() => gameStore.robots);
</script>

<style scoped></style>
