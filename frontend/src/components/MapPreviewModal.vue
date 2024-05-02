<template>
  <BaseModalWithActions>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-map-location-dot" />
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
          :path-to-display="displayPath"
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
                :class="{ 'is-loading': routingActive }"
                :disabled="!routingActionEnabled"
                title="Calculates the shortest path between the selected points. (Ignoring the heights)"
                @click="calculatePath(true)"
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
                :class="{ 'is-loading': routingActive }"
                :disabled="!routingActionEnabled"
                title="Calculates the path with the minimal fuel requirement between the selected points."
                @click="calculatePath()"
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
import { computed, onMounted, onUnmounted, ref } from "vue";
import BaseModalWithActions from "@/components/templates/BaseModalWithActions.vue";
import type { PathFindingWorkerInput } from "@/workers/PathFindingWorker";
import log from "loglevel";

const pathDrawEnabled = ref<boolean>(false);
const drawnPath = ref<Path>({ points: [] });
const displayPath = ref<Path>({ points: [] });
const routingActionEnabled = computed<boolean>(() => drawnPath.value.points.length > 1);
const routingActive = ref<boolean>(false);
const minimalTurns = computed<number>(() => displayPath.value.points.length);
const requiredFuel = computed<number>(() => determineCostOfPath(displayPath.value));

const routingWorker = new Worker(new URL("../workers/PathFindingWorker.ts", import.meta.url), { type: "module" });

const props = defineProps<{
  map: PlaygroundMap;
}>();

defineEmits<{
  (e: "close"): void;
}>();

onMounted(() => {
  routingWorker.onmessage = (message) => {
    log.info("Retrieved path from worker");
    displayPath.value = JSON.parse(message.data);
    routingActive.value = false;
  };
});

onUnmounted(() => {
  routingWorker.terminate();
});

const mapWidth = computed(() => {
  if (props.map && props.map.size.width <= 50) {
    return "40rem";
  } else if (props.map.size.width < 100) {
    return "70rem";
  } else {
    return "110rem";
  }
});

const calculatePath = (ignoreHeights: boolean = false) => {
  routingActive.value = true;
  // Convert data to json, since couldn't get the object ot work
  routingWorker.postMessage(
    JSON.stringify({
      mapSize: props.map.size,
      heightMap: props.map.mapData,
      inputPath: drawnPath.value,
      ignoreHeights: ignoreHeights,
    } as PathFindingWorkerInput),
  );
};

const determineCostOfPath = (path: Path) => {
  let costSum = 0;
  for (let index = 0; index < path.points.length - 1; index++) {
    const first = path.points[index];
    const second = path.points[index + 1];

    const firstHeight = props.map.mapData.find((it) => it.position.x === first.x && it.position.y === first.y)!.height;
    const secondHeight = props.map.mapData.find((it) => it.position.x === second.x && it.position.y === second.y)!.height;

    const heightDiff = secondHeight - firstHeight;
    if (heightDiff < 0) {
      // If the difference is negative aka "down the hill" there is only static cost. (1 is used for static cost)
      costSum += 1;
    } else {
      costSum += 1 + heightDiff;
    }
  }
  return costSum;
};
</script>

<style scoped lang="scss">
:deep(.modal-card) {
  width: fit-content;
  max-width: 90%;
}
</style>
