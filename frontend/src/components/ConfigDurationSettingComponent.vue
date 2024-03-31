<template>
  <span class="label">{{ setting.key }}</span>
  <div class="field has-addons">
    <div class="control is-flex-grow-1">
      <input
        class="input"
        type="text"
        placeholder="ISO 8601 duration input"
        v-model="setting.value"
        :class="{ 'is-success': status.success, 'is-danger': status.failed }"
      />
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
  <p class="help has-text-grey-light">Enter only ISO 8601 duration strings</p>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import { type Setting, SettingType } from "@/models/Config";
import log from "loglevel";

const configStore = useConfigStore();
const setting = ref<Setting>({ key: "invalid", type: SettingType.DURATION, value: "-0" });
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
      log.warn("Could not save int setting. ", reason);
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

<style scoped></style>
