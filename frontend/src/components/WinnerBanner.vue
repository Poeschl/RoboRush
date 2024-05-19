<template>
  <div
      class="box is-color-black is-flex is-justify-content-center is-size-4"
      :class="{'has-background-success' : winnerAvailable, 'has-background-warning' : !winnerAvailable}"
      v-if="gameStore.currentGame.currentState === GameState.ENDED">
    <p v-if="gameStore.currentGame.nameOfWinningRobot != null">{{ gameStore.currentGame.nameOfWinningRobot }} won the round!</p>
    <p v-else>No robot reached the target!</p>
  </div>
</template>

<script setup lang="ts">

import {GameState} from "@/models/Game";
import {useGameStore} from "@/stores/GameStore";
import {computed} from "vue";

const gameStore = useGameStore();

const winnerAvailable = computed(() => gameStore.currentGame.nameOfWinningRobot != null)

</script>

<style scoped lang="scss">
.box {
  width: 100%;
}
</style>
