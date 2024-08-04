<template>
  <div class="section pt-0">
    <div class="title">Current maps</div>
    <div class="subtitle">During the preparing phase of every game one of the active maps is chosen randomly</div>
    <MapList />
  </div>
  <div class="section">
    <div class="title">Upload new</div>
    <div class="subtitle">Upload a greyscale hightmap as new map or use a exported one.</div>
    <div class="box">
      <MapUploadComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { onMounted } from "vue";
import MapUploadComponent from "@/components/MapUploadComponent.vue";
import MapList from "@/components/MapList.vue";

const configStore = useConfigStore();

onMounted(() => {
  configStore.updateMaps();
});
</script>

<style scoped lang="scss">
.section {
  padding: 2rem;
}
</style>
