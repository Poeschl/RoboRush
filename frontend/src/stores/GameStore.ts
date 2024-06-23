import { defineStore } from "pinia";
import type { ComputedRef, Ref } from "vue";
import { computed, ref } from "vue";
import type { ActiveRobot, PublicRobot, ScoreboardEntry } from "@/models/Robot";
import type { PlaygroundMap, Position } from "@/models/Map";
import { WebSocketTopic } from "@/services/WebsocketService";
import type { User } from "@/models/User";
import { useGameService } from "@/services/GameService";
import type { Error, Game } from "@/models/Game";
import { GameState } from "@/models/Game";
import useRobotService from "@/services/RobotService";
import log from "loglevel";
import type { AxiosError } from "axios";

const robotService = useRobotService();

export const useGameStore = defineStore("gameStore", () => {
  const websocketService = ref<{ initWebsocket: Function; registerForTopicCallback: Function } | undefined>();
  const gameService = useGameService();

  const currentGame = ref<Game>({ currentState: GameState.ENDED, currentTurn: 0, solarChargePossible: false, fullMapScanPossible: false });

  const internalMap = ref<PlaygroundMap>();
  const globalKnownPositions: Ref<{ data: Position[] }> = ref({ data: [] });

  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalRobots: Ref<{ data: PublicRobot[] }> = ref({ data: [] });
  const scoreBoard: Ref<{ data: ScoreboardEntry[] }> = ref({ data: [] });

  const userRobot = ref<ActiveRobot | undefined>();
  const userRobotActive = computed<boolean>(() => {
    return userRobot.value != null && internalRobots.value.data.map((robot) => robot.id).includes(userRobot.value.id);
  });
  const userRobotKnownPositions: Ref<{ data: Position[] }> = ref({ data: [] });

  const updateMap = () => {
    gameService
      .getMap()
      .then((response) => {
        // Clears whole object first
        internalMap.value = undefined;
        internalMap.value = response;
        updateGameInfo();
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
        internalRobots.value.data = [];
        internalRobots.value.data = response;
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

  const initWebsocket = (service: { initWebsocket: Function; registerForTopicCallback: Function }, user: ComputedRef<User | undefined>) => {
    websocketService.value = service;

    websocketService.value.initWebsocket(user);
    websocketService.value.registerForTopicCallback(WebSocketTopic.PUBLIC_ROBOT_TOPIC, updateRobot);
    websocketService.value.registerForTopicCallback(WebSocketTopic.PRIVATE_ROBOT_TOPIC, updateUserRobot);
    websocketService.value.registerForTopicCallback(WebSocketTopic.GAME_STATE_TOPIC, updateGameStateTo);
    websocketService.value.registerForTopicCallback(WebSocketTopic.GAME_TURN_TOPIC, updateGameTurnTo);
    websocketService.value.registerForTopicCallback(WebSocketTopic.PUBLIC_KNOWN_POSITIONS_TOPIC, updateGlobalKnownPositionsTo);
    websocketService.value.registerForTopicCallback(WebSocketTopic.PRIVATE_KNOWN_POSITIONS_TOPIC, updateUserRobotKnownPositionsTo);
  };

  const updateRobot = (updatedRobot: PublicRobot) => {
    const index = internalRobots.value.data.findIndex((robot: PublicRobot) => robot.id == updatedRobot.id);

    if (index >= 0) {
      // It's an update
      internalRobots.value.data[index] = updatedRobot;
    } else {
      // It's a new robot
      internalRobots.value.data.push(updatedRobot);
    }
  };

  const updateUserRobot = (activeRobot: ActiveRobot | undefined) => {
    userRobot.value = activeRobot;
  };

  const updateGameInfo = () => {
    gameService.getCurrentGame().then((gameInfo) => (currentGame.value = gameInfo));
    updateGlobalKnownPositions();
  };

  const updateGlobalKnownPositions = () => {
    robotService.getAllRobotsKnownPositions().then((positions) => updateGlobalKnownPositionsTo(positions));
  };

  const updateUserRobotKnownPositions = () => {
    robotService.getUserRobotKnownPositions().then((positions) => updateUserRobotKnownPositionsTo(positions));
  };

  const updateScoreBoard = () => {
    robotService.getTop10Robots().then((newScoreboard) => (scoreBoard.value.data = newScoreboard));
  };

  const updateGameStateTo = (gameState: GameState) => {
    const previousGameState = currentGame.value.currentState;
    currentGame.value.currentState = gameState;

    if (previousGameState == GameState.ENDED && gameState === GameState.PREPARE) {
      // Reset robot list on prepare stage
      internalRobots.value.data = [];
      updateGameInfo();
    }
    if (previousGameState == GameState.PREPARE && gameState === GameState.WAIT_FOR_PLAYERS) {
      // Update map data after game preparations
      updateMap();
      updateGlobalKnownPositions();
    }
    if (gameState === GameState.ENDED) {
      updateScoreBoard();
      updateGameInfo();
    }
  };

  const updateGameTurnTo = (turnCount: number) => {
    currentGame.value.currentTurn = turnCount;
  };

  const updateGlobalKnownPositionsTo = (positions: Position[]) => {
    globalKnownPositions.value.data = positions;
  };

  const updateUserRobotKnownPositionsTo = (positions: Position[]) => {
    userRobotKnownPositions.value.data = positions;
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

  const fullMapScan = (): Promise<void> => {
    return robotService.fullMapScan();
  };

  const waitThatRobot = (): Promise<void> => {
    return robotService.waitOnRobot();
  };

  const refuelRobot = (): Promise<void> => {
    return robotService.refuelRobot();
  };

  const solarCharge = (): Promise<void> => {
    return robotService.solarChargeRobot();
  };

  return {
    currentMap: internalMap,
    robots: internalRobots,
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
    fullMapScan,
    userRobotActive,
    waitThatRobot,
    refuelRobot,
    solarCharge,
    updateScoreBoard,
    scoreBoard,
    globalKnownPositions,
    userRobotKnownPositions,
    updateUserRobotKnownPositions,
  };
});
