<template>
  <div class="columns">
    <div class="column is-flex is-align-items-center is-flex-direction-column">
      <WinnerBanner />
      <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" style="width: 90%" />
    </div>
    <div class="column is-one-half is-one-quarter-desktop is-flex-direction-column is-narrow-mobile">
      <RobotActiveList />
      <GameStateBox />
      <RobotScoreBoard />
      <div class="is-flex is-justify-content-flex-end">
        <button class="button is-text is-hidden-touch" title="Open fullscreen view" @click="forwardToFullscreen()">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-expand" />
          </div>
        </button>
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
import { computed } from "vue";
import type { Position } from "@/models/Map";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useRouter } from "vue-router";
import WinnerBanner from "@/components/WinnerBanner.vue";

const gameStore = useGameStore();
const router = useRouter();

const forwardToFullscreen = () => {
  router.push({ path: "/fullscreen" });
};
</script>

<style scoped>
.data-columns .column {
  min-width: 30%;
  max-width: 33%;
}
</style>
