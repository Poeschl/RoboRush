<template>
  <InfoBoxTemplate title="Score Board">
    <Transition>
      <div>
        <div class="level mb-1" v-for="(winner, index) of winners">
          <div class="level-left">
            <div class="level-item">
              <div class="mr-1">{{ index + 1 }}.</div>
              <RobotTag :robot="winner" />
            </div>
          </div>
          <div class="level-right">
            <div class="level-item"><span class="has-text-weight-bold mr-1">0</span> wins</div>
          </div>
        </div>
      </div>
    </Transition>
  </InfoBoxTemplate>
</template>

<script setup lang="ts">
import RobotTag from "@/components/RobotTag.vue";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
import { useGameStore } from "@/stores/GameStore";
import { computed } from "vue";
import type { PublicRobot } from "@/models/Robot";

const gameStore = useGameStore();

// TODO: Determine the score correctly
const winners = computed<PublicRobot[]>(() => {
  const winners = gameStore.robots.slice();
  winners.sort((a, b) => a.position.x - b.position.x);
  winners.slice(0, 10);
  return winners;
});
</script>

<style scoped lang="scss">
.v-enter-active,
.v-leave-active {
  transition: opacity 1000ms ease-out;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
