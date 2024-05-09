<template xmlns="http://www.w3.org/1999/html">
  <div data-theme="dark" class="mb-5">
    <NavBar class="mb-5" />
    <main class="container">
      <router-view />
      <div class="is-flex is-justify-content-center" v-if="!route.meta.hideFooter">
        <div class="is-flex is-justify-content-center pt-1 tool-links">
          <a class="button is-text" href="https://poeschl.xyz" target="_blank" title="Website">
            <div class="icon">
              <FontAwesomeIcon icon="fa-solid fa-globe" class="fa-xl" />
            </div>
          </a>
          <a class="button is-text" href="https://chaos.social/@Mr_Poeschl" target="_blank" title="Mastodon Contact">
            <div class="icon">
              <FontAwesomeIcon icon="fa-brands fa-mastodon" class="fa-xl" />
            </div>
          </a>
          <a class="button is-text" href="https://github.com/Poeschl/PathSeeker" target="_blank" title="Source">
            <div class="icon">
              <FontAwesomeIcon icon="fa-brands fa-github" class="fa-xl" />
            </div>
          </a>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import NavBar from "@/components/NavBar.vue";
import { useGameStore } from "@/stores/GameStore";
import { computed, watch } from "vue";
import { useUserStore } from "@/stores/UserStore";
import { useSystemStore } from "@/stores/SystemStore";
import { useRoute, useRouter } from "vue-router";
import log from "loglevel";

log.info(`Swagger UI: ${window.location.origin}/api/swagger-ui`);

const router = useRouter();
const route = useRoute();

const userStore = useUserStore();
const systemStore = useSystemStore();
const gameStore = useGameStore();

//Start the websocket
gameStore.updateGameInfo();
gameStore.updateMap();
gameStore.updateRobots();
gameStore.initWebsocket(computed(() => userStore.user));

if (userStore.loggedIn) {
  gameStore.retrieveUserRobotState();
}
watch(
  () => userStore.loggedIn,
  (current, previous, onCleanup) => {
    if (current) {
      gameStore.retrieveUserRobotState();
    }
  },
);

watch(
  () => systemStore.backendAvailable,
  (value, oldValue, onCleanup) => {
    if (!value) {
      router.push({ path: "/not-connected" });
    }
  },
);
</script>

<style lang="scss">
@use "bulma/sass/utilities/initial-variables";

.tool-links {
  width: 25%;
  border-top-style: solid;
  border-top-color: initial-variables.$black-ter;
}
</style>
