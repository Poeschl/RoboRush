<template>
  <div class="has-background-black" :style="{ width: width + 'px', height: height + 'px' }">
    <canvas id="worldmap" ref="mapCanvas" class="is-absolute" :width="width" :height="height" />
    <canvas id="robots" ref="robotCanvas" class="is-absolute" :width="width" :height="height" />
  </div>
</template>

<script setup lang="ts">
import type { HeightMap, Position, Tile } from "@/models/Map";
import type { PublicRobot, Robot } from "@/models/Robot";
import { computed, onMounted, ref, watch } from "vue";
import Color from "@/models/Color";

const cellSize = 16;
const cellBorder = 1;
const mapBorderColor = new Color(0, 0, 0);
const mapColor = new Color(120, 211, 71);
const mapHeightSteps = 5;

const props = defineProps<{
  width: number;
  height: number;
  mapData: HeightMap;
  robotData: PublicRobot[];
}>();

const mapCanvas = ref<HTMLCanvasElement>();
const mapDrawContext = computed(() => mapCanvas.value?.getContext("2d"));
const robotCanvas = ref<HTMLCanvasElement>();
const robotDrawContext = computed(() => robotCanvas.value?.getContext("2d"));
const mapXOffset = ref<number>(0);
const mapYOffset = ref<number>(0);

onMounted(() => {
  drawMap(props.mapData);
  drawRobots(props.robotData);
});

const drawMap = (data: HeightMap) => {
  if (mapCanvas.value && mapDrawContext.value) {
    const drawContext = mapDrawContext.value;
    console.info("Draw map");

    drawContext.clearRect(0, 0, mapCanvas.value.width, mapCanvas.value.height);

    let maxX = 0;
    let maxY = 0;
    data.tiles
      .map((tile) => tile.position)
      .forEach((position: Position) => {
        maxX = Math.max(maxX, position.x);
        maxY = Math.max(maxY, position.y);
      });
    console.debug(`Max X: ${maxX} Max Y: ${maxY}`);

    mapXOffset.value = mapCanvas.value.width / 2 - (maxX * (cellSize + 2 * cellBorder)) / 2;
    mapYOffset.value = mapCanvas.value.height / 2 - (maxY * (cellSize + 2 * cellBorder)) / 2;

    drawContext.translate(mapXOffset.value, mapYOffset.value);

    for (const index in data.tiles) {
      const tile = data.tiles[index];
      drawContext.save();
      drawContext.translate(tile.position.x * (cellSize + 2 * cellBorder), tile.position.y * (cellSize + 2 * cellBorder));
      drawTile(drawContext, mapColor.enlighten(tile.height * mapHeightSteps));
      drawContext.restore();
    }
  }
};
watch(() => props.mapData, drawMap);

const drawRobots = (robots: PublicRobot[]) => {
  if (robotCanvas.value && robotDrawContext.value) {
    const drawContext = robotDrawContext.value;
    console.info("Draw robots");

    drawContext.clearRect(0, 0, robotCanvas.value.width, robotCanvas.value.height);

    drawContext.translate(mapXOffset.value, mapYOffset.value);

    for (const index in robots) {
      const robot = robots[index];
      console.debug(`Draw robot ${JSON.stringify(robot)}`);
      drawContext.save();
      drawContext.translate(robot.position.x * (cellSize + 2 * cellBorder), robot.position.y * (cellSize + 2 * cellBorder));
      drawRobot(drawContext, robot.color);
      drawContext.restore();
    }
  }
};
watch(() => props.robotData, drawRobots);

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

<style scoped></style>
