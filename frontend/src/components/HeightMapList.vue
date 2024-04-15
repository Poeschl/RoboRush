<template>
  <div v-if="loading">
    <progress class="progress is-medium"></progress>
  </div>
  <div class="box" v-else v-for="map in maps">
    <HeightMapListEntry :map="map" :removable="maps.length > 1" :unselectable="activeMaps.length <= 1" />
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed } from "vue";
import type { PlaygroundMap } from "@/models/Map";
import HeightMapListEntry from "@/components/HeightMapListEntry.vue";

const configStore = useConfigStore();

const loading = computed<boolean>(() => configStore.availableMaps.maps.length == 0);
const maps = computed<PlaygroundMap[]>(() => configStore.availableMaps.maps);
const activeMaps = computed<PlaygroundMap[]>(() => maps.value.filter((map) => map.active));
</script>

<style scoped lang="scss"></style>
