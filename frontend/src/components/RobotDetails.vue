<template>
  <InfoBoxTemplate title="Your robot">
    <div v-if="robot != undefined">
      <div class="level mb-3">
        <div class="level-left">
          <div class="level-item">Name</div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <RobotTag :robot="robot" />
          </div>
        </div>
      </div>

      <div class="level mb-3">
        <div class="level-left">
          <div class="level-item">Fuel</div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <progress class="progress fuel is-primary" :value="robot.fuel" :max="robot.maxFuel" :title="robot.fuel + ' / ' + robot.maxFuel" />
          </div>
        </div>
      </div>

      <div class="level mb-3">
        <div class="level-left">
          <div class="level-item">Position</div>
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
import { computed } from "vue";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import RobotTag from "@/components/RobotTag.vue";

const gameStore = useGameStore();
const robot = computed(() => gameStore.userRobot);
</script>

<style scoped lang="scss">
.progress.fuel {
  width: 12rem;
}
</style>
