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
          <NavbarUserComponent />
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from "vue";
import NavbarUserComponent from "@/components/NavbarUserComponent.vue";

const mobileNavOpen = ref<boolean>(false);
const toggleMobileNav = () => {
  mobileNavOpen.value = !mobileNavOpen.value;
};
</script>

<style scoped lang="scss"></style>
