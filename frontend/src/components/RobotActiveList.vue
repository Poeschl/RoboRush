<template>
  <InfoBoxTemplate title="Active robots">
    <Transition>
      <div class="tags">
        <RobotTag :robot="robot" v-for="robot in robots" />
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

const robots = computed<PublicRobot[]>(() => gameStore.robots.data);
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
