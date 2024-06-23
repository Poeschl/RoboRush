<template>
  <InfoBoxTemplate title="Game Flags">
    <div>
      <div class="level mb-1">
        <div class="level-left">
          <div class="level-item">Target position known</div>
        </div>
        <div class="level-right">
          <FontAwesomeIcon icon="fa-solid fa-square-check" v-if="currentGame.targetPosition !== undefined" />
          <FontAwesomeIcon icon="fa-regular fa-square" v-else />
        </div>
      </div>
      <div class="level mb-1">
        <div class="level-left">
          <div class="level-item">Solar charge possible</div>
        </div>
        <div class="level-right">
          <FontAwesomeIcon icon="fa-solid fa-square-check" v-if="currentGame.solarChargePossible" />
          <FontAwesomeIcon icon="fa-regular fa-square" v-else />
        </div>
      </div>
      <div class="level mb-1">
        <div class="level-left">
          <div class="level-item">Full map scan possible</div>
        </div>
        <div class="level-right">
          <FontAwesomeIcon icon="fa-solid fa-square-check" v-if="currentGame.fullMapScanPossible" />
          <FontAwesomeIcon icon="fa-regular fa-square" v-else />
        </div>
      </div>
    </div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import { computed, ref, watch } from "vue";
import { useGameStore } from "@/stores/GameStore";

const gameStore = useGameStore();

const currentGame = computed(() => gameStore.currentGame);
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
