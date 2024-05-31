<template>
  <div class="box has-background-primary is-flex is-flex-direction-column">
    <a href="/" class="is-align-self-center">
      <img class="mr-2" src="/img/icon_white.png" alt="Logo with two R's and a checkered flag" />
    </a>
    <a class="is-size-4" href="https://chaos.social/tags/RoboRush" target="_blank">
      <div class="icon mr-5">
        <FontAwesomeIcon icon="fa-brands fa-mastodon" class="fa-xl" />
      </div>
      <div class="text">#RoboRush</div>
    </a>
    <a class="is-size-4" :href="currentUrl" target="_blank">
      <div class="icon mr-5">
        <FontAwesomeIcon icon="fa-solid fa-globe" class="fa-xl" />
      </div>
      <div class="text">{{ currentUrl }}</div>
    </a>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const currentUrl = computed<string>(() => window.location.origin);
</script>

<style scoped lang="scss">
.box {
  a {
    display: flex;
    justify-content: start;
    align-items: center;
    margin-top: 0.5rem;
    margin-bottom: 0.5rem;

    &:first-child {
      margin-top: 0;
    }
    &:last-child {
      margin-bottom: 0;
    }

    .icon {
      justify-content: start;
    }

    .icon:after {
      content: ":";
      margin-left: 0.2rem;
    }

    .text {
      word-break: break-word;
    }
  }
}

a {
  color: var(--bulma-body-text-color);
}
</style>
