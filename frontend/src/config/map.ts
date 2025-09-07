import Color from "@/models/Color";

export default function useMapConstants() {
  const mapBorderColor = new Color(0, 0, 0);
  const mapColor = new Color(10, 60, 1);
  const robotCircleColor = new Color(30, 30, 30);
  const robotIdColor = robotCircleColor;
  const targetTileBorderColor = new Color(0, 130, 255);
  const startTileBorderColor = new Color(210, 110, 0);
  const fuelTileBorderColor = new Color(210, 0, 130);
  const pathMarkerBorderColor = new Color(255, 30, 30);
  const displayPathMarkerBorderColor = new Color(0, 50, 255);

  return {
    mapBorderColor,
    mapColor,
    robotCircleColor,
    robotIdColor,
    targetTileBorderColor,
    startTileBorderColor,
    fuelTileBorderColor,
    pathMarkerBorderColor,
    displayPathMarkerBorderColor,
  };
}
