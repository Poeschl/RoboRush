<template>
  <img :src="imageSrc" :alt="title" :title="title" />
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  imageSrc: string;
}>();
</script>

<style scoped lang="scss">
@use "bulma/sass/utilities/initial-variables";

img {
  box-shadow: initial-variables.$grey 0 0 4px 1px;
}
</style>
