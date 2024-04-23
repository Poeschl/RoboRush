<template>
  <BaseModal>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-edit" />
        Edit map - {{ map.mapName }}
      </h4>
    </template>

    <template #content>
      <div class="field">
        <span class="label">Map name</span>
        <div class="control">
          <input class="input" type="text" placeholder="The name of this map" v-model="mapAttributes.mapName" />
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
            v-model="mapAttributes.maxRobotFuel"
            :class="{ 'is-danger': mapAttributes.maxRobotFuel < 10 }"
          />
        </div>
      </div>
      <div class="field">
        <span class="label">Is charging over solar power possible</span>
        <div class="control">
          <div class="select is-fullwidth">
            <select v-model="mapAttributes.solarChargeEnabled">
              <option>true</option>
              <option>false</option>
            </select>
          </div>
        </div>
      </div>
    </template>
    <template #footer>
      <button class="button is-primary mr-2" @click="save" :class="{ 'is-loading': saving }">Save</button>
      <button class="button" @click="$emit('close')" :disabled="saving">Cancel</button>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from "@/components/templates/BaseModal.vue";
import type { PlaygroundMap, PlaygroundMapAttributes } from "@/models/Map";
import { ref } from "vue";
import { useConfigStore } from "@/stores/ConfigStore";

const props = defineProps<{
  map: PlaygroundMap;
}>();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const mapAttributes = ref<PlaygroundMapAttributes>({
  id: props.map.id,
  mapName: props.map.mapName,
  maxRobotFuel: props.map.maxRobotFuel,
  solarChargeEnabled: props.map.solarChargeEnabled,
});
const saving = ref<boolean>(false);

const save = () => {
  saving.value = true;
  useConfigStore()
    .setMapAttributes(mapAttributes.value)
    .then(() => {
      saving.value = false;
      emit("close");
    });
};
</script>

<style scoped lang="scss"></style>
