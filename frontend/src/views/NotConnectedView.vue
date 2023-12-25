<template>
  <div class="notification is-info">
    <div class="is-size-1">Can't reach the backend server.</div>
    <div class="is-size-4">Please wait until the server is fully started. This page will refresh when the backend can be reached.</div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { onMounted, watch } from "vue";
import { useSystemStore } from "@/stores/SystemStore";

const router = useRouter();
const systemStore = useSystemStore();

onMounted(() => {
  systemStore.startCyclicAvailabilityCheck();
});

watch(
  () => systemStore.backendAvailable,
  (value, oldValue, onCleanup) => {
    if (value) {
      systemStore.stopCyclicAvailabilityCheck();
      // Refresh router view to force components reload
      router.go(0);
    }
  },
);
</script>

<style scoped></style>
