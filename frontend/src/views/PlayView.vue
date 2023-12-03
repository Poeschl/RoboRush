<template>
  <div class="is-flex is-justify-content-center">
    <FullMapCanvas class="is-centered" :width="720" :height="400" :robot-data="robots" :map-data="dummymap" />
  </div>
</template>

<script setup lang="ts">
import FullMapCanvas from "@/components/FullMapCanvas.vue";
import { createDummyMap, HeightMap } from "@/models/Map";
import { ref } from "vue";
import type { PublicRobot } from "@/models/Robot";
import Color from "@/models/Color";

const dummymap = ref<HeightMap>(createDummyMap());
const robots = ref<PublicRobot[]>([
  { position: { x: 2, y: 2 }, color: new Color(127, 0, 0), id: 0 },
  { position: { x: 7, y: 6 }, color: new Color(0, 0, 127), id: 1 },
]);
</script>

<style scoped></style>
