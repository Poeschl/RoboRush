<template>
  <div ref="container" class="box map-container p-0" :style="{ 'aspect-ratio': mapAspectRatio }">
    <div class="fog-container">
      <div class="fog">
        <div class="fog-image1"></div>
        <div class="fog-image2"></div>
      </div>
    </div>
    <canvas id="worldmap" ref="mapCanvas" />
    <canvas id="robots" ref="robotCanvas" />
    <canvas id="displayPath" ref="displayPathCanvas" />
    <canvas id="path" ref="pathCanvas" @click="onPathCanvasClick" />
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
const displayPathMarkerBorderColor = new Color(0, 50, 255);

const props = withDefaults(
  defineProps<{
    map: PlaygroundMap | undefined;
    // Strange wrapping is needed since VUE does not recognize the array update correctly
    robots?: { data: PublicRobot[] } | undefined;
    drawablePath?: boolean;
    pathToDisplay?: Path | undefined;
    positionsToDraw?: { data: Position[] } | undefined;
  }>(),
  {
    robots: undefined,
    drawablePath: false,
    pathToDisplay: undefined,
    positionsToDraw: undefined,
  },
);

const container = ref<HTMLDivElement>();
const mapCanvas = ref<HTMLCanvasElement>();
const mapDrawContext = computed(() => mapCanvas.value?.getContext("2d"));
const robotCanvas = ref<HTMLCanvasElement>();
const robotDrawContext = computed(() => robotCanvas.value?.getContext("2d"));
const displayPathCanvas = ref<HTMLCanvasElement>();
const displayPathDrawContext = computed(() => displayPathCanvas.value?.getContext("2d"));
const pathCanvas = ref<HTMLCanvasElement>();
const pathDrawContext = computed(() => pathCanvas.value?.getContext("2d"));

const mapWidth = ref<number>(800);
const mapHeight = ref<number>(800);
const mapAspectRatio = computed<number>(() => mapWidth.value / mapHeight.value);
const heightMap = computed<Tile[]>(() => {
  if (props.map != undefined) {
    return props.map.mapData;
  } else {
    return [];
  }
});
const currentPath = ref<Path>({ points: [] });
const mapTileMinHeight = ref<number>(0);
const mapTileMaxHeight = ref<number>(255);

const emits = defineEmits<{
  (e: "pathUpdate", path: Path): void;
}>();

onMounted(() => {
  if (props.robots) {
    watch(props.robots, drawRobots);
  }
  if (props.pathToDisplay) {
    watch(() => props.pathToDisplay!.points, drawDisplayPath);
  }
  watch(() => props.map?.mapData, drawMapTiles);
  watch(
    () => props.drawablePath,
    (newValue) => {
      if (newValue) {
        currentPath.value.points = [];
        emits("pathUpdate", currentPath.value);
        redraw();
      }
    },
  );
  watch(() => props.map, redraw);
  redraw();
});

const onPathCanvasClick = (event: MouseEvent) => {
  if (pathCanvas.value && container.value && props.drawablePath) {
    // Get css scale factor
    const scale = mapWidth.value / container.value.clientWidth;

    // Make coordinates relative to canvas
    const rect = pathCanvas.value.getBoundingClientRect();
    const x = (event.clientX - rect.x) * scale;
    const y = (event.clientY - rect.y) * scale;

    const positionClicked: Position = { x: Math.floor(x / fullTileSize), y: Math.floor(y / fullTileSize) };

    log.debug(
      "Clicked on",
      positionClicked,
      " rect: ",
      rect.left,
      "|",
      rect.top,
      " event: ",
      event.clientX,
      ",",
      event.y,
      " calc: ",
      x,
      ",",
      y,
      " scale: ",
      scale,
    );

    const knownPath = currentPath.value;
    if (currentPath.value.points.find((it) => it.x === positionClicked.x && it.y === positionClicked.y)) {
      knownPath.points = knownPath.points.filter((it) => !(it.x === positionClicked.x && it.y === positionClicked.y));
    } else {
      knownPath.points.push(positionClicked);
    }
    currentPath.value = knownPath;
    drawPathMarkers();
    emits("pathUpdate", knownPath);
  }
};

const redraw = () => {
  updateCanvasData();
  drawMapTiles();
  drawRobots();
  drawDisplayPath();
  drawPathMarkers();
};

const updateCanvasData = () => {
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
    if (displayPathCanvas.value && displayPathDrawContext.value) {
      displayPathDrawContext.value.canvas.width = mapWidth.value;
      displayPathDrawContext.value.canvas.height = mapHeight.value;
    }

    mapTileMinHeight.value = props.map.minHeight;
    mapTileMaxHeight.value = props.map.maxHeight;
  }
};

