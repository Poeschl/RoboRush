<template>
  <div v-if="loading">
    <progress class="progress is-medium"></progress>
  </div>
  <div class="box" v-else v-for="map in maps">
    <div class="columns">
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div>Active</div>
        <button
          class="button"
          title="Toggle active for map"
          @click="toggleMapActive(map)"
          :class="{ 'is-loading': processing.active == map.id }"
          :disabled="map.active && activeMaps.length < 2"
        >
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-square-check" class="fa-xl" v-if="map.active" />
            <FontAwesomeIcon icon="fa-regular fa-square" class="fa-xl" v-else />
          </div>
        </button>
      </div>
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div>Preview</div>
        <button class="button mr-1" title="Preview map">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-magnifying-glass" />
          </div>
        </button>
      </div>
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div>Name</div>
        <div class="is-size-5">{{ map.mapName }}</div>
      </div>
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div>Size</div>
        <div class="is-size-5">{{ map.size.width }} x {{ map.size.height }}</div>
      </div>
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div>Max player</div>
        <div class="is-size-5">{{ map.possibleStartPositions.length }}</div>
      </div>
      <div class="column is-1 is-flex is-align-items-center is-justify-content-end">
        <button
          class="button is-text remove"
          title="Remove map"
          v-if="maps.length > 1"
          :class="{ 'is-loading': processing.delete == map.id }"
          @click="removeMap(map)"
        >
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-trash" />
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed, ref } from "vue";
import type { PlaygroundMap } from "@/models/Map";

const configStore = useConfigStore();

const loading = computed<boolean>(() => configStore.availableMaps.maps.length == 0);
const maps = computed<PlaygroundMap[]>(() => configStore.availableMaps.maps);
const processing = ref<{ active: number | undefined; delete: number | undefined }>({ active: undefined, delete: undefined });
const activeMaps = computed<PlaygroundMap[]>(() => maps.value.filter((map) => map.active));

const removeMap = (map: PlaygroundMap) => {
  processing.value.delete = map.id;
  configStore.removeMap(map.id).finally(() => {
    processing.value.delete = undefined;
  });
};

const toggleMapActive = (map: PlaygroundMap) => {
  processing.value.active = map.id;
  configStore.setMapActive(map.id, !map.active).finally(() => {
    processing.value.active = undefined;
  });
};
</script>

<style scoped lang="scss">
@use "bulma/sass/utilities/initial-variables";
@use "bulma/sass/utilities/derived-variables";

.button.remove:hover {
  color: initial-variables.$black-ter;
  background-color: derived-variables.$danger;
}
</style>
