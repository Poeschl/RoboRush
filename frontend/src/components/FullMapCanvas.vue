<template>
  <div class="has-background-black map-container">
    <canvas id="worldmap" ref="mapCanvas" />
    <canvas id="robots" ref="robotCanvas" />
  </div>
</template>

<script setup lang="ts">
import type { Position, Tile } from "@/models/Map";
import type { PublicRobot } from "@/models/Robot";
import { computed, onMounted, ref, watch } from "vue";
import Color from "@/models/Color";

const cellSize = 16;
const cellBorder = 1;
const mapBorderColor = new Color(0, 0, 0);
const mapColor = new Color(120, 211, 71);
const mapHeightSteps = 5;

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

  // console.debug(`Max X: ${maxX} Max Y: ${maxY}`);

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
    console.info("Draw map");

    updateCanvasSize(props.mapData);

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in props.mapData) {
      const tile = props.mapData[index];
      // console.debug(`Draw Tile ${JSON.stringify(tile.position)}`);
      drawContext.save();
      drawContext.translate(tile.position.x * (cellSize + 2 * cellBorder), tile.position.y * (cellSize + 2 * cellBorder));
      drawTile(drawContext, mapColor.enlighten(tile.height * mapHeightSteps));
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
    console.info("Draw robots");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in props.robotData) {
      const robot = props.robotData[index];
      // console.debug(`Draw robot ${JSON.stringify(robot)}`);
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

const drawRobot = (drawContext: CanvasRenderingContext2D, color: Color) => {
  drawContext.beginPath();
  drawContext.arc((cellSize + 2 * cellBorder) / 2, (cellSize + 2 * cellBorder) / 2, cellSize / 2 - cellBorder * 2, 0, 360);
  drawContext.fillStyle = color.toHex();
  drawContext.fill();
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
  max-height: 50rem;
  max-width: 100%;
  display: grid;
  overflow: auto;
}
</style>
