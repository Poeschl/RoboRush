<template>
  <div class="section pt-0">
    <div class="title">Settings</div>
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
    <div class="columns">
      <div class="column">
        <HeightMapList />
      </div>
      <div class="column is-one-third">
        <div class="box">
          <HeightMapUploadComponent />
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
import HeightMapUploadComponent from "@/components/HeightMapUploadComponent.vue";
import HeightMapList from "@/components/HeightMapList.vue";

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
