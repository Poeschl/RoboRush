<template>
  <img class="grey-shadow" :src="imageSrc" :alt="title" :title="title" />
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  imageSrc: string;
}>();
</script>

<style scoped lang="scss"></style>