const drawMapTiles = () => {
  const drawContext = mapDrawContext.value;
  const tiles = heightMap.value;

  if (drawContext && tiles.length > 0) {
    log.debug("Draw map tiles");

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    const searchSet = new Set(props.positionsToDraw?.data.map((pos) => `${pos.x}|${pos.y}`));
    let filteredTiles;
    if (props.positionsToDraw != undefined) {
      filteredTiles = tiles.filter((tile) => searchSet.has(`${tile.position.x}|${tile.position.y}`));
    } else {
      filteredTiles = tiles;
    }

    for (const tile of filteredTiles) {
      drawContext.save();
      drawContext.translate(tile.position.x * fullTileSize, tile.position.y * fullTileSize);

      const heightInPercentage = (tile.height - mapTileMinHeight.value) / (mapTileMaxHeight.value - mapTileMinHeight.value);
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
  if (robotCanvas.value && robotDrawContext.value && props.robots?.data) {
    const drawContext = robotDrawContext.value;
    log.debug(`Draw robots (count: ${props.robots.data.length})`);

    drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

    for (const robot of props.robots.data) {
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
  drawContext.fillRect(0, 0, fullTileSize, fullTileSize);
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

const drawDisplayPath = () => {
  if (displayPathCanvas.value && displayPathDrawContext.value && props.pathToDisplay) {
    const drawContext = displayPathDrawContext.value;
    log.debug("Draw display path markers");

    drawPath(drawContext, props.pathToDisplay, displayPathMarkerBorderColor, false);
  }
};

const drawPathMarkers = () => {
  if (pathCanvas.value && pathDrawContext.value && props.drawablePath) {
    const drawContext = pathDrawContext.value;
    log.debug("Draw path markers");

    drawPath(drawContext, currentPath.value, pathMarkerBorderColor, true);
  }
};

const drawPath = (drawContext: CanvasRenderingContext2D, path: Path, color: Color, withDots: boolean) => {
  drawContext.clearRect(0, 0, mapWidth.value, mapHeight.value);

  let previousMarker: Position | undefined = undefined;
  for (const marker of path.points) {
    if (withDots) {
      drawContext.save();
      const origin = pixelOriginOfPosition(marker);
      drawContext.translate(origin.x, origin.y);
      drawPathMarker(drawContext, color);
      drawContext.restore();
    }

    if (previousMarker) {
      drawContext.save();
      drawPathBetween(drawContext, color, previousMarker, marker);
      drawContext.restore();
    }

    previousMarker = marker;
  }
};

const drawPathBetween = (drawContext: CanvasRenderingContext2D, color: Color, firstPosition: Position, secondPosition: Position) => {
  const firstOrigin = pixelOriginOfPosition(firstPosition);
  const secondOrigin = pixelOriginOfPosition(secondPosition);

  // shift drawing space to the middle of a tile
  drawContext.translate(fullTileSize / 2, fullTileSize / 2);
  drawContext.beginPath();
  // goto the previous position
  drawContext.moveTo(firstOrigin.x, firstOrigin.y);
  // draw to the current position
  drawContext.lineTo(secondOrigin.x, secondOrigin.y);
  drawContext.strokeStyle = color.toHex();
  drawContext.lineWidth = 2;
  drawContext.stroke();
  drawContext.closePath();
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
@keyframes fog_opacity {
  0% {
    opacity: 0.2;
  }
  22% {
    opacity: 0.8;
  }
  40% {
    opacity: 0.28;
  }
  58% {
    opacity: 0.6;
  }
  80% {
    opacity: 0.16;
  }
  100% {
    opacity: 0.2;
  }
}

@keyframes fog_move {
  0% {
    left: 0;
  }
  100% {
    left: -100%;
  }
}

.map-container {
  position: relative;
  min-width: 200px;
  width: auto;
  height: auto;

  .fog-container {
    position: absolute;
    overflow: hidden;
    width: 100%;
    height: 100%;
    filter: blur(4px) grayscale(0.2) saturate(1.2) sepia(0.2);

    .fog {
      position: absolute;
      width: 200%;
      height: 100%;
      animation:
        fog_opacity 20s linear infinite,
        fog_move 15s linear infinite;

      .fog-image1,
      .fog-image2 {
        background: url("/img/fog.png") repeat-x center left transparent;
        background-size: 180%;
        width: 50%;
        height: 100%;
        float: left;
      }
    }
  }

  canvas {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: var(--bulma-radius);
  }
}
</style>
