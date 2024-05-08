<template>
  <InfoBoxTemplate title="Score Board">
    <Transition>
      <div>
        <div class="level mb-1" v-for="index in numberOfRobots">
          <div class="level-left">
            <div class="level-item">
              <div v-if="index === 1" class="mr-1 position">🏆</div>
              <div v-else class="mr-1 position">{{ index }}.</div>
              <RobotTag :robot="winners[index - 1]" />
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
import type { ActiveRobot, PublicRobot } from "@/models/Robot";

const numberOfRobots = 10;

const gameStore = useGameStore();

// TODO: Determine the score correctly
const winners = computed<PublicRobot[]>(() => {
  const winners = gameStore.robots.slice();
  winners.sort((a, b) => a.position.x - b.position.x);
  winners.slice(0, 10);
  return winners;
});

const getRobotOrUndefined = (robot: ActiveRobot) => {};
</script>

<style scoped lang="scss">
.position {
  width: 1.5rem;
  text-align: end;
}

.v-enter-active,
.v-leave-active {
  transition: opacity 500ms ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
