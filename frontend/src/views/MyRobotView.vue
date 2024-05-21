<template>
  <div class="columns">
    <div class="column is-flex is-align-items-center is-flex-direction-column">
      <WinnerBanner />
      <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" :positions-to-draw="shownTiles" style="width: 90%" />
    </div>
    <div class="column is-one-half is-one-quarter-desktop is-flex-direction-column is-narrow-mobile mobile">
      <GameFlags />
      <GameStateBox />
      <RobotDetails />
      <RobotControl />
    </div>
  </div>
</template>

<script setup lang="ts">
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import { useGameStore } from "@/stores/GameStore";
import RobotControl from "@/components/RobotsControl.vue";
import GameStateBox from "@/components/GameStateBox.vue";
import GameFlags from "@/components/GameFlags.vue";
import RobotDetails from "@/components/RobotDetails.vue";
import WinnerBanner from "@/components/WinnerBanner.vue";
import { computed } from "vue";
import type { Position } from "@/models/Map";

const gameStore = useGameStore();

const shownTiles = computed<{ data: Position[] }>(() => {
  const positions = gameStore.userRobotKnownPositions;

  if (gameStore.currentGame.targetPosition !== undefined) {
    positions.data.push(gameStore.currentGame.targetPosition);
  }
  return positions;
});
</script>

<style scoped>
.data-columns .column {
  min-width: 30%;
  max-width: 33%;
}
</style>
