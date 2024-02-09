import { defineStore } from "pinia";
import type { ComputedRef, Ref, UnwrapRef } from "vue";
import { computed, ref } from "vue";
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import MapService from "@/services/MapService";
import type { Tile } from "@/models/Map";
import RobotService from "@/services/RobotService";
import { useWebSocket, WebSocketTopic } from "@/services/WebsocketService";
import type { LoginRequest, RegisterRequest, User } from "@/models/User";

const mapService = new MapService();
const robotService = new RobotService();

export const useGameStore = defineStore("gameStore", () => {
  let websocketService = useWebSocket();

  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalHeightMap: Ref<{ tiles: Tile[] }> = ref({ tiles: [] });
  const heightMap = computed<Tile[]>(() => internalHeightMap.value.tiles);

  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalRobots: Ref<{ robots: PublicRobot[] }> = ref({ robots: [] });
  const robots = computed<PublicRobot[]>(() => internalRobots.value.robots);

  const userRobot = ref<ActiveRobot | undefined>();

  function updateMap() {
    mapService
      .getHeightMap()
      .then((response) => {
        // Clears whole array
        internalHeightMap.value.tiles = [];
        internalHeightMap.value.tiles = response;
      })
      .catch((reason) => {
        console.error(`Could not get heightmap (${reason})`);
      });
  }

  function updateRobots() {
    robotService
      .getRobots()
      .then((response: PublicRobot[]) => {
        // Clears whole array
        internalRobots.value.robots = [];
        internalRobots.value.robots = response;
      })
      .catch((reason) => {
        console.error(`Could not get robots (${reason})`);
      });
  }

  function retrieveUserRobotState() {
    robotService
      .getUserRobot()
      .then((activeRobot) => updateUserRobot(activeRobot))
      .catch((reason) => {
        console.error(`Could not retrieve user robot data: ${reason}`);
      });
  }

  function initWebsocket(user: ComputedRef<User | undefined>) {
    websocketService.initWebsocket(user);
    websocketService.registerForTopicCallback(WebSocketTopic.PUBLIC_ROBOT_TOPIC, updateRobot);
    websocketService.registerForTopicCallback(WebSocketTopic.PRIVATE_ROBOT_TOPIC, updateUserRobot);
  }

  function updateRobot(updatedRobot: PublicRobot) {
    const index = internalRobots.value.robots.findIndex((robot: PublicRobot) => robot.id == updatedRobot.id);
    console.debug(`Update robot with index ${index}`);
    internalRobots.value.robots[index] = updatedRobot;
  }

  function updateUserRobot(activeRobot: ActiveRobot | undefined) {
    userRobot.value = activeRobot;
  }

  return { heightMap, robots, userRobot, updateMap, updateRobots, initWebsocket, retrieveUserRobotState };
});
