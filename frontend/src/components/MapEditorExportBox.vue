<template>
  <InfoBoxTemplate title="Export Map">
    <div class="control">
      <button class="button is-primary is-fullwidth" :class="{ 'is-loading': exporting }" @click.prevent="exportMap">
        <div class="icon mr-1">
          <FontAwesomeIcon icon="fa-solid fa-file-export" />
        </div>
        Export
      </button>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import type { PlaygroundMap, PlaygroundMapAttributes } from "@/models/Map";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import { useConfigStore } from "@/stores/ConfigStore";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import log from "loglevel";

const configStore = useConfigStore();

const exporting = ref<boolean>(false);

const props = defineProps<{
  map: PlaygroundMap;
}>();

const exportMap = () => {
  exporting.value = true;
  configStore
    .exportMap(props.map)
    .then((link) => {
      link.click();
    })
    .finally(() => {
      exporting.value = false;
    });
};
</script>

<style scoped lang="scss"></style>
