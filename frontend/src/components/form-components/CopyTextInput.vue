<template>
  <div class="field has-addons">
    <div class="control">
      <input class="input" readonly :value="value" />
    </div>
    <div class="control">
      <a class="button" :class="{ 'is-success': copied }" @click="copyToClipboard">
        <FontAwesomeIcon icon="fa-regular fa-copy" />
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import log from "loglevel";

const copied = ref<boolean>(false);
const copiedIndicatorTimeout = 2000;

const props = defineProps<{
  value: any;
}>();

async function copyToClipboard() {
  const textToCopy = props.value;

  // Navigator clipboard api needs a secure context (https)
  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(textToCopy);
  } else {
    // Use the 'out of viewport hidden text area' trick
    const textArea = document.createElement("textarea");
    textArea.value = textToCopy;

    textArea.style.position = "absolute";
    textArea.style.left = "-999999px";
    document.body.prepend(textArea);
    textArea.select();

    try {
      document.execCommand("copy");
    } catch (error) {
      log.error(error);
    } finally {
      textArea.remove();
    }
  }

  copied.value = true;
  setTimeout(() => (copied.value = false), copiedIndicatorTimeout);
}
</script>

<style scoped></style>
