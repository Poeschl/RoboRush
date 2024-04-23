<template>
  <div class="columns is-variable is-2">
    <div class="column is-flex is-align-items-center is-justify-content-start">
      <button
        class="button is-text mr-1"
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
      <button class="button is-text" title="Preview map" @click="openPreview">
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
    <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
      <div>Max robot fuel</div>
      <div class="is-size-5">{{ map.maxRobotFuel }}</div>
    </div>
    <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
      <div>Solar charge</div>
      <div class="icon">
        <FontAwesomeIcon icon="fa-solid fa-square-check" class="fa-xl" v-if="map.solarChargeEnabled" />
        <FontAwesomeIcon icon="fa-regular fa-square" class="fa-xl" v-else />
      </div>
    </div>

    <div class="column is-flex is-align-items-center is-justify-content-end">
      <button class="button is-text mr-1" title="Edit map attributes" @click="openEdit">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-edit" />
        </div>
      </button>
      <button class="button is-text remove" title="Remove map" v-if="removable" :class="{ 'is-loading': processing.delete }" @click="removeMap(map)">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-trash" />
        </div>
      </button>
    </div>
  </div>
  <MapPreviewModal :map="map" v-if="previewOpen" @close="previewOpen = false" />
  <MapEditModal :map="map" v-if="editOpen" @close="editOpen = false" />
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { ref } from "vue";
import type { PlaygroundMap } from "@/models/Map";
import MapPreviewModal from "@/components/MapPreviewModal.vue";
import MapEditModal from "@/components/MapEditModal.vue";

const configStore = useConfigStore();

const processing = ref<{ active: boolean; delete: boolean }>({ active: false, delete: false });
const previewOpen = ref<boolean>(false);
const editOpen = ref<boolean>(false);

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

const openPreview = () => {
  previewOpen.value = true;
};

const openEdit = () => {
  editOpen.value = true;
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
</style>
