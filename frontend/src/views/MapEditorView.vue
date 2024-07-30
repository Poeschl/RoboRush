<template>
  <div class="columns editor-root">
    <div class="column is-flex is-align-items-center is-flex-direction-column">
      <MapCanvasComponent
        :map="map"
        :drawable-path="pathDrawEnabled"
        @path-update="(path) => (drawnPath = path)"
        :path-to-display="displayPath"
        :style="{ width: mapWidth }"
      />
    </div>
    <div class="column is-one-half is-one-quarter-desktop is-flex-direction-column is-narrow-mobile">
      <div class="tabs is-right">
        <ul>
          <li :class="{ 'is-active': activeTab == EditorTabs.ATTRIBUTES }">
            <a @click="activeTab = EditorTabs.ATTRIBUTES">
              <div class="icon">
                <FontAwesomeIcon icon="fa-regular fa-rectangle-list" />
              </div>
            </a>
          </li>
          <li :class="{ 'is-active': activeTab == EditorTabs.LOCATIONS }">
            <a @click="activeTab = EditorTabs.LOCATIONS">
              <div class="icon">
                <FontAwesomeIcon icon="fa-solid fa-location-dot" />
              </div>
            </a>
          </li>
          <li :class="{ 'is-active': activeTab == EditorTabs.EXPORT }">
            <a @click="activeTab = EditorTabs.EXPORT">
              <div class="icon">
                <FontAwesomeIcon icon="fa-solid fa-file-export" />
              </div>
            </a>
          </li>
        </ul>
      </div>
      <MapEditorAttributesBox :map="map" v-if="map != undefined && activeTab == EditorTabs.ATTRIBUTES" @modifiedMap="updateMapFromStore" />
      <div class="box" v-if="map != undefined && activeTab == EditorTabs.LOCATIONS">LOCATIONS</div>
      <div class="box" v-if="map != undefined && activeTab == EditorTabs.EXPORT">EXPORT</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import type { Path, PlaygroundMap } from "@/models/Map";
import log from "loglevel";
import { useConfigStore } from "@/stores/ConfigStore";
import type { PathFindingWorkerInput } from "@/workers/PathFindingWorker";
import MapEditorAttributesBox from "@/components/MapEditorAttributesBox.vue";

const route = useRoute();
const router = useRouter();
const configStore = useConfigStore();

enum EditorTabs {
  ATTRIBUTES = "ATTRIBUTES",
  LOCATIONS = "LOCATIONS",
  EXPORT = "EXPORT",
}

const mapId = ref<number>();
const map = ref<PlaygroundMap>();
const activeTab = ref<EditorTabs>(EditorTabs.ATTRIBUTES);
const pathDrawEnabled = ref<boolean>(false);
const drawnPath = ref<Path>({ points: [] });
const displayPath = ref<Path>({ points: [] });
const routingActionEnabled = computed<boolean>(() => drawnPath.value.points.length > 1);
const routingActive = ref<boolean>(false);
const minimalTurns = computed<number>(() => displayPath.value.points.length);
const requiredFuel = computed<number>(() => determineCostOfPath(displayPath.value));

const routingWorker = new Worker(new URL("../workers/PathFindingWorker.ts", import.meta.url), { type: "module" });

onMounted(() => {
  const inputId = route.params.mapId;

  if (inputId.length > 0) {
    log.info(`Load map with id ${inputId}`);
    mapId.value = parseInt(inputId as string);
    updateMapFromStore();

    if (map.value == undefined) {
      router.push({ path: "/config/maps" });
    }
  } else {
    router.push({ path: "/config/maps" });
  }

  routingWorker.onmessage = (message) => {
    log.info("Retrieved path from worker");
    displayPath.value = JSON.parse(message.data);
    routingActive.value = false;
  };
});

onUnmounted(() => {
  routingWorker.terminate();
});

const updateMapFromStore = () => {
  map.value = configStore.availableMaps.maps.find((map) => map.id === mapId.value);
};

const mapWidth = computed(() => {
  if (map.value != undefined && map.value.size.width <= 50) {
    return "55%";
  } else if (map.value != undefined && map.value.size.width < 100) {
    return "60%";
  } else {
    return "90%";
  }
});

const calculatePath = (ignoreHeights: boolean = false) => {
  if (map.value != undefined) {
    routingActive.value = true;
    // Convert data to json, since couldn't get the object ot work
    routingWorker.postMessage(
      JSON.stringify({
        mapSize: map.value.size,
        heightMap: map.value.mapData,
        inputPath: drawnPath.value,
        ignoreHeights: ignoreHeights,
      } as PathFindingWorkerInput),
    );
  }
};

const determineCostOfPath = (path: Path) => {
  let costSum = 0;
  if (map.value != undefined) {
    for (let index = 0; index < path.points.length - 1; index++) {
      const first = path.points[index];
      const second = path.points[index + 1];

      const firstHeight = map.value.mapData.find((it) => it.position.x === first.x && it.position.y === first.y)!.height;
      const secondHeight = map.value.mapData.find((it) => it.position.x === second.x && it.position.y === second.y)!.height;

      const heightDiff = secondHeight - firstHeight;
      if (heightDiff < 0) {
        // If the difference is negative aka "down the hill" there is only static cost. (1 is used for static cost)
        costSum += 1;
      } else {
        costSum += 1 + heightDiff;
      }
    }
  }
  return costSum;
};
</script>

<style scoped lang="scss">
.editor-root {
  margin: 0 4rem 1rem;
}
</style>
