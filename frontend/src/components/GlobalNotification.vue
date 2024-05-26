<template>
  <Transition>
    <div class="box has-text-centered has-background-info is-color-black p-3 is-size-4 has-text-weight-semibold" v-if="shown">
      {{ text }}
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/stores/ConfigStore";
import { computed } from "vue";

const configStore = useConfigStore();

const text = computed<string>(() => configStore.clientSettings.globalNotificationText);
const shown = computed<boolean>(() => configStore.clientSettings.globalNotificationText.length > 0);
</script>

<style scoped lang="scss">
.v-enter-active,
.v-leave-active {
  transition: opacity 200ms ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
