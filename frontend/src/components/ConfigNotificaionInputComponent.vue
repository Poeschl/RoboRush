<template>
  <div class="box">
    <span class="label">Global notification text</span>
    <div class="field has-addons">
      <div class="control is-flex-grow-1">
        <textarea
          class="textarea has-fixed-size"
          placeholder="The text to display. Leave empty to hide notification box"
          v-model="text"
          :class="{ 'is-success': status.success, 'is-danger': status.failed }"
        ></textarea>
      </div>
      <div class="control">
        <button class="button" @click="clear" title="Clear the whole textbox. (Does not save the text)">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-eraser" />
          </div>
        </button>
      </div>
      <div class="control">
        <button class="button" @click="resetValue" title="Resets the text to the last saved state">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-arrow-rotate-left" />
          </div>
        </button>
      </div>
      <div class="control">
        <button class="button is-primary" @click="saveValue" title="Saves the text and sends it to the server">
          <div class="icon">
            <FontAwesomeIcon icon="fa-solid fa-check" />
          </div>
        </button>
      </div>
    </div>
    <p class="help has-text-grey-light">Newlines are ignored on display</p>
  </div>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed, onMounted, type Ref, ref, watch } from "vue";
import log from "loglevel";

const configStore = useConfigStore();
const text = ref<string>("");
const status = ref({ success: false, failed: false });

const storedText = computed<string>(() => configStore.clientSettings.globalNotificationText);

onMounted(() => {
  resetValue();
});

watch(
  () => storedText,
  (value, oldValue, onCleanup) => {
    setInternalValue(value);
  },
);

const saveValue = () => {
  configStore
    .setGlobalNotificationText(text.value)
    .then(() => {
      status.value.success = true;
      triggerResetStatus();
    })
    .catch((reason) => {
      log.warn("Could not save notification text. ", reason);
      status.value.failed = true;
      triggerResetStatus();
    });
};

const resetValue = () => {
  setInternalValue(storedText);
};

const setInternalValue = (newText: Ref<string>) => {
  if (newText.value != undefined) {
    text.value = newText.value;
  }
};

const triggerResetStatus = () => {
  setTimeout(() => (status.value = { success: false, failed: false }), 1000);
};

const clear = () => {
  text.value = "";
};
</script>

<style scoped>
.field {
  align-items: end;
}
</style>
