<template>
  <InfoBoxTemplate title="Edit locations">
    <div class="field is-grouped is-justify-content-center">
      <p class="control">
        <button class="button" title="Clear tile" :class="{ 'is-active': selectedEdit == EditMode.CLEAR }" @click="setMode(EditMode.CLEAR)">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-ban" />
          </div>
        </button>
      </p>
      <p class="control">
        <button class="button start" title="Start tile" :class="{ 'is-active': selectedEdit == EditMode.START }" @click="setMode(EditMode.START)">
          <div class="icon is-double">
            <FontAwesomeIcon icon="fa-solid fa-flag-checkered" />
            <FontAwesomeIcon icon="fa-solid fa-arrow-right" />
          </div>
        </button>
      </p>
      <p class="control">
        <button class="button target" title="Target tile" :class="{ 'is-active': selectedEdit == EditMode.TARGET }" @click="setMode(EditMode.TARGET)">
          <div class="icon is-double">
            <FontAwesomeIcon icon="fa-solid fa-arrow-right" />
            <FontAwesomeIcon icon="fa-solid fa-flag-checkered" />
          </div>
        </button>
      </p>
      <p class="control">
        <button class="button fuel" title="Fuel station tile" :class="{ 'is-active': selectedEdit == EditMode.FUEL }" @click="setMode(EditMode.FUEL)">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-gas-pump" />
          </div>
        </button>
      </p>
    </div>
    <div class="has-text-centered">All actions are saved immediately.</div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import { type PlaygroundMap, type Position, type Tile, TileType } from "@/models/Map";
import { ref, watch } from "vue";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import useMapConstants from "@/config/map";
import log from "loglevel";
import { useConfigStore } from "@/stores/ConfigStore";

enum EditMode {
  NONE = "NONE",
  CLEAR = "CLEAR",
  START = "START",
  TARGET = "TARGET",
  FUEL = "FUEL",
}

const mapConstants = useMapConstants();
const startTileColor = mapConstants.startTileBorderColor.toHex();
const targetTileColor = mapConstants.targetTileBorderColor.toHex();
const fuelTileColor = mapConstants.fuelTileBorderColor.toHex();

const selectedEdit = ref<EditMode>(EditMode.NONE);

const configStore = useConfigStore();

const props = defineProps<{
  map: PlaygroundMap;
  clickedPosition: Position | undefined;
}>();

const emits = defineEmits<{
  (e: "modifiedMap"): void;
  (e: "mapClickEnabled", value: boolean): void;
  (e: "resetMap"): void;
}>();

watch(
  () => props.clickedPosition,
  (value, oldValue, onCleanup) => {
    if (value != undefined) {
      applyChange(value);
    }
  },
);

const applyChange = (position: Position) => {
  if (selectedEdit.value != EditMode.NONE) {
    const originalTile = props.map.mapData.find((it) => it.position.x === position.x && it.position.y === position.y);
    if (originalTile != undefined) {
      log.debug(`Change map on ${JSON.stringify(position)} to ${selectedEdit.value}`);
      let newType: TileType;
      switch (selectedEdit.value) {
        case EditMode.START:
          newType = TileType.START_TILE;
          break;
        case EditMode.TARGET:
          newType = TileType.TARGET_TILE;
          break;
        case EditMode.FUEL:
          newType = TileType.FUEL_TILE;
          break;
        case EditMode.CLEAR:
        default:
          newType = TileType.DEFAULT_TILE;
          break;
      }

      const mapId = props.map.id;
      const newTile: Tile = { position: originalTile.position, height: originalTile.height, type: newType };
      configStore.setMapTile(mapId, newTile).then(() => emits("modifiedMap"));
    }
  }
};

const setMode = (mode: EditMode) => {
  if (selectedEdit.value == mode) {
    selectedEdit.value = EditMode.NONE;
    emits("mapClickEnabled", false);
  } else {
    selectedEdit.value = mode;
    emits("mapClickEnabled", true);
  }
};
</script>

<style scoped lang="scss">
.icon {
  &.is-double {
    width: 3em;

    svg + svg {
      margin-left: 0.2em;
    }
  }
}

.button {
  &.start {
    color: v-bind(startTileColor);
  }

  &.target {
    color: v-bind(targetTileColor);
  }

  &.fuel {
    color: v-bind(fuelTileColor);
  }
}
</style>
