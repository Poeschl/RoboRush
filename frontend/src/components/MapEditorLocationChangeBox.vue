<template>
  <InfoBoxTemplate title="Edit locations">
    <div class="field is-grouped is-justify-content-center">
      <p class="control">
        <button class="button" title="Clear tile" :disabled="saving" :class="{ 'is-active': selectedEdit == EditMode.CLEAR }" @click="setMode(EditMode.CLEAR)">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-ban" />
          </div>
        </button>
      </p>
      <p class="control">
        <button
          class="button start"
          title="Start tile"
          :disabled="saving"
          :class="{ 'is-active': selectedEdit == EditMode.START }"
          @click="setMode(EditMode.START)"
        >
          <div class="icon is-double">
            <FontAwesomeIcon icon="fa-solid fa-flag-checkered" />
            <FontAwesomeIcon icon="fa-solid fa-arrow-right" />
          </div>
        </button>
      </p>
      <p class="control">
        <button
          class="button target"
          title="Target tile"
          :disabled="saving"
          :class="{ 'is-active': selectedEdit == EditMode.TARGET }"
          @click="setMode(EditMode.TARGET)"
        >
          <div class="icon is-double">
            <FontAwesomeIcon icon="fa-solid fa-arrow-right" />
            <FontAwesomeIcon icon="fa-solid fa-flag-checkered" />
          </div>
        </button>
      </p>
      <p class="control">
        <button
          class="button fuel"
          title="Fuel station tile"
          :disabled="saving"
          :class="{ 'is-active': selectedEdit == EditMode.FUEL }"
          @click="setMode(EditMode.FUEL)"
        >
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-gas-pump" />
          </div>
        </button>
      </p>
    </div>
    <div class="field has-addons is-justify-content-flex-end">
      <div class="control">
        <button class="button" :disabled="saving || emptyChanges" @click="undo" title="Undo last change">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-arrow-rotate-left" />
          </div>
        </button>
      </div>
      <div class="control">
        <button class="button" :disabled="saving || emptyChanges" @click="reset" title="Reset all changes">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-arrows-rotate" />
          </div>
        </button>
      </div>
      <div class="control">
        <button class="button is-primary" :disabled="emptyChanges" :class="{ 'is-loading': saving }" @click="save">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-check" />
          </div>
        </button>
      </div>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import type { PlaygroundMap, PlaygroundMapAttributes, Position, Tile } from "@/models/Map";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import { useConfigStore } from "@/stores/ConfigStore";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import useMapConstants from "@/config/map";

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

const saving = ref<boolean>(false);
const selectedEdit = ref<EditMode>(EditMode.NONE);
const changedTiles = ref<{ data: Tile[] }>({ data: [] });
const emptyChanges = computed<boolean>(() => changedTiles.value.data.length < 1);

const props = defineProps<{
  map: PlaygroundMap;
  clickedPosition: Position | undefined;
}>();

const emits = defineEmits<{
  (e: "modifiedMap"): void;
  (e: "mapClickEnabled", value: boolean): void;
  (e: "resetMap"): void;
}>();

onMounted(() => {
  reset();
});

watch(
  () => props.clickedPosition,
  () => {
    if (props.clickedPosition != undefined) {
      applyChange(props.clickedPosition);
    }
  },
);

const applyChange = (position: Position) => {
  const originalTile = props.map.mapData.find((it) => it.position.x === position.x && it.position.y === position.y)!;
  //TODO: Make the change on the viewed map
  //TODO: Store the change in the changes list
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

const save = () => {
  saving.value = true;
  selectedEdit.value = EditMode.NONE;
  setTimeout(() => {
    saving.value = false;
    emits("modifiedMap");
  }, 2000);
};

const undo = () => {
  changedTiles.value.data.pop();
};

const reset = () => {
  emits("resetMap");
  changedTiles.value.data = [];
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
