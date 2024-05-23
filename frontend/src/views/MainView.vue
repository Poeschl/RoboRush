<template>
  <div class="columns">
    <div class="column is-flex is-justify-content-center is-align-items-start">
      <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" style="width: 90%" />
    </div>
    <div class="column is-one-quarter is-flex-direction-column is-narrow">
      <RobotActiveList />
      <GameStateBox />
      <RobotScoreBoard />
      <div class="is-flex is-justify-content-flex-end">
        <button class="button is-text" title="Open fullscreen view" @click="forwardToFullscreen()">
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
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useRouter } from "vue-router";

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
