<template>
  <span class="label">{{ setting.key }}</span>
  <div class="field has-addons">
    <div class="control is-flex-grow-1">
      <div class="select is-fullwidth" :class="{ 'is-success': status.success, 'is-danger': status.failed }">
        <select v-model="setting.value">
          <option>true</option>
          <option>false</option>
        </select>
      </div>
    </div>
    <div class="control">
      <button class="button" @click="resetValue">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-arrow-rotate-left" />
        </div>
      </button>
    </div>
    <div class="control">
      <button class="button is-primary" @click="saveValue">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-check" />
        </div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import { type Setting, SettingType } from "@/models/Config";
import log from "loglevel";

const configStore = useConfigStore();
const setting = ref<Setting>({ key: "invalid", type: SettingType.BOOLEAN, value: "-0" });
const status = ref({ success: false, failed: false });

const storedSetting = computed<Setting | undefined>(() => configStore.currentConfig.get(props.settingKey));

const props = defineProps<{
  settingKey: string;
}>();

onMounted(() => {
  resetValue();
});

watch(
  () => storedSetting,
  (value, oldValue, onCleanup) => {
    setInternalValue(value);
  },
);

const saveValue = () => {
  configStore
    .save({ key: setting.value.key, value: setting.value.value })
    .then(() => {
      status.value.success = true;
      triggerResetStatus();
    })
    .catch((reason) => {
      log.warn("Could not save boolean setting. ", reason);
      status.value.failed = true;
      triggerResetStatus();
    });
};

const resetValue = () => {
  setInternalValue(storedSetting);
};

const setInternalValue = (newSetting: Ref<Setting | undefined>) => {
  if (newSetting.value != undefined) {
    setting.value = { ...newSetting.value };
  }
};

const triggerResetStatus = () => {
  setTimeout(() => (status.value = { success: false, failed: false }), 1000);
};
</script>

<style scoped lang="scss">
//Fix until the style is supported
.select.is-success > select {
  --bulma-input-h: var(--bulma-success-h);
  --bulma-input-s: var(--bulma-success-s);
  --bulma-input-l: var(--bulma-success-l);
  --bulma-input-focus-h: var(--bulma-success-h);
  --bulma-input-focus-s: var(--bulma-success-s);
  --bulma-input-focus-l: var(--bulma-success-l);
  --bulma-input-border-l: var(--bulma-success-l);
}

//Fix until the style is supported
.select.is-danger > select {
  --bulma-input-h: var(--bulma-danger-h);
  --bulma-input-s: var(--bulma-danger-s);
  --bulma-input-l: var(--bulma-danger-l);
  --bulma-input-focus-h: var(--bulma-danger-h);
  --bulma-input-focus-s: var(--bulma-danger-s);
  --bulma-input-focus-l: var(--bulma-danger-l);
  --bulma-input-border-l: var(--bulma-danger-l);
}
</style>
