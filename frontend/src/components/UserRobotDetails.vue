<template>
  <div class="box">
    <div class="is-size-3 mb-3">Robot Details</div>
    <div v-if="userStore.loggedIn">
      <div v-if="robot != undefined">
        <div class="level">
          <div class="level-left">
            <div class="level-item is-size-5">
              <FontAwesomeIcon icon="fa-solid fa-robot" class="mr-2" :style="{ color: robot.color.toHex() }" />
              Robot Id
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <div>{{ robot.id }}</div>
            </div>
          </div>
        </div>

        <div class="level">
          <div class="level-left">
            <div class="level-item is-size-5">
              <FontAwesomeIcon icon="fa-solid fa-gas-pump" class="mr-2" />
              Fuel
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <progress class="progress fuel is-primary" :value="robot.fuel" max="100" :title="'Fuel: ' + robot.fuel" />
            </div>
          </div>
        </div>

        <div class="level">
          <div class="level-left">
            <div class="level-item is-size-5">
              <FontAwesomeIcon icon="fa-solid fa-location-dot" class="mr-2" />
              Position
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <div title="(x, y)">({{ robot.position.x }}, {{ robot.position.y }})</div>
            </div>
          </div>
        </div>

        <div class="level" v-if="robot.lastResult">
          <div class="level-left">
            <div class="level-item is-size-5">
              <FontAwesomeIcon icon="fa-solid fa-clipboard" class="mr-2" />
              Last action result
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <div title="Result from the last executed action">{{ robot.lastResult }}</div>
            </div>
          </div>
        </div>

        <div class="level" v-if="robot.nextAction">
          <div class="level-left">
            <div class="level-item is-size-5">
              <FontAwesomeIcon icon="fa-solid fa-clipboard" class="mr-2" />
              Next action
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <div title="The action of your robot in the next round">{{ robot.nextAction }}</div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="has-text-centered">Your robot does not participate in the current game.</div>
    </div>
    <div v-else class="has-text-centered">Login to see detailed information about your robot.</div>
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from "@/stores/GameStore";
import { computed, ref } from "vue";
import type { ActiveRobot } from "@/models/Robot";
import { useUserStore } from "@/stores/UserStore";

const userStore = useUserStore();
const gameStore = useGameStore();
const robot = computed(() => gameStore.userRobot);
</script>

<style scoped lang="scss">
.progress.fuel {
  width: 12rem;
}
</style>
