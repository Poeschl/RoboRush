<template>
  <div class="box" v-for="map in maps">
    {{ map.mapName }}
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed, ref } from "vue";
import type { PlaygroundMap } from "@/models/Map";

const configStore = useConfigStore();

const loading = computed<boolean>(() => configStore.availableMaps.maps.length == 0);
const maps = computed<PlaygroundMap[]>(() => configStore.availableMaps.maps);
</script>

<style scoped lang="scss"></style>
