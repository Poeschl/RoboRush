<template>
  <span
    v-if="props.robot !== undefined"
    class="tag"
    :class="{ 'has-text-white': inverseColor, 'has-text-black': !inverseColor }"
    :style="{ 'background-color': tagColor }"
  >
    {{ text }}
  </span>
</template>

<script setup lang="ts">
import type { ActiveRobot, PublicRobot, ScoreboardEntry } from "@/models/Robot";
import { computed } from "vue";

const props = defineProps<{
  robot: PublicRobot | ActiveRobot | ScoreboardEntry | undefined;
}>();

const tagColor = computed<string | undefined>(() => props.robot?.color.toHex());
const text = computed<string | undefined>(() => props.robot?.name);
const inverseColor = computed<boolean>(() => {
  const lightLevel = props.robot?.color.getLightLevel();
  if (lightLevel !== undefined) {
    // Find a better light level detection, future me!
    return lightLevel < (255 * 3) / 2;
  } else {
    return true;
  }
});
</script>

<style scoped lang="scss">
@use "bulma/sass/utilities/initial-variables";

.tag {
  font-weight: bold;
  cursor: default;
}
</style>
