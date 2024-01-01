<template>
  <div class="is-flex is-justify-content-center mb-5">
    <FullMapCanvas :robot-data="robots" :map-data="heightMap" />
  </div>
  <div class="columns is-fullwidth is-justify-content-center data-columns">
    <div class="column">
      <RobotsList />
    </div>
    <div class="column">
      <UserRobotDetails />
    </div>
  </div>
</template>

<script setup lang="ts">
import FullMapCanvas from "@/components/FullMapCanvas.vue";
import type { Tile } from "@/models/Map";
import { computed } from "vue";
import type { PublicRobot } from "@/models/Robot";
import { useGameStore } from "@/stores/GameStore";
import RobotsList from "@/components/PublicRobotsList.vue";
import UserRobotDetails from "@/components/UserRobotDetails.vue";

const gameStore = useGameStore();

const heightMap = computed<Tile[]>(() => gameStore.heightMap);
const robots = computed<PublicRobot[]>(() => gameStore.robots);
</script>

<style scoped>
.data-columns .column {
  max-width: 33%;
}
</style>
