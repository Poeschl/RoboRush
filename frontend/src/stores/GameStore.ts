import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import type { PublicRobot, Robot } from "@/models/Robot";
import MapService from "@/services/MapService";
import RobotService from "@/services/RobotService";
import type { Tile } from "@/models/Map";

const mapService = new MapService();
const robotService = new RobotService();

export const useGameStore = defineStore("gameStore", () => {
  const heightMap = ref<Tile[]>([]);
  const robots = ref<PublicRobot[]>([]);

  function updateMap() {
    mapService
      .getHeightMap()
      .then((response) => {
        // Clears whole array
        heightMap.value = [];
        heightMap.value = response;
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
        robots.value = [];
        robots.value = response;
      })
      .catch((reason) => {
        console.error(`Could not get robots (${reason})`);
      });
  }

  return { heightMap, robots, updateMap, updateRobots };
});
