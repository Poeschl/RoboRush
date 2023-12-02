<template>
  <div class="has-background-black" :style="{ width: width + 'px', height: height + 'px' }">
    <canvas id="worldmap" class="is-absolute" :width="width" :height="height" />
    <canvas id="robots" class="is-absolute" :width="width" :height="height" />
  </div>
</template>

<script setup lang="ts">
import type { HeightMap } from "@/models/Map";
import type { PublicRobot } from "@/models/Robot";

const props = defineProps<{
  width: number;
  height: number;
  mapData: HeightMap;
  robotData: PublicRobot[];
}>();
</script>

<style scoped></style>
