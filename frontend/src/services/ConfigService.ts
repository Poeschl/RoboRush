import type { MapGenerationResult, SaveSetting, Setting } from "@/models/Config";
import axiosWithAuth from "@/config/axiosWithAuth";
import type { AxiosResponse } from "axios";
import type { PlaygroundMap, PlaygroundMapAttributes } from "@/models/Map";

export default function useConfigService() {
  const baseConfigUrl = "/api/config";

  const getAllSettings = (): Promise<Setting[]> => {
    return axiosWithAuth.get(`${baseConfigUrl}`).then((response) => response.data);
  };

  const saveSetting = (setting: SaveSetting): Promise<Setting> => {
    return axiosWithAuth.post(`${baseConfigUrl}`, setting).then((response) => response.data);
  };

  const uploadNewHeightmap = (file: File): Promise<MapGenerationResult> => {
    const data = new FormData();
    data.append("heightmap", file);

    return axiosWithAuth
      .post(`${baseConfigUrl}/map/heightmap`, data, {
        headers: { "Content-Type": `multipart/form-data` },
      })
      .then((data: AxiosResponse<MapGenerationResult>) => data.data);
  };

  const getAvailableMaps = (): Promise<PlaygroundMap[]> => {
    return axiosWithAuth.get(`${baseConfigUrl}/map`).then((response) => response.data);
  };

  const setMapActive = (mapId: number, active: boolean): Promise<PlaygroundMap> => {
    return axiosWithAuth.post(`${baseConfigUrl}/map/${mapId}/active`, { active: active }).then((response) => response.data);
  };

  const setMapAttributes = (mapId: number, attributes: PlaygroundMapAttributes): Promise<PlaygroundMap> => {
    return axiosWithAuth
      .post(`${baseConfigUrl}/map/${mapId}`, {
        mapName: attributes.mapName,
        maxRobotFuel: attributes.maxRobotFuel,
        solarChargeEnabled: attributes.solarChargeEnabled,
      })
      .then((response) => response.data);
  };

  const removeMap = (mapId: number): Promise<void> => {
    return axiosWithAuth.delete(`${baseConfigUrl}/map/${mapId}`);
  };

  return { getAllSettings, saveSetting, uploadNewHeightmap, getAvailableMaps, setMapActive, removeMap, setMapAttributes };
}
