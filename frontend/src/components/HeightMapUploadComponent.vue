<template>
  <div class="is-size-4 mb-3">Upload heightmap</div>
  <div class="field has-addons">
    <div class="control is-flex-grow-1">
      <input
        class="input"
        type="file"
        placeholder="Select your heightmap (*.png)"
        accept="image/png"
        :class="{ 'is-success': status.success, 'is-danger': status.failed }"
        @change="(event: Event) => (fileToUpload = (event.target as HTMLInputElement)?.files?.item(0))"
      />
    </div>
    <div class="control">
      <button class="button is-primary" @click="uploadHeightMap" :disabled="fileToUpload == null" :class="{ 'is-loading': status.processing }">
        <div class="icon">
          <FontAwesomeIcon icon="fa-solid fa-upload" />
        </div>
      </button>
    </div>
  </div>
  <a class="button is-text is-fullwidth" target="_blank" href="https://github.com/Poeschl/PathSeeker/tree/main/maps/README.md">How to create a heightmap?</a>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { ref } from "vue";
import log from "loglevel";

const configStore = useConfigStore();

const fileToUpload = ref<File | null>();
const status = ref({ success: false, failed: false, processing: false });

const uploadHeightMap = () => {
  if (fileToUpload.value != null) {
    status.value.processing = true;
    configStore
      .uploadNewHeightmap(fileToUpload.value)
      .then(() => {
        status.value.success = true;
        triggerResetStatus();
      })
      .catch((reason) => {
        log.warn("Could not save int setting. ", reason);
        status.value.failed = true;
        triggerResetStatus();
      })
      .finally(() => (status.value.processing = false));
  }
};

const triggerResetStatus = () => {
  setTimeout(() => {
    status.value.success = false;
    status.value.failed = false;
  }, 1000);
};
</script>

<style scoped lang="scss"></style>
