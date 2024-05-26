<template>
  <InfoBoxTemplate title="Score Board">
    <Transition>
      <div>
        <div class="level mb-1" v-for="(winner, index) in winners">
          <div class="level-left">
            <div class="level-item">
              <div v-if="index === 0" class="mr-1 position">🏆</div>
              <div v-else class="mr-1 position">{{ index + 1 }}.</div>
              <RobotTag :robot="winner" />
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <span class="has-text-weight-bold mr-1">{{ winner.score }}</span> win<span v-if="winner.score != 1">s</span>
            </div>
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
import type { ScoreboardEntry } from "@/models/Robot";

const gameStore = useGameStore();

const winners = computed<ScoreboardEntry[]>(() => {
  const winners = gameStore.scoreBoard.data;
  winners.sort((a, b) => b.score - a.score);
  winners.slice(0, 10);
  return winners;
});
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
