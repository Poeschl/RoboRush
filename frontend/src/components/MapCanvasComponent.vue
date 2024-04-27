<template>
  <div class="has-background-black map-container">
    <canvas id="worldmap" ref="mapCanvas" />
    <canvas id="robots" ref="robotCanvas" />
  </div>
</template>

<script setup lang="ts">
import { type PlaygroundMap, type Tile, TileType } from "@/models/Map";
import type { PublicRobot } from "@/models/Robot";
import { computed, onMounted, ref, watch } from "vue";
import Color from "@/models/Color";
import log from "loglevel";

const cellSize = 16;
const cellBorder = 1;
const mapBorderColor = new Color(0, 0, 0);
const mapColor = new Color(10, 60, 1);
const robotCircleColor = new Color(30, 30, 30);
const targetTileBorderColor = new Color(0, 130, 255);
const startTileBorderColor = new Color(210, 110, 0);
const fuelTileBorderColor = new Color(210, 0, 130);
const specialTileBorderWidth = 4;

const props = defineProps<{
  map: PlaygroundMap | undefined;
  robots: PublicRobot[];
}>();

const mapCanvas = ref<HTMLCanvasElement>();
const mapDrawContext = computed(() => mapCanvas.value?.getContext("2d"));
const robotCanvas = ref<HTMLCanvasElement>();
const robotDrawContext = computed(() => robotCanvas.value?.getContext("2d"));
const mapWidth = ref<number>(800);
const mapHeight = ref<number>(800);
const heightMap = computed<Tile[]>(() => {
  if (props.map != undefined) {
    return props.map.mapData;
  } else {
    return [];
  }
});

onMounted(() => {
  drawMap();
  drawRobots();
});

const updateCanvasSize = (map: PlaygroundMap) => {
  mapWidth.value = map.size.width * (cellSize + 2 * cellBorder);
  mapHeight.value = map.size.height * (cellSize + 2 * cellBorder);

  if (mapCanvas.value && mapDrawContext.value && robotCanvas.value && robotDrawContext.value) {
    mapDrawContext.value.canvas.width = mapWidth.value;
    mapDrawContext.value.canvas.height = mapHeight.value;
    robotDrawContext.value.canvas.width = mapWidth.value;
    robotDrawContext.value.canvas.height = mapHeight.value;
  }
};

const drawMap = () => {
  const tiles = heightMap.value;
  if (mapCanvas.value && mapDrawContext.value && tiles.length > 0) {
    const drawContext = mapDrawContext.value;
    log.debug("Draw map");

    updateCanvasSize(props.map!!);

    const maxHeight = Math.max(...tiles.map((t) => t.height));
    const minHeight = Math.min(...tiles.map((t) => t.height));

    log.debug("Max height: ", maxHeight, "Min height: ", minHeight);

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in tiles) {
      const tile: Tile = tiles[index];
      drawContext.save();
      drawContext.translate(tile.position.x * (cellSize + 2 * cellBorder), tile.position.y * (cellSize + 2 * cellBorder));

      const heightInPercentage = (tile.height - minHeight) / (maxHeight - minHeight);
      drawTile(drawContext, mapColor.enlightenWithPercentage(heightInPercentage));

      if (tile.type == TileType.TARGET_TILE) {
        drawTileBorder(drawContext, targetTileBorderColor);
      } else if (tile.type == TileType.START_TILE) {
        drawTileBorder(drawContext, startTileBorderColor);
      } else if (tile.type == TileType.FUEL_TILE) {
        drawTileBorder(drawContext, fuelTileBorderColor);
      }

      drawContext.restore();
    }
  }
};
watch(
  () => props.map,
  () => {
    drawMap();
    drawRobots();
  },
);

const drawRobots = () => {
  if (robotCanvas.value && robotDrawContext.value) {
    const drawContext = robotDrawContext.value;
    log.debug("Draw robots");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in props.robots) {
      const robot = props.robots[index];
      // log.debug(`Draw robot ${JSON.stringify(robot)}`);
      drawContext.save();
      drawContext.translate(robot.position.x * (cellSize + 2 * cellBorder), robot.position.y * (cellSize + 2 * cellBorder));
      drawRobot(drawContext, robot.color);
      drawContext.restore();
    }
  }
};
watch(props.robots, drawRobots);

const drawTile = (drawContext: CanvasRenderingContext2D, color: Color) => {
  drawContext.fillStyle = mapBorderColor.toHex();
  drawContext.fillRect(0, 0, cellSize + 2 * cellBorder, cellSize + 2 * cellBorder);
  drawContext.fillStyle = color.toHex();
  drawContext.fillRect(cellBorder, cellBorder, cellSize, cellSize);
};

const drawTileBorder = (drawContext: CanvasRenderingContext2D, color: Color) => {
  drawContext.lineWidth = specialTileBorderWidth;
  drawContext.strokeStyle = color.toHex();
  drawContext.strokeRect(
    cellBorder + specialTileBorderWidth / 2,
    cellBorder + specialTileBorderWidth / 2,
    cellSize - specialTileBorderWidth,
    cellSize - specialTileBorderWidth,
  );
};

const drawRobot = (drawContext: CanvasRenderingContext2D, color: Color) => {
  drawContext.beginPath();
  drawContext.arc((cellSize + 2 * cellBorder) / 2, (cellSize + 2 * cellBorder) / 2, cellSize / 2 - cellBorder * 2, 0, 360);
  drawContext.fillStyle = color.toHex();
  drawContext.fill();
  drawContext.fillStyle = robotCircleColor.toHex();
  drawContext.stroke();
  drawContext.closePath();
};
</script>

<style scoped lang="scss">
.map-container {
  position: relative;
  aspect-ratio: 1/1;
  width: 100%;

  canvas {
    position: absolute;
    width: 100%;
    height: 100%;
  }
}
</style>
