<template>
  <div class="box">
    <div class="subtitle is-4 has-text-white is-capitalized">{{ props.title }}</div>

    <slot name="default" />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ title: string }>();
</script>

<style scoped lang="scss"></style>
