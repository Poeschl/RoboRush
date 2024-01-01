<template>
  <div class="box">
    <div class="is-size-3">
      {{ title }}
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>

<style scoped></style>
