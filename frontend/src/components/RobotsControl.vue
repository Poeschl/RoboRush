<template>
  <div class="box">
    <div class="is-size-3 mb-3">Robot Control</div>
    <div v-if="userStore.loggedIn">
      <div v-if="gameStore.userRobotActive">
        <div class="columns is-variable is-1">
          <div class="column">
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
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightLeft }" @click="move('WEST')">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-caret-left" class="fa-2x" />
                    </div>
                  </button>
                </div>
                <div class="column is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightWait }" @click="wait()" title="Wait the next turn">
                    <div class="icon">
                      <FontAwesomeIcon icon="fa-solid fa-hourglass-half" class="fa-lg" />
                    </div>
                  </button>
                </div>
                <div class="column is-one-third">
                  <button class="button" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightRight }" @click="move('EAST')">
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
              <div class="control">
                <input
                  class="input"
                  type="number"
                  min="0"
                  :disabled="!controlsEnabled"
                  placeholder="Scan distance"
                  @input="(event) => (scanNumber = parseInt((event.target as HTMLInputElement)?.value))"
                />
              </div>
              <div class="control">
                <button class="button" title="Scan the give distance" :disabled="!controlsEnabled" :class="{ 'is-selected': highlightScan }" @click="scan">
                  <div class="icon">
                    <FontAwesomeIcon icon="fa-solid fa-satellite-dish" class="fa-xl" />
                  </div>
                </button>
              </div>
            </div>
            <div class="columns is-multiline additional-actions is-variable is-1">
              <div class="column">
                <button
                  class="button"
                  :disabled="!controlsEnabled || !refuelPossible"
                  :class="{ 'is-selected': highlightRefuel }"
                  @click="refuel()"
                  title="Refuel your robot, if standing on a fuel tile"
                >
                  <div class="icon">
                    <FontAwesomeIcon icon="fa-solid fa-gas-pump" class="fa-lg" />
                  </div>
                </button>
              </div>
              <div class="column">
                <button
                  class="button"
                  :disabled="!controlsEnabled || !solarChargePossible"
                  :class="{ 'is-selected': highlightSolarRecharge }"
                  @click="solarRecharge()"
                  title="Solar charge your robot"
                >
                  <div class="icon">
                    <FontAwesomeIcon icon="fa-solid fa-solar-panel" class="fa-lg" />
                  </div>
                </button>
              </div>
            </div>
            <div class="mb-3">
              <p class="mb-1">Action in next turn:</p>
              <p v-if="robot?.nextAction?.type == 'move'">Move {{ (robot?.nextAction as Move).direction }}</p>
              <p v-if="robot?.nextAction?.type == 'scan'">Scan with distance {{ (robot?.nextAction as Scan).distance }}</p>
              <p v-if="robot?.nextAction?.type == 'wait'">Wait the next turn</p>
              <p v-if="robot?.nextAction?.type == 'refuel'">Refuel the robot</p>
              <p v-if="robot?.nextAction?.type == 'solarCharge'">Solar recharge</p>
            </div>
            <div>
              <p class="mb-1">Last result:</p>
              <p>{{ robot?.lastResult }}</p>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="has-text-centered">
        <div class="mb-2">Your robot does not participate in the current game.</div>
        <button
          class="button"
          @click="participateInGame"
          :disabled="!participationEnabled"
          title="Participation only possible during WAITING_FOR_PLAYERS phase"
        >
          <span class="icon">
            <FontAwesomeIcon icon="fa-solid fa-play" class="fa-xl" />
          </span>
          <span>Participate in current game</span>
        </button>
      </div>
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
import { AxiosError } from "axios";
import { TileType } from "@/models/Map";

const userStore = useUserStore();
const gameStore = useGameStore();

const toast = ref<{ shown: boolean; type: ToastType; message: string }>({ shown: false, type: ToastType.INFO, message: "" });
const scanNumber = ref<number>(0);

const robot = computed(() => gameStore.userRobot);
const controlsEnabled = computed<boolean>(() => gameStore.currentGame.currentState == GameState.WAIT_FOR_ACTION);
const participationEnabled = computed<boolean>(() => gameStore.currentGame.currentState == GameState.WAIT_FOR_PLAYERS);
const refuelPossible = computed<boolean>(
  () => gameStore.currentMap?.mapData.find((tile) => tile.position == robot.value?.position)?.type == TileType.FUEL_TILE,
);
const solarChargePossible = computed<boolean>(() => gameStore.isSolarChargePossible());

const highlightUp = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "NORTH"));
const highlightRight = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "EAST"));
const highlightDown = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "SOUTH"));
const highlightLeft = computed<boolean>(() => isMovementInDirection(robot.value?.nextAction, "WEST"));
const highlightScan = computed<boolean>(() => isType(robot.value?.nextAction, "scan"));
const highlightWait = computed<boolean>(() => isType(robot.value?.nextAction, "wait"));
const highlightRefuel = computed<boolean>(() => isType(robot.value?.nextAction, "refuel"));
const highlightSolarRecharge = computed<boolean>(() => isType(robot.value?.nextAction, "solarCharge"));

const isMovementInDirection = (action: Action | undefined, direction: string) => {
  return action?.type == "move" && (action as Move).direction == direction;
};

const isType = (action: Action | undefined, type: string) => {
  return action?.type == type;
};

const participateInGame = () => {
  handleControlInput(gameStore.registerRobotOnGame());
};

const move = (direction: string) => {
  handleControlInput(gameStore.moveRobotInDirection(direction));
};

const scan = () => {
  handleControlInput(gameStore.scanAroundRobot(scanNumber.value));
};

const wait = () => {
  handleControlInput(gameStore.waitThatRobot());
};

const refuel = () => {
  handleControlInput(gameStore.refuelRobot());
};

const solarRecharge = () => {
  handleControlInput(gameStore.solarCharge());
};

const handleControlInput = (promise: Promise<void>) => {
  promise
    .then(() => {
      toast.value.message = "Sent action";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((error: AxiosError) => {
      toast.value.message = `${(error.response?.data as Error).message} (${error.response?.status})`;
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
    });
};
</script>

<style scoped lang="scss">
@use "../assets/custom-variables";

.is-dpad-control {
  height: 7.6rem;
  width: 7.6rem;
  --bulma-column-gap: 0;
  --bulma-block-spacing: 0;

  .columns {
    justify-content: flex-start;
  }

  .button.is-selected {
    .icon {
      color: custom-variables.$primary;
    }
  }
}

.is-scan-action .button.is-selected {
  .icon {
    color: custom-variables.$primary;
  }
}

.additional-actions {
  .column {
    flex-grow: 0;
  }
}
</style>
