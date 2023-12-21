import { defineStore } from "pinia";
import type { Ref } from "vue";
import { computed, ref, watch } from "vue";
import type { PublicRobot, Robot } from "@/models/Robot";
import MapService from "@/services/MapService";
import type { Tile } from "@/models/Map";
import Color from "@/models/Color";
import { useUserStore } from "@/stores/UserStore";

const mapService = new MapService();

export const useGameStore = defineStore("gameStore", () => {
  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalHeightMap: Ref<{ tiles: Tile[] }> = ref({ tiles: [] });
  const heightMap = computed<Tile[]>(() => internalHeightMap.value.tiles);

  // Needed workaround, since ref() don't detect updates on pure arrays.
  const internalRobots: Ref<{ robots: PublicRobot[] }> = ref({ robots: [] });
  const robots = computed<PublicRobot[]>(() => internalRobots.value.robots);

  const userRobot = ref<Robot>();

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
    mapService
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

  function updateRobot(updatedRobot: PublicRobot) {
    const index = internalRobots.value.robots.findIndex((robot: PublicRobot) => robot.id == updatedRobot.id);
    console.debug(`Update robot with index ${index}`);
    internalRobots.value.robots[index] = updatedRobot;

    const userStore = useUserStore();
    if (userStore.loggedIn) {
      updateUserRobot();
    }
  }

  function updateUserRobot() {
    const userStore = useUserStore();
    //TODO: Make proper calls, as soon as the robots api detects the correct robot for the user
    userRobot.value = { id: 0, position: { x: 2, y: 3 }, fuel: 100, color: new Color(123, 123, 123) };
  }

  return { heightMap, robots, userRobot, updateMap, updateRobots, updateRobot, updateUserRobot };
});
