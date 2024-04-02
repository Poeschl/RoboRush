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

  <div class="notification is-warning" v-if="genResult.warnings.length > 0">
    <button class="delete" @click="genResult.warnings = []"></button>
    <div v-for="text in genResult.warnings">{{ text }}</div>
  </div>
  <div class="notification is-danger" v-if="genResult.errors.length > 0">
    <button class="delete" @click="genResult.errors = []"></button>
    <div v-for="text in genResult.errors">{{ text }}</div>
  </div>

  <a class="button is-text is-fullwidth" target="_blank" href="https://github.com/Poeschl/PathSeeker/tree/main/maps/README.md">How to create a heightmap?</a>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { ref } from "vue";
import log from "loglevel";
import type { MapGenerationResult } from "@/models/Config";
import type { AxiosError } from "axios";

const configStore = useConfigStore();

const fileToUpload = ref<File | null>();
const status = ref({ success: false, failed: false, processing: false });
const genResult = ref<{ warnings: string[]; errors: string[] }>({ warnings: [], errors: [] });

const uploadHeightMap = () => {
  if (fileToUpload.value != null) {
    status.value.processing = true;
    genResult.value = { warnings: [], errors: [] };
    configStore
      .uploadNewHeightmap(fileToUpload.value)
      .then((result: MapGenerationResult) => {
        genResult.value.warnings = result.warnings;
        status.value.success = true;
        triggerResetStatus();
      })
      .catch((reason: AxiosError<{ message: string }>) => {
        log.warn("Could not upload height map. ", reason);
        genResult.value.errors[0] = reason.response?.data?.message || "";
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
