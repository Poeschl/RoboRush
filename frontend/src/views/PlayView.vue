<template>
  <div class="is-flex is-justify-content-center">
    <FullMapCanvas class="is-centered" :width="720" :height="400" :robot-data="[]" :map-data="dummymap" />
  </div>
</template>

<script setup lang="ts">
import FullMapCanvas from "@/components/FullMapCanvas.vue";
import { createDummyMap } from "@/models/Map";

const dummymap = createDummyMap();
</script>

<style scoped></style>
