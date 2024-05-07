<template>
  <span class="tag" :class="{ 'has-text-white': inverseColor, 'has-text-black': !inverseColor }" :style="{ 'background-color': tagColor }">{{ text }}</span>
</template>

<script setup lang="ts">
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import { computed } from "vue";

const props = defineProps<{
  robot: PublicRobot | ActiveRobot;
}>();

const tagColor = computed<string>(() => props.robot.color.toHex());
const text = computed<string>(() => props.robot.id.toString());
const inverseColor = computed<boolean>(() => {
  const lightLevel = props.robot.color.getLightLevel();
  // Find a better light level detection, future me!
  return lightLevel > 300;
});
</script>

<style scoped lang="scss">
@use "bulma/sass/utilities/initial-variables";

.tag {
  font-weight: bold;
}
</style>
