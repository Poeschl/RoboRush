<template>
  <InfoBoxTemplate title="Map attributes">
    <div class="field">
      <span class="label">Map name</span>
      <div class="control">
        <input class="input" type="text" placeholder="The name of this map" v-model="internalAttributes.mapName" />
      </div>
    </div>
    <div class="field">
      <span class="label">Available fuel per robot</span>
      <div class="control">
        <input
          class="input"
          type="number"
          step="1"
          placeholder="Max robot fuel on this map"
          min="1"
          v-model="internalAttributes.maxRobotFuel"
          :class="{ 'is-danger': internalAttributes.maxRobotFuel < 10 }"
        />
      </div>
    </div>
    <div class="field">
      <span class="label">Amount of fuel for one solar recharge turn. (Percentage of a robots max fuel)</span>
      <div class="control">
        <input
          class="input"
          type="number"
          step="0.01"
          placeholder="How much should one turn solar loading gain."
          min="0"
          max="1"
          v-model="internalAttributes.solarChargeRate"
          :class="{ 'is-danger': internalAttributes.solarChargeRate < 0 || internalAttributes.solarChargeRate > 1 }"
        />
      </div>
      <span class="help is-dark is-size-6">~ {{ solarChargeInFuel }} fuel</span>
    </div>

    <div class="field has-addons is-justify-content-flex-end">
      <div class="control">
        <button class="button" :disabled="saving" @click="reset">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-arrow-rotate-left" />
          </div>
        </button>
      </div>
      <div class="control">
        <button class="button is-primary" :class="{ 'is-loading': saving }" @click="save">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-check" />
          </div>
        </button>
      </div>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import type { PlaygroundMap, PlaygroundMapAttributes } from "@/models/Map";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import { useConfigStore } from "@/stores/ConfigStore";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";

const configStore = useConfigStore();

const props = defineProps<{
  map: PlaygroundMap;
}>();

const emits = defineEmits<{
  (e: "modifiedMap"): void;
}>();

const mapAttributes = computed<PlaygroundMapAttributes>(() => {
  return {
    id: props.map.id,
    mapName: props.map.mapName,
    maxRobotFuel: props.map.maxRobotFuel,
    solarChargeRate: props.map.solarChargeRate,
  };
});

const internalAttributes = ref<PlaygroundMapAttributes>({
  id: 0,
  mapName: "",
  maxRobotFuel: 0,
  solarChargeRate: 0,
});

onMounted(() => {
  reset();
});

watch(
  () => mapAttributes,
  (value) => {
    setInternalValue(value);
  },
);

const saving = ref<boolean>(false);
const solarChargeInFuel = computed(() => Math.ceil(internalAttributes.value.maxRobotFuel * internalAttributes.value.solarChargeRate));

const save = () => {
  saving.value = true;
  configStore.setMapAttributes(internalAttributes.value).then(() => {
    saving.value = false;
    emits("modifiedMap");
  });
};

const reset = () => {
  setInternalValue(mapAttributes);
};

const setInternalValue = (newSetting: Ref<PlaygroundMapAttributes | undefined>) => {
  if (newSetting.value != undefined) {
    internalAttributes.value = { ...newSetting.value };
  }
};
</script>

<style scoped lang="scss"></style>
