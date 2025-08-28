<template>
  <InfoBoxTemplate title="Measure">
    <div class="columns">
      <div class="column">
        <button
          class="button is-fullwidth"
          :class="{ 'is-primary': pathDrawEnabled }"
          @click="$emit('update:pathDrawEnabled', !pathDrawEnabled)"
          title="If active click on the map to draw a path from clicked tile to clicked tile."
        >
          <div class="icon mr-1">
            <FontAwesomeIcon icon="fa-solid fa-square-check" v-if="pathDrawEnabled" />
            <FontAwesomeIcon icon="fa-regular fa-square" v-else />
          </div>
          Draw Move Path
        </button>
      </div>
    </div>
    <div class="columns">
      <div class="column">
        <div class="field has-addons is-justify-content-center">
          <div class="control">
            <button
              class="button"
              :class="{ 'is-loading': routingActive, 'is-primary': routingType == RoutingType.SHORTEST }"
              :disabled="!routingActionEnabled"
              title="Calculates the shortest path between the selected points. (Ignoring the heights)"
              @click="calculatePath(RoutingType.SHORTEST)"
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
              :class="{ 'is-loading': routingActive, 'is-primary': routingType == RoutingType.FUEL_EFFICIENT }"
              :disabled="!routingActionEnabled"
              title="Calculates the path with the minimal fuel requirement between the selected points."
              @click="calculatePath(RoutingType.FUEL_EFFICIENT)"
            >
              <div class="icon mr-1">
                <FontAwesomeIcon icon="fa-solid fa-route" />
              </div>
              Fuel-efficient
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="columns">
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div class="has-text-centered">Required fuel</div>
        <div class="subtitle has-text-white">{{ requiredFuel }}</div>
      </div>
      <div class="column is-flex is-flex-direction-column is-align-items-center is-justify-content-center">
        <div class="has-text-centered">Minimal turns</div>
        <div class="subtitle has-text-white">{{ minimalTurns }}</div>
      </div>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import type { Path, PlaygroundMap } from "@/models/Map";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import log from "loglevel";
import type { PathFindingWorkerInput } from "@/workers/PathFindingWorker";

enum RoutingType {
  SHORTEST = "SHORTEST",
  FUEL_EFFICIENT = "FUEL_EFFICIENT",
}

const routingActionEnabled = computed<boolean>(() => props.drawnPath.points.length > 1);
const routingActive = ref<boolean>(false);
const routingType = ref<RoutingType>();
const minimalTurns = computed<number>(() => displayPath.value.points.length);
const requiredFuel = computed<number>(() => determineCostOfPath(displayPath.value));
const displayPath = ref<Path>({ points: [] });

const routingWorker = new Worker(new URL("../workers/PathFindingWorker.ts", import.meta.url), { type: "module" });

const props = defineProps<{
  map: PlaygroundMap;
  drawnPath: Path;
  pathDrawEnabled: boolean;
}>();

const emits = defineEmits<{
  (e: "update:pathDrawEnabled", value: boolean): void;
  (e: "update:displayPath", value: Path): void;
}>();

watch(
  () => displayPath.value,
  () => emits("update:displayPath", displayPath.value),
);

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

const calculatePath = (newRoutingType: RoutingType) => {
  routingActive.value = true;
  routingType.value = newRoutingType;
  const ignoreHeights = newRoutingType == RoutingType.SHORTEST;
  // Convert data to json, since couldn't get the object to work
  routingWorker.postMessage(
    JSON.stringify({
      mapSize: props.map.size,
      heightMap: props.map.mapData,
      inputPath: props.drawnPath,
      ignoreHeights: ignoreHeights,
    } as PathFindingWorkerInput),
  );
};

const determineCostOfPath = (path: Path) => {
  let costSum = 0;
  for (let index = 0; index < path.points.length - 1; index++) {
    const first = path.points[index];
    const second = path.points[index + 1];

    const firstHeight = props.map.mapData.find((it) => it.position.x === first!.x && it.position.y === first!.y)!.height;
    const secondHeight = props.map.mapData.find((it) => it.position.x === second!.x && it.position.y === second!.y)!.height;

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

<style scoped lang="scss"></style>
