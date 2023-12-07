<template>
  <nav class="navbar is-primary is-radiusless" role="navigation">
    <div class="container">
      <div class="navbar-brand">
        <a href="/" class="navbar-item">
          <img class="mr-3" src="/img/icon_white.png" alt="Logo with the letter P and S" />
          <span class="is-uppercase">PathSeeker</span>
        </a>
        <a role="button" class="navbar-burger" data-target="menuItems" :class="{ 'is-active': mobileNavOpen }" @click="toggleMobileNav">
          <span aria-hidden="true" />
          <span aria-hidden="true" />
          <span aria-hidden="true" />
        </a>
      </div>

      <div id="menuItems" class="navbar-menu" :class="{ 'is-active': mobileNavOpen }">
        <div class="navbar-start">
          <router-link class="navbar-item" to="/">Home</router-link>
          <router-link class="navbar-item" to="/howtoplay">How To Play</router-link>
          <router-link class="navbar-item" to="/rules">Rules</router-link>
        </div>
        <div class="navbar-end">
          <div class="navbar-item">
            <button class="button">
              <span class="has-text-weight-semibold">Create a account</span>
            </button>
          </div>
          <div class="navbar-item">
            <button class="button is-primary">
              <span>Login</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { ref } from "vue";

const mobileNavOpen = ref<boolean>(false);
const toggleMobileNav = () => {
  mobileNavOpen.value = !mobileNavOpen.value;
};
</script>

<style scoped lang="scss">
.navbar-end .navbar-item {
  padding-right: 0.25rem;
  padding-left: 0.25rem;
}
</style>
