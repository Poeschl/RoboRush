<template>
  <div class="has-background-black map-container">
    <canvas id="worldmap" ref="mapCanvas" />
    <canvas id="robots" ref="robotCanvas" />
  </div>
</template>

<script setup lang="ts">
import { type Position, type Tile, TileType } from "@/models/Map";
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
  mapData: Tile[];
  robotData: PublicRobot[];
}>();

const mapCanvas = ref<HTMLCanvasElement>();
const mapDrawContext = computed(() => mapCanvas.value?.getContext("2d"));
const robotCanvas = ref<HTMLCanvasElement>();
const robotDrawContext = computed(() => robotCanvas.value?.getContext("2d"));
const mapWidth = ref<number>(800);
const mapHeight = ref<number>(800);

onMounted(() => {
  drawMap();
  drawRobots();
});

const updateCanvasSize = (data: Tile[]) => {
  let maxX = 0;
  let maxY = 0;
  data
    .map((tile) => tile.position)
    .forEach((position: Position) => {
      maxX = Math.max(maxX, position.x);
      maxY = Math.max(maxY, position.y);
    });
  // Adjust to the zero-based map
  maxX += 1;
  maxY += 1;

  // log.debug(`Max X: ${maxX} Max Y: ${maxY}`);

  mapWidth.value = maxX * (cellSize + 2 * cellBorder);
  mapHeight.value = maxY * (cellSize + 2 * cellBorder);

  if (mapCanvas.value && mapDrawContext.value && robotCanvas.value && robotDrawContext.value) {
    mapDrawContext.value.canvas.width = mapWidth.value;
    mapDrawContext.value.canvas.height = mapHeight.value;
    robotDrawContext.value.canvas.width = mapWidth.value;
    robotDrawContext.value.canvas.height = mapHeight.value;
  }
};

const drawMap = () => {
  if (mapCanvas.value && mapDrawContext.value) {
    const drawContext = mapDrawContext.value;
    log.debug("Draw map");

    updateCanvasSize(props.mapData);

    const maxHeight = Math.max(...props.mapData.map((t) => t.height));
    const minHeight = Math.min(...props.mapData.map((t) => t.height));

    log.debug("Max height: ", maxHeight, "Min height: ", minHeight);

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in props.mapData) {
      const tile: Tile = props.mapData[index];
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
watch(props.mapData, () => {
  drawMap();
  drawRobots();
});

const drawRobots = () => {
  if (robotCanvas.value && robotDrawContext.value) {
    const drawContext = robotDrawContext.value;
    log.debug("Draw robots");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in props.robotData) {
      const robot = props.robotData[index];
      // log.debug(`Draw robot ${JSON.stringify(robot)}`);
      drawContext.save();
      drawContext.translate(robot.position.x * (cellSize + 2 * cellBorder), robot.position.y * (cellSize + 2 * cellBorder));
      drawRobot(drawContext, robot.color);
      drawContext.restore();
    }
  }
};
watch(props.robotData, drawRobots);

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

<style scoped>
canvas {
  display: block;
  grid-column-start: 1;
  grid-row-start: 1;
}

.map-container {
  display: grid;
  width: fit-content;
  height: fit-content;
}
</style>
