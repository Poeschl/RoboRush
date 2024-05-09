<template>
  <nav class="navbar is-primary is-radiusless" role="navigation">
    <div class="container">
      <div class="navbar-brand">
        <a href="/" class="navbar-item">
          <img class="mr-2" src="/img/icon_white.png" alt="Logo with two R's and a checkered flag" />
          <span class="is-size-5">RoboRush</span>
        </a>
        <a role="button" class="navbar-burger" data-target="menuItems" :class="{ 'is-active': mobileNavOpen }" @click="toggleMobileNav">
          <span aria-hidden="true" />
          <span aria-hidden="true" />
          <span aria-hidden="true" />
        </a>
      </div>

      <div id="menuItems" class="navbar-menu" :class="{ 'is-active': mobileNavOpen }">
        <div class="navbar-start">
          <router-link class="navbar-item" to="/">
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-chalkboard" />
            </div>
            Overview
          </router-link>
          <router-link v-if="userLoggedIn && !userIsAdmin" class="navbar-item" to="/myRobot">
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-robot" />
            </div>
            My Robot
          </router-link>
          <router-link class="navbar-item" to="/how-to-play">
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-book" />
            </div>
            How To Play
          </router-link>
          <a v-if="!userIsAdmin" class="navbar-item" href="/api/swagger-ui" target="_blank">
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-code" />
            </div>
            API docs
          </a>
          <router-link v-if="userIsAdmin" class="navbar-item" to="/config">
            <div class="icon mr-1">
              <FontAwesomeIcon icon="fa-solid fa-user-tie" />
            </div>
            Game Configuration
          </router-link>
        </div>
        <div class="navbar-end">
          <NavbarUserComponent />
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import NavbarUserComponent from "@/components/NavbarUserComponent.vue";
import { useUserStore } from "@/stores/UserStore";

const userStore = useUserStore();

const mobileNavOpen = ref<boolean>(false);
const userIsAdmin = computed<boolean>(() => userStore.isAdmin);
const userLoggedIn = computed<boolean>(() => userStore.loggedIn);

const toggleMobileNav = () => {
  mobileNavOpen.value = !mobileNavOpen.value;
};
</script>

<style scoped lang="scss"></style>
