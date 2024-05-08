<template>
  <InfoBoxTemplate title="Your robot">
    <div v-if="robot != undefined">
      <div class="level">
        <div class="level-left">
          <div class="level-item is-size-5">
            <FontAwesomeIcon icon="fa-solid fa-robot" class="mr-2" :style="{ color: robot.color.toHex() }" />
            Robot Id
          </div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <div>{{ robot.id }}</div>
          </div>
        </div>
      </div>

      <div class="level">
        <div class="level-left">
          <div class="level-item is-size-5">
            <FontAwesomeIcon icon="fa-solid fa-gas-pump" class="mr-2" />
            Fuel
          </div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <progress class="progress fuel is-primary" :value="robot.fuel" :max="robot.maxFuel" :title="robot.fuel + ' / ' + robot.maxFuel" />
          </div>
        </div>
      </div>

      <div class="level">
        <div class="level-left">
          <div class="level-item is-size-5">
            <FontAwesomeIcon icon="fa-solid fa-location-dot" class="mr-2" />
            Position
          </div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <div title="(x, y)">({{ robot.position.x }}, {{ robot.position.y }})</div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="has-text-centered">Your robot does not participate in the current game.</div>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import { useGameStore } from "@/stores/GameStore";
import { computed, ref } from "vue";
import type { ActiveRobot, Move, Scan } from "@/models/Robot";
import { useUserStore } from "@/stores/UserStore";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";

const userStore = useUserStore();
const gameStore = useGameStore();
const robot = computed(() => gameStore.userRobot);
</script>

<style scoped lang="scss">
.progress.fuel {
  width: 12rem;
}
</style>
