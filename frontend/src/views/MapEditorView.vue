<template>
  <div v-if="mapLoading" class="container mb-5">
    <progress class="progress is-medium"></progress>
  </div>
  <div v-else class="columns editor-root">
    <div class="column is-flex is-align-items-center is-flex-direction-column">
      <MapCanvasComponent
        :map="map"
        :drawable-path="pathDrawEnabled"
        :clickable-tiles="mapClickEnabled"
        @path-update="(path) => (drawnPath = path)"
        @clicked-tile="(position) => (lastClickedPosition = position)"
        :path-to-display="displayPath"
        :style="{ width: mapWidth }"
      />
    </div>
    <div class="column is-one-half is-one-quarter-desktop is-flex-direction-column is-narrow-mobile">
      <div class="tabs is-right">
        <ul>
          <li :class="{ 'is-active': activeTab == EditorTab.ATTRIBUTES }">
            <a @click="switchToTab(EditorTab.ATTRIBUTES)">
              <div class="icon">
                <FontAwesomeIcon icon="fa-regular fa-rectangle-list" />
              </div>
            </a>
          </li>
          <li :class="{ 'is-active': activeTab == EditorTab.LOCATIONS }">
            <a @click="switchToTab(EditorTab.LOCATIONS)">
              <div class="icon">
                <FontAwesomeIcon icon="fa-solid fa-location-dot" />
              </div>
            </a>
          </li>
          <li :class="{ 'is-active': activeTab == EditorTab.EXPORT }">
            <a @click="switchToTab(EditorTab.EXPORT)">
              <div class="icon">
                <FontAwesomeIcon icon="fa-solid fa-file-export" />
              </div>
            </a>
          </li>
        </ul>
      </div>
      <MapEditorAttributesBox :map="map" v-if="map != undefined && activeTab == EditorTab.ATTRIBUTES" @modifiedMap="updateMapFromStore" />
      <div v-if="map != undefined && activeTab == EditorTab.LOCATIONS">
        <MapEditorLocationChangeBox
          :map="map"
          :clickedPosition="lastClickedPosition"
          @mapClickEnabled="(enabled) => enableMapClick(enabled)"
          @modifiedMap="updateMapFromStore"
        />
        <MapEditorMeasureBox
          :map="map"
          :drawnPath="drawnPath"
          :pathDrawEnabled="pathDrawEnabled"
          @update:displayPath="(value) => (displayPath = value)"
          @update:pathDrawEnabled="(value) => (pathDrawEnabled = value)"
        />
      </div>
      <div class="box" v-if="activeTab == EditorTab.EXPORT">EXPORT</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import MapCanvasComponent from "@/components/MapCanvasComponent.vue";
import type { Path, PlaygroundMap, Position } from "@/models/Map";
import log from "loglevel";
import { useConfigStore } from "@/stores/ConfigStore";
import MapEditorAttributesBox from "@/components/MapEditorAttributesBox.vue";
import MapEditorMeasureBox from "@/components/MapEditorMeasureBox.vue";
import MapEditorLocationChangeBox from "@/components/MapEditorLocationChangeBox.vue";

const route = useRoute();
const router = useRouter();
const configStore = useConfigStore();

enum EditorTab {
  ATTRIBUTES = "ATTRIBUTES",
  LOCATIONS = "LOCATIONS",
  EXPORT = "EXPORT",
}

const mapId = ref<number>();
const map = ref<PlaygroundMap>();
const mapLoading = computed<boolean>(() => map.value == undefined);
const activeTab = ref<EditorTab>(EditorTab.ATTRIBUTES);
const pathDrawEnabled = ref<boolean>(false);
const drawnPath = ref<Path>({ points: [] });
const displayPath = ref<Path>({ points: [] });
const mapClickEnabled = ref<boolean>(false);
const lastClickedPosition = ref<Position | undefined>();

onMounted(() => {
  const inputId = route.params.mapId;

  if (inputId.length > 0) {
    log.info(`Load map with id ${inputId}`);
    mapId.value = parseInt(inputId as string);
    updateMapFromStore().then(() => {
      // If no map is found after data load
      if (map.value == undefined) {
        router.push({ path: "/config/maps" });
      }
    });
  } else {
    router.push({ path: "/config/maps" });
  }
});

const updateMapFromStore = (): Promise<void> => {
  if (configStore.availableMaps.maps.length > 0) {
    return new Promise<void>((resolve) => {
      map.value = configStore.availableMaps.maps.find((map) => map.id === mapId.value);
      resolve();
    });
  } else {
    // Load map data
    return configStore.updateMaps().then(() => {
      updateMapFromStore();
    });
  }
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

const switchToTab = (tab: EditorTab) => {
  if (activeTab.value != tab) {
    activeTab.value = tab;
    resetMapMeasureDrawing();
  }
};

const resetMapMeasureDrawing = () => {
  displayPath.value.points = [];
  drawnPath.value.points = [];
  pathDrawEnabled.value = false;
};

const enableMapClick = (enabled: boolean) => {
  lastClickedPosition.value = undefined;
  mapClickEnabled.value = enabled;
};
</script>

<style scoped lang="scss">
.editor-root {
  margin: 0 10rem 1rem;
}
</style>
