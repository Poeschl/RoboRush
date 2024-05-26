<template>
  <FullScreenInfoBar class="mb-5" />
  <div class="fullscreen-container ml-4 mr-4">
    <div class="columns">
      <div class="column is-flex is-justify-content-center is-align-items-start">
        <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" style="height: 100%" />
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
import FullScreenInfoBar from "@/components/FullScreenInfoBar.vue";

const gameStore = useGameStore();
</script>

<style scoped lang="scss">
.fullscreen-container {
  // Beware of very hacky height
  height: calc(100vh - 1.5rem - 1.5rem - 52px);
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
  margin-left: calc(var(--bulma-column-gap) * 2);
}
</style>
