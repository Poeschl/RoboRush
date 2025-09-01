<template>
  <div class="fullscreen-container pl-4 pr-4" :class="{ 'has-background-grey': lightMode }">
    <div class="columns">
      <div class="column is-one-fifth">
        <FullScreenInfoBox />
        <div class="is-flex is-justify-content-flex-start hidden-unless-mouse">
          <button class="button is-text is-hidden-touch" title="Switch Dark/White" @click="toggleLightMode">
            <div class="icon">
              <FontAwesomeIcon icon="fa-solid fa-circle-half-stroke" />
            </div>
          </button>
        </div>
      </div>
      <div class="column is-flex is-align-items-center is-flex-direction-column">
        <WinnerBanner />
        <MapCanvasComponent :robots="gameStore.robots" :map="gameStore.currentMap" style="max-height: 100%; width: 100%" />
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
import { onMounted, ref } from "vue";
import { useConfigStore } from "@/stores/ConfigStore";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useRoute } from "vue-router";

const configStore = useConfigStore();
const gameStore = useGameStore();

const route = useRoute();
const lightMode = ref<boolean>(false);

onMounted(() => {
  lightMode.value = route.meta.lightMode == true;
});

const toggleLightMode = () => {
  lightMode.value = !lightMode.value;
};
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
  transform-origin: top right;
  height: fit-content;
}

.hidden-unless-mouse {
  opacity: 0;

  &:hover {
    opacity: 1;
  }
}
</style>
