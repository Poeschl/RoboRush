<template>
  <BaseModalWithActions>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-magnifying-glass" />
        Map Preview - {{ map.mapName }}
      </h4>
    </template>

    <template #content>
      <div class="is-flex is-justify-content-center">
        <MapCanvasComponent
          :map="map"
          :style="{ width: mapWidth }"
          :drawable-path="pathDrawEnabled"
          @path-update="(path) => (drawnPath = path)"
          :path-to-display="{
            points: [
              { x: 1, y: 1 },
              { x: 2, y: 1 },
              { x: 2, y: 2 },
              { x: 20, y: 20 },
            ],
          }"
        />
      </div>
    </template>
    <template #actions>
      <div class="columns is-align-items-center">
        <div class="column">
          <button
            class="button"
            :class="{ 'is-info': pathDrawEnabled }"
            @click="pathDrawEnabled = !pathDrawEnabled"
            title="If active click on the map to draw a path from clicked tile to clicked tile."
          >
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-square-check" v-if="pathDrawEnabled" />
              <FontAwesomeIcon icon="fa-regular fa-square" v-else />
            </div>
            Draw Path
          </button>
        </div>
        <div class="column">
          <div class="field has-addons">
            <div class="control">
              <button
                class="button"
                :disabled="!routingFunctionsEnabled"
                title="Calculates the shortest path between the selected points. (Ignoring the heights)"
              >
                <div class="icon mr-1">
                  <FontAwesomeIcon icon="fa-solid fa-route" />
                </div>
                Shortest
              </button>
            </div>
            <div class="control">
              <button
                class="button"
                :disabled="!routingFunctionsEnabled || true"
                title="Calculates the path with the minimal fuel requirement between the selected points."
              >
                <div class="icon mr-1">
                  <FontAwesomeIcon icon="fa-solid fa-route" />
                </div>
                Fuel-efficient
              </button>
            </div>
          </div>
        </div>
        <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
          <div class="has-text-centered">Required fuel</div>
          <div>{{ requiredFuel }}</div>
        </div>
        <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
          <div class="has-text-centered">Minimal turns</div>
          <div>{{ minimalTurns }}</div>
        </div>
      </div>
    </template>
    <template #footer>
      <button class="button" @click="$emit('close')">Close</button>
    </template>
  </BaseModalWithActions>
</template>

<script setup lang="ts">
import type { Path, PlaygroundMap } from "@/models/Map";
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import { computed, ref } from "vue";
import BaseModalWithActions from "@/components/templates/BaseModalWithActions.vue";

const pathDrawEnabled = ref<boolean>(false);
const drawnPath = ref<Path>({ points: [] });
const routingFunctionsEnabled = computed(() => drawnPath.value.points.length > 1);
const requiredFuel = computed<number>(() => 123);
const minimalTurns = computed<number>(() => drawnPath.value.points.length);

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
