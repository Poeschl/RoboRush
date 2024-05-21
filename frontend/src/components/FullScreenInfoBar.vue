<template>
  <nav class="navbar is-primary is-radiusless pl-4 pr-4" role="navigation">
    <div class="navbar-brand">
      <a href="/" class="navbar-item">
        <img class="mr-2" src="/img/icon_white.png" alt="Logo with two R's and a checkered flag" />
        <span class="is-size-4">RoboRush</span>
      </a>
    </div>

    <div id="menuItems" class="navbar-menu">
      <div class="navbar-start"></div>
      <div class="navbar-end">
        <a class="navbar-item is-size-4" href="https://chaos.social/tags/RoboRush" target="_blank">
          <div class="icon mr-1">
            <FontAwesomeIcon icon="fa-brands fa-mastodon" class="fa-xl" />
          </div>
          : #RoboRush
        </a>
        <a class="navbar-item is-size-4" :href="currentUrl" target="_blank">
          <div class="icon mr-1">
            <FontAwesomeIcon icon="fa-solid fa-globe" class="fa-xl" />
          </div>
          : {{ currentUrl }}
        </a>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";

const currentUrl = computed<string>(() => window.location.origin);
</script>

<style scoped lang="scss"></style>
