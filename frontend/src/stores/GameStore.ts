import { defineStore } from "pinia";
import type { Ref } from "vue";
import { computed, ref } from "vue";
import type { PublicRobot } from "@/models/Robot";
import MapService from "@/services/MapService";
import RobotService from "@/services/RobotService";
import type { Tile } from "@/models/Map";
import { state } from "vue-tsc/out/shared";

const mapService = new MapService();
const robotService = new RobotService();

export const useGameStore = defineStore("gameStore", () => {
  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalHeightMap: Ref<{ tiles: Tile[] }> = ref({ tiles: [] });
  const heightMap = computed<Tile[]>(() => internalHeightMap.value.tiles);
  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalRobots: Ref<{ robots: PublicRobot[] }> = ref({ robots: [] });
  const robots = computed<PublicRobot[]>(() => internalRobots.value.robots);

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
      .getAllRobotStates()
      .then((response: PublicRobot[]) => {
        // Clears whole array
        internalRobots.value.robots = [];
        internalRobots.value.robots = response;
      })
      .catch((reason) => {
        console.error(`Could not get robots (${reason})`);
      });
  }

  function updateRobot(updatedRobot: PublicRobot) {
    const index = internalRobots.value.robots.findIndex((robot: PublicRobot) => robot.id == updatedRobot.id);
    console.debug(`Update robot with index ${index}`);
    internalRobots.value.robots[index] = updatedRobot;
  }

  return { heightMap, robots, updateMap, updateRobots, updateRobot };
});
