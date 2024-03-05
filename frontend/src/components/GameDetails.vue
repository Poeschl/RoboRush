<template>
  <div class="box">
    <div class="is-size-3 mb-3">Game Details</div>
    <div class="level">
      <div class="level-left">
        <div class="level-item is-size-5">
          <FontAwesomeIcon ref="stateIcon" icon="fa-solid fa-rotate" class="mr-2 state-change-animation" :class="{ 'fa-spin': !stateAnimateAgain }" />
          Current game state
        </div>
      </div>
      <div class="level-right">
        <div class="level-item">
          <div>{{ currentGame.currentState }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from "@/stores/GameStore";
import { computed, ref, watch } from "vue";

const gameStore = useGameStore();

const stateIcon = ref<SVGElement>();
const stateAnimateAgain = ref<boolean>(false);

const currentGame = computed(() => gameStore.currentGame);

watch(
  () => currentGame.value.currentState,
  () => {
    // will triger the one-time animation again (hopefully)
    stateAnimateAgain.value = true;
    setTimeout(() => (stateAnimateAgain.value = false), 100);
  },
);
</script>

<style scoped lang="scss">
.state-change-animation {
  animation-iteration-count: 1;
}
</style>
