<template>
  <BaseModal>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-magnifying-glass" />
        Map Preview - {{ map.mapName }}
      </h4>
    </template>

    <template #content>
      <div class="is-flex is-justify-content-center">
        <MapCanvasComponent :map="map" :style="{ width: mapWidth }" />
      </div>
    </template>
    <template #footer>
      <button class="button" @click="$emit('close')">Close</button>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from "@/components/templates/BaseModal.vue";
import type { PlaygroundMap } from "@/models/Map";
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import { computed } from "vue";

const props = defineProps<{
  map: PlaygroundMap;
}>();

defineEmits<{
  (e: "close"): void;
}>();

const mapWidth = computed(() => {
  if (props.map && props.map.size.width <= 50) {
    return "40rem";
  } else if (props.map.size.width < 100) {
    return "70rem";
  } else {
    return "110rem";
  }
});
</script>

<style scoped lang="scss">
:deep(.modal-card) {
  width: fit-content;
  max-width: 90%;
}
</style>
