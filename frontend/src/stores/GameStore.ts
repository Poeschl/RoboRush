import { defineStore } from "pinia";
import type { ComputedRef, Ref } from "vue";
import { computed, ref } from "vue";
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import type { PlaygroundMap, Tile } from "@/models/Map";
import { useWebSocket, WebSocketTopic } from "@/services/WebsocketService";
import type { User } from "@/models/User";
import { useGameService } from "@/services/GameService";
import type { Error, Game } from "@/models/Game";
import { GameState } from "@/models/Game";
import useRobotService from "@/services/RobotService";
import log from "loglevel";
import type { AxiosError } from "axios";

const robotService = useRobotService();

export const useGameStore = defineStore("gameStore", () => {
  const websocketService = useWebSocket();
  const gameService = useGameService();

  const currentGame = ref<Game>({ currentState: GameState.ENDED, solarChargeEnabled: false });

  const internalMap = ref<PlaygroundMap>();

  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalRobots: Ref<{ robots: PublicRobot[] }> = ref({ robots: [] });
  const robots = computed<PublicRobot[]>(() => internalRobots.value.robots);

  const userRobot = ref<ActiveRobot | undefined>();
  const userRobotActive = computed<boolean>(() => {
    return userRobot.value != null && internalRobots.value.robots.map((robot) => robot.id).includes(userRobot.value.id);
  });

  const updateMap = () => {
    gameService
      .getMap()
      .then((response) => {
        // Clears whole object first
        internalMap.value = undefined;
        internalMap.value = response;
      })
      .catch((reason) => {
        log.error(`Could not get map data (${reason})`);
      });
  };

  const updateRobots = () => {
    robotService
      .getRobots()
      .then((response: PublicRobot[]) => {
        // Clears whole array
        internalRobots.value.robots = [];
        internalRobots.value.robots = response;
      })
      .catch((reason) => {
        log.error(`Could not get robots (${reason})`);
      });
  };

  const retrieveUserRobotState = () => {
    robotService
      .getUserRobot()
      .then((activeRobot) => updateUserRobot(activeRobot))
      .catch((error: AxiosError) => {
        log.info(`Could not retrieve user robot data: ${(error.response?.data as Error).message}`);
        userRobot.value = undefined;
      });
  };

  const initWebsocket = (user: ComputedRef<User | undefined>) => {
    websocketService.initWebsocket(user);
    websocketService.registerForTopicCallback(WebSocketTopic.PUBLIC_ROBOT_TOPIC, updateRobot);
    websocketService.registerForTopicCallback(WebSocketTopic.PRIVATE_ROBOT_TOPIC, updateUserRobot);
    websocketService.registerForTopicCallback(WebSocketTopic.GAME_STATE_TOPIC, updateGameStateTo);
  };

  const updateRobot = (updatedRobot: PublicRobot) => {
    const index = internalRobots.value.robots.findIndex((robot: PublicRobot) => robot.id == updatedRobot.id);
    log.debug(`Update robot with index ${index}`);
    internalRobots.value.robots[index] = updatedRobot;
  };

  const updateUserRobot = (activeRobot: ActiveRobot | undefined) => {
    userRobot.value = activeRobot;
  };

  const updateGameInfo = () => {
    gameService.getCurrentGame().then((gameInfo) => (currentGame.value = gameInfo));
  };

  const updateGameStateTo = (gameState: GameState) => {
    const previousGameState = currentGame.value.currentState;
    currentGame.value.currentState = gameState;

    if (previousGameState == GameState.PREPARE && gameState === GameState.WAIT_FOR_PLAYERS) {
      // Update map data after game preparations
      updateMap();
    }
  };

  const registerRobotOnGame = (): Promise<void> => {
    return robotService.registerCurrentRobotForGame().then(() => {
      updateRobots();
    });
  };

  const moveRobotInDirection = (direction: string): Promise<void> => {
    return robotService.moveRobot(direction);
  };

  const scanAroundRobot = (distance: number): Promise<void> => {
    return robotService.scanOnRobot(distance);
  };

  const waitThatRobot = (): Promise<void> => {
    return robotService.waitOnRobot();
  };

  const refuelRobot = (): Promise<void> => {
    return robotService.refuelRobot();
  };

  const isSolarChargePossible = (): boolean => {
    return currentGame.value.solarChargeEnabled;
  };

  const solarCharge = (): Promise<void> => {
    return robotService.solarChargeRobot();
  };

  return {
    currentMap: internalMap,
    robots,
    userRobot,
    updateMap,
    updateRobots,
    updateGameInfo,
    initWebsocket,
    retrieveUserRobotState,
    currentGame,
    registerRobotOnGame,
    moveRobotInDirection,
    scanAroundRobot,
    userRobotActive,
    waitThatRobot,
    refuelRobot,
    isSolarChargePossible,
    solarCharge,
  };
});
