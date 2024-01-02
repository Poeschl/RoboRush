import { defineStore } from "pinia";
import { ref } from "vue";
import SystemService from "@/services/SystemService";

const systemService = new SystemService();
const backendCheckIntervalMs: number = 5000;

export const useSystemStore = defineStore("systemStore", () => {
  const backendAvailable = ref<boolean>(true);
  const checkTimerId = ref<number>();

  function checkBackendAvailability() {
    systemService
      .getPing()
      .then((responseCode: number) => {
        backendAvailable.value = responseCode == 200;
      })
      .catch((reason) => {
        backendAvailable.value = false;
        console.error(`Could not retrieve backend ping interface (${reason})`);
      });
  }

  function startCyclicAvailabilityCheck() {
    checkTimerId.value = window.setInterval(checkBackendAvailability, backendCheckIntervalMs);
  }

  function stopCyclicAvailabilityCheck() {
    if (checkTimerId.value !== undefined) {
      window.clearInterval(checkTimerId.value);
    }
  }

  return { backendAvailable, checkBackendAvailability, startCyclicAvailabilityCheck, stopCyclicAvailabilityCheck };
});