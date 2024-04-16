<template>
  <div class="section pt-0">
    <div class="title">Settings</div>
    <div class="subtitle">All settings are applied immediately after saving</div>
    <div class="columns is-multiline">
      <div class="column" v-for="setting in configStore.currentConfig.values()">
        <div class="box">
          <ConfigIntSettingComponent v-if="setting.type == SettingType.INT" :setting-key="setting.key" />
          <ConfigDurationSettingComponent v-if="setting.type == SettingType.DURATION" :setting-key="setting.key" />
          <ConfigBooleanSettingComponent v-if="setting.type == SettingType.BOOLEAN" :setting-key="setting.key" />
        </div>
      </div>
    </div>
  </div>
  <div class="section">
    <div class="title">Map Editor</div>
    <div class="subtitle">
      During the preparing phase of every game one of the active maps is chosen randomly and the fuel value becomes the robots max fuel
    </div>
    <div class="columns">
      <div class="column">
        <MapList />
      </div>
      <div class="column is-one-third">
        <div class="box">
          <MapUploadComponent />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { onMounted } from "vue";
import ConfigIntSettingComponent from "@/components/ConfigIntSettingComponent.vue";
import { SettingType } from "@/models/Config";
import ConfigDurationSettingComponent from "@/components/ConfigDurationSettingComponent.vue";
import ConfigBooleanSettingComponent from "@/components/ConfigBooleanSettingComponent.vue";
import MapUploadComponent from "@/components/MapUploadComponent.vue";
import MapList from "@/components/MapList.vue";

const configStore = useConfigStore();

onMounted(() => {
  configStore.updateConfig();
});
</script>

<style scoped lang="scss">
.section {
  padding: 2rem;
}
</style>
