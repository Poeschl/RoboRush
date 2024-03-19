<template>
  <div class="box">
    <div class="is-size-3 mb-3">Robot Control</div>
    <div v-if="userStore.loggedIn">
      <div v-if="robot != undefined">
        <div class="columns">
          <div class="column is-one-third">
            <div class="is-dpad-control">
              <div class="columns">
                <div class="column is-offset-one-third is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightUp }" @click="move('NORTH')">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-caret-up" class="fa-2x" />
                    </div>
                  </button>
                </div>
              </div>
              <div class="columns">
                <div class="column is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightLeft }" @click="move('EAST')">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-caret-left" class="fa-2x" />
                    </div>
                  </button>
                </div>
                <div class="column is-offset-one-third is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightRight }" @click="move('WEST')">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-caret-right" class="fa-2x" />
                    </div>
                  </button>
                </div>
              </div>
              <div class="columns">
                <div class="column is-offset-one-third is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightDown }" @click="move('SOUTH')">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-caret-down" class="fa-2x" />
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="column">
            <div class="field has-addons is-scan-action">
              <div class="control is-flex-grow-1">
                <input class="input" type="number" min="0" :disabled="!controlsEnabled" placeholder="Scan distance" />
              </div>
              <div class="control">
                <button class="button" title="Scan the give distance" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightScan }">
                  <div class="icon">
                    <FontAwesomeIcon icon="fa-solid fa-satellite-dish" class="fa-xl" />
                  </div>
                </button>
              </div>
            </div>
            <div class="mb-3">
              <p class="mb-1">Set action:</p>
              <p v-if="robot.nextAction?.type == 'move'">Move {{ (robot.nextAction as Move).direction }}</p>
              <p v-if="robot.nextAction?.type == 'scan'">Scan with distance {{ (robot.nextAction as Scan).distance }}</p>
            </div>
            <div>
              <p class="mb-1">Last result:</p>
              <p>{{ robot.lastResult }}</p>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="has-text-centered">Your robot does not participate in the current game.</div>
    </div>
    <div v-else class="has-text-centered">Login to control your robot.</div>
  </div>
  <Toast v-if="toast.shown" :type="toast.type" :message="toast.message" @close="() => (toast.shown = false)" />
</template>

<script setup lang="ts">
import { useGameStore } from "@/stores/GameStore";
import { useUserStore } from "@/stores/UserStore";
import { computed, ref } from "vue";
import { GameState } from "@/models/Game";
import type { Action, Move, Scan } from "@/models/Robot";
import Toast from "@/components/Toast.vue";
import { ToastType } from "@/models/ToastType";

const userStore = useUserStore();
const gameStore = useGameStore();

const toast = ref<{ shown: boolean; type: ToastType; message: string }>({ shown: false, type: ToastType.INFO, message: "" });

const robot = computed(() => gameStore.userRobot);
const controlsEnabled = computed<boolean>(() => gameStore.currentGame.currentState == GameState.WAIT_FOR_ACTION);

const highlightUp = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "NORTH"));
const highlightRight = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "EAST"));
const highlightDown = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "SOUTH"));
const highlightLeft = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "WEST"));
const highlightScan = computed<boolean>(() => isScan(robot.value?.nextAction));

const isMovementInDirection = (action: Action | undefined, direction: string) => {
  return action?.type == "move" && (action as Move).direction == direction;
};

const isScan = (action: Action | undefined) => {
  return action?.type == "scan";
};

//TODO: Make it work
const participateInGame = () => {
  gameStore
    .registerRobotOnGame()
    .then(() => {
      toast.value.message = "Registered robot for game";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((reason) => {
      toast.value.message = `Failed to participate. (${reason})`;
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
    });
};

const move = (direction: string) => {
  gameStore
    .moveRobotInDirection(direction)
    .then(() => {
      toast.value.message = "Send robot movement request";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((reason) => {
      toast.value.message = `Failed to send robot move. (${reason})`;
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
    });
};

//TODO: Make it work
const scan = (distance: number) => {
  gameStore
    .scanAroundRobot(distance)
    .then(() => {
      toast.value.message = "Send robot scan request";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((reason) => {
      toast.value.message = `Failed to send robot scan. (${reason})`;
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
    });
};
</script>

<style scoped lang="scss">
@import "../assets/custom-variables";
@import "../assets/darkly-variables";

.is-dpad-control {
  height: 7.6rem;
  width: 7.6rem;

  .columns {
    margin: 0;
  }

  .column {
    padding: 0;
  }

  .button.is-selected,
  .is-scan-action .button {
    .icon {
      color: $link-hover;
    }
  }
}
</style>
