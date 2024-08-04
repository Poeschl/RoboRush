<template>
  <div class="columns is-variable is-2">
    <div class="column is-flex is-align-items-center is-justify-content-start">
      <button
        class="button is-text mr-2"
        title="Toggle active for map"
        @click="toggleMapActive(map)"
        :class="{ 'is-loading': processing.active }"
        :disabled="map.active && unselectable"
      >
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-square-check" class="fa-xl" v-if="map.active" />
          <FontAwesomeIcon icon="fa-regular fa-square" class="fa-xl" v-else />
        </div>
      </button>
      <MapCanvasComponent class="preview-map" :map="map" />
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
    <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
      <div>Max robot fuel</div>
      <div class="is-size-5">{{ map.maxRobotFuel }}</div>
    </div>
    <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
      <div>Solar charge</div>
      <div class="is-size-5">{{ map.solarChargeRate * 100 }} %</div>
    </div>

    <div class="column is-flex is-align-items-center is-justify-content-end">
      <router-link class="button is-text mr-1" title="Edit map attributes" :to="`/config/maps/${map.id}`">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-edit" />
        </div>
      </router-link>
      <button class="button is-text remove" title="Remove map" v-if="removable" :class="{ 'is-loading': processing.delete }" @click="removeMap(map)">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-trash" />
        </div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { ref } from "vue";
import type { PlaygroundMap } from "@/models/Map";
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";

const configStore = useConfigStore();

const processing = ref<{ active: boolean; delete: boolean }>({ active: false, delete: false });

defineProps<{
  map: PlaygroundMap;
  removable: boolean;
  unselectable: boolean;
}>();

const removeMap = (map: PlaygroundMap) => {
  processing.value.delete = true;
  configStore.removeMap(map.id).finally(() => {
    processing.value.delete = false;
  });
};

const toggleMapActive = (map: PlaygroundMap) => {
  processing.value.active = true;
  configStore.setMapActive(map.id, !map.active).finally(() => {
    processing.value.active = false;
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

.column > div {
  text-align: center;
  margin-bottom: 0.2rem;
}

.column > .preview-map {
  text-align: unset;
  width: 60px;
  max-height: 60px;
}

.columns {
  cursor: default;
}
</style>
