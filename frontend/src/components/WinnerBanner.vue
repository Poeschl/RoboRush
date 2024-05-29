<template>
  <div
    class="box is-color-black is-flex is-justify-content-center is-size-4 p-3"
    :class="{ 'has-background-success': winnerAvailable, 'has-background-warning': !winnerAvailable }"
    v-if="gameStore.currentGame.currentState === GameState.ENDED"
  >
    <div class="is-flex is-flex-direction-column is-align-items-center" v-if="winnerAvailable">
      <div>
        🏆 <span class="has-text-weight-bold">{{ winnerName }}</span> won the round! 🏆
      </div>
      <div v-confetti="{ stageHeight: 1000, particleCount: 300 }" />
    </div>
    <div class="has-text-weight-bold" v-else>No robot reached the target!</div>
  </div>
</template>

<script setup lang="ts">
import { GameState } from "@/models/Game";
import { useGameStore } from "@/stores/GameStore";
import { computed } from "vue";
import { vConfetti } from "@neoconfetti/vue";

const gameStore = useGameStore();

const winnerAvailable = computed<boolean>(() => gameStore.currentGame.nameOfWinningRobot != null);
const winnerName = computed<string | undefined>(() => gameStore.currentGame.nameOfWinningRobot);
</script>

<style scoped lang="scss">
.box {
  width: 100%;
}
</style>
