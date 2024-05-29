<template>
  <div class="fullscreen-container ml-4 mr-4">
    <div class="columns">
      <div class="column is-one-fifth">
        <FullScreenInfoBox />
      </div>
      <div class="column is-flex is-align-items-center is-flex-direction-column pr-5">
        <WinnerBanner />
        <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" :positions-to-draw="shownTiles" style="height: 100%" />
      </div>
      <div class="column is-one-fifth is-flex-direction-column is-narrow data-column">
        <RobotScoreBoard />
        <RobotActiveList />
        <GameStateBox />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import { useGameStore } from "@/stores/GameStore";
import GameStateBox from "@/components/GameStateBox.vue";
import RobotActiveList from "@/components/RobotActiveList.vue";
import RobotScoreBoard from "@/components/RobotScoreBoard.vue";
import FullScreenInfoBox from "@/components/FullScreenInfoBox.vue";
import WinnerBanner from "@/components/WinnerBanner.vue";
import { computed } from "vue";
import type { Position } from "@/models/Map";
import { useConfigStore } from "@/stores/ConfigStore";

const configStore = useConfigStore();
const gameStore = useGameStore();

const shownTiles = computed<{ data: Position[] } | undefined>(() => {
  if (configStore.clientSettings.useFogOfWar) {
    const positions = gameStore.globalKnownPositions;

    if (gameStore.currentGame.targetPosition !== undefined) {
      positions.data.push(gameStore.currentGame.targetPosition);
    }
    return positions;
  } else {
    return undefined;
  }
});
</script>

<style scoped lang="scss">
.fullscreen-container {
  // Beware of very hacky height
  height: calc(100vh);
}

.columns {
  height: 100%;
}

.data-column {
  $scale: 1.2;
  transform-origin: top right;
  transform: scale($scale);
  height: fit-content;
  width: calc((20% - var(--bulma-column-gap) / 2) * 1 / $scale);
  margin-left: calc(var(--bulma-column-gap) * 3);
}
</style>
