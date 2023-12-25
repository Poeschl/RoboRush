<template>
  <div class="box">
    <div class="is-size-3 mb-3">Robots List</div>
    <div class="is-flex is-align-items-center" v-for="robot in gameStore.robots">
      <span class="color-dot mr-2" :style="{ 'background-color': robot.color.toHex() }"></span>
      <span>{{ robot.id }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from "@/stores/GameStore";

const gameStore = useGameStore();
</script>

<style scoped lang="scss">
.color-dot {
  width: 1.5ex;
  height: 1.5ex;
  border-radius: 50%;
}
</style>
