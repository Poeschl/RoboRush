<template>
  <InfoBoxTemplate title="Game State">
    <div class="is-flex">
      <div class="mr-2">{{ currentGame.currentState }}</div>
      <Transition>
        <div class="icon" v-if="showStateChanged">
          <FontAwesomeIcon ref="stateIcon" icon="fa-solid fa-rotate" class="mr-2 state-change-animation fa-spin" />
        </div>
      </Transition>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import { computed, ref, watch } from "vue";
import { useGameStore } from "@/stores/GameStore";

const gameStore = useGameStore();

const showStateChanged = ref<boolean>(false);
const lastStateChangedTimer = ref<number | null>(null);

const currentGame = computed(() => gameStore.currentGame);

watch(
  () => currentGame.value.currentState,
  () => {
    // Show state change indicator
    showStateChanged.value = true;
    if (lastStateChangedTimer.value) {
      clearTimeout(lastStateChangedTimer.value);
    }
    lastStateChangedTimer.value = window.setTimeout(() => {
      showStateChanged.value = false;
      lastStateChangedTimer.value = null;
    }, 400);
  },
);
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
