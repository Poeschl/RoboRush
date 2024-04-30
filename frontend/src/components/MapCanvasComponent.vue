<template>
  <div class="has-background-black map-container">
    <canvas id="worldmap" ref="mapCanvas" />
    <canvas id="robots" ref="robotCanvas" />
    <canvas id="path" ref="pathCanvas" />
  </div>
</template>

<script setup lang="ts">
import { type Path, type PixelPosition, type PlaygroundMap, type Position, type Tile, TileType } from "@/models/Map";
import type { PublicRobot } from "@/models/Robot";
import { computed, onMounted, ref, watch } from "vue";
import Color from "@/models/Color";
import log from "loglevel";

const cellSize = 16;
const cellBorder = 1;
const specialTileBorderWidth = 4;
const fullTileSize = cellSize + 2 * cellBorder;

const mapBorderColor = new Color(0, 0, 0);
const mapColor = new Color(10, 60, 1);
const robotCircleColor = new Color(30, 30, 30);
const targetTileBorderColor = new Color(0, 130, 255);
const startTileBorderColor = new Color(210, 110, 0);
const fuelTileBorderColor = new Color(210, 0, 130);
const pathMarkerBorderColor = new Color(255, 30, 30);

const props = withDefaults(
  defineProps<{
    map: PlaygroundMap | undefined;
    robots?: PublicRobot[] | undefined;
    drawablePath: boolean;
  }>(),
  {
    drawablePath: false,
  },
);

const mapCanvas = ref<HTMLCanvasElement>();
const mapDrawContext = computed(() => mapCanvas.value?.getContext("2d"));
const robotCanvas = ref<HTMLCanvasElement>();
const robotDrawContext = computed(() => robotCanvas.value?.getContext("2d"));
const pathCanvas = ref<HTMLCanvasElement>();
const pathDrawContext = computed(() => pathCanvas.value?.getContext("2d"));

const mapWidth = ref<number>(800);
const mapHeight = ref<number>(800);
const heightMap = computed<Tile[]>(() => {
  if (props.map != undefined) {
    return props.map.mapData;
  } else {
    return [];
  }
});
const currentPath = ref<Path>({
  points: [
    { x: 1, y: 1 },
    { x: 18, y: 20 },
    { x: 20, y: 1 },
  ],
});

onMounted(() => {
  redraw();
  if (props.robots) {
    watch(props.robots, drawRobots);
  }
  watch(() => props.drawablePath, redraw);
  watch(() => props.map, redraw);
});

const redraw = () => {
  updateCanvasSize();
  drawMap();
  drawPathMarkers();
  drawRobots();
};

const updateCanvasSize = () => {
  if (props.map) {
    mapWidth.value = props.map.size.width * fullTileSize;
    mapHeight.value = props.map.size.height * fullTileSize;

    if (mapCanvas.value && mapDrawContext.value) {
      mapDrawContext.value.canvas.width = mapWidth.value;
      mapDrawContext.value.canvas.height = mapHeight.value;
    }
    if (robotCanvas.value && robotDrawContext.value) {
      robotDrawContext.value.canvas.width = mapWidth.value;
      robotDrawContext.value.canvas.height = mapHeight.value;
    }
    if (pathCanvas.value && pathDrawContext.value) {
      pathDrawContext.value.canvas.width = mapWidth.value;
      pathDrawContext.value.canvas.height = mapHeight.value;
    }
  }
};

const drawMap = () => {
  const tiles = heightMap.value;
  if (mapCanvas.value && mapDrawContext.value && tiles.length > 0) {
    const drawContext = mapDrawContext.value;
    log.debug("Draw terrain");

    const maxHeight = Math.max(...tiles.map((t) => t.height));
    const minHeight = Math.min(...tiles.map((t) => t.height));

    log.debug("Max height: ", maxHeight, "Min height: ", minHeight);

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const index in tiles) {
      const tile: Tile = tiles[index];
      drawContext.save();
      drawContext.translate(tile.position.x * fullTileSize, tile.position.y * fullTileSize);

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

const drawRobots = () => {
  if (robotCanvas.value && robotDrawContext.value && props.robots) {
    const drawContext = robotDrawContext.value;
    log.debug("Draw robots");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const robot of props.robots) {
      // log.debug(`Draw robot ${JSON.stringify(robot)}`);
      drawContext.save();
      const origin = pixelOriginOfPosition(robot.position);
      drawContext.translate(origin.x, origin.y);
      drawRobot(drawContext, robot.color);
      drawContext.restore();
    }
  }
};

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
  drawContext.arc(fullTileSize / 2, fullTileSize / 2, cellSize / 2 - cellBorder * 2, 0, 360);
  drawContext.fillStyle = color.toHex();
  drawContext.fill();
  drawContext.fillStyle = robotCircleColor.toHex();
  drawContext.stroke();
  drawContext.closePath();
};

const drawPathMarkers = () => {
  if (pathCanvas.value && pathDrawContext.value && props.drawablePath) {
    const drawContext = pathDrawContext.value;
    log.debug("Draw path markers");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    let previousMarker: Position | undefined = undefined;
    for (const marker of currentPath.value.points) {
      drawContext.save();
      const origin = pixelOriginOfPosition(marker);
      drawContext.translate(origin.x, origin.y);
      drawPathMarker(drawContext, pathMarkerBorderColor);
      drawContext.restore();

      if (previousMarker) {
        drawPathBetween(drawContext, pathMarkerBorderColor, previousMarker, marker);
      }

      previousMarker = marker;
    }
  }
};

const drawPathBetween = (drawContext: CanvasRenderingContext2D, color: Color, firstPosition: Position, secondPosition: Position) => {
  drawContext.save();
  const firstOrigin = pixelOriginOfPosition(firstPosition);
  const secondOrigin = pixelOriginOfPosition(secondPosition);

  // shift drawing space to the middle of a tile
  drawContext.translate(fullTileSize / 2, fullTileSize / 2);
  // goto the previous position
  drawContext.moveTo(firstOrigin.x, firstOrigin.y);
  // draw to the current position
  drawContext.lineTo(secondOrigin.x, secondOrigin.y);
  drawContext.strokeStyle = color.toHex();
  drawContext.lineWidth = 2;
  drawContext.stroke();
  drawContext.restore();
};

const drawPathMarker = (drawContext: CanvasRenderingContext2D, color: Color) => {
  drawContext.beginPath();
  drawContext.arc(fullTileSize / 2, fullTileSize / 2, cellSize / 2 - cellBorder * 4, 0, 360);
  drawContext.fillStyle = color.toHex();
  drawContext.fill();
  drawContext.closePath();
};

const pixelOriginOfPosition = (position: Position): PixelPosition => {
  return { x: position.x * fullTileSize, y: position.y * fullTileSize };
};
</script>

<style scoped lang="scss">
.map-container {
  position: relative;
  aspect-ratio: 1/1;
  min-width: 600px;
  width: auto;
  height: auto;

  canvas {
    position: absolute;
    width: 100%;
    height: 100%;
  }
}
</style>
