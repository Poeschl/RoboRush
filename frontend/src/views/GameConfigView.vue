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
    <div class="title">Notifications</div>
    <div class="subtitle">The given text will be displayed immediately on all open websites. It will be not persisted between backend restarts.</div>
    <ConfigNotificaionInputComponent />
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
import ConfigNotificaionInputComponent from "@/components/ConfigNotificaionInputComponent.vue";

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
