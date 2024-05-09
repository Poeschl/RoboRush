<template>
  <div class="columns">
    <div class="column is-flex is-justify-content-center is-align-items-start">
      <MapCanvasComponent :robots="robots" :map="currentMap" style="width: 90%" />
    </div>
    <div class="column is-one-quarter is-flex-direction-column is-narrow">
      <RobotActiveList />
      <GameStateBox />
      <RobotScoreBoard />
    </div>
  </div>
</template>

<script setup lang="ts">
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import type { PlaygroundMap } from "@/models/Map";
import { computed } from "vue";
import type { PublicRobot } from "@/models/Robot";
import { useGameStore } from "@/stores/GameStore";
import GameStateBox from "@/components/GameStateBox.vue";
import RobotActiveList from "@/components/RobotActiveList.vue";
import RobotScoreBoard from "@/components/RobotScoreBoard.vue";

const gameStore = useGameStore();

const currentMap = computed<PlaygroundMap | undefined>(() => gameStore.currentMap);
const robots = computed<PublicRobot[]>(() => gameStore.robots);
</script>

<style scoped>
.data-columns .column {
  min-width: 30%;
  max-width: 33%;
}
</style>
