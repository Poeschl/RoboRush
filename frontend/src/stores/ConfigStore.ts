import { defineStore } from "pinia";
import { ref } from "vue";
import type { MapGenerationResult, SaveSetting, Setting } from "@/models/Config";
import useConfigService from "@/services/ConfigService";
import type { PlaygroundMap } from "@/models/Map";

const configService = useConfigService();

export const useConfigStore = defineStore("configStore", () => {
  const currentConfig = ref<Map<string, Setting>>(new Map());
  const availableMaps = ref<{ maps: PlaygroundMap[] }>({ maps: [] });

  const updateConfig = () => {
    configService.getAllSettings().then((response) => {
      currentConfig.value = new Map(response.map((setting) => [setting.key, setting]));
    });

    configService.getAvailableMaps().then((response) => {
      availableMaps.value.maps = response;
    });
  };

  const save = (setting: SaveSetting): Promise<void> => {
    return configService.saveSetting(setting).then((response) => {
      currentConfig.value.set(response.key, response);
    });
  };

  const uploadNewHeightmap = (file: File): Promise<MapGenerationResult> => {
    return configService.uploadNewHeightmap(file).then((result) => {
      updateConfig();
      return result;
    });
  };

  const setMapActive = (mapId: number, active: boolean): Promise<void> => {
    return configService.setMapActive(mapId, active).then((newMap) => {
      const index = availableMaps.value.maps.findIndex((map) => map.id == mapId);
      availableMaps.value.maps[index] = newMap;
    });
  };

  const removeMap = (mapId: number): Promise<void> => {
    return configService.removeMap(mapId).then(() => {
      availableMaps.value.maps = availableMaps.value.maps.filter((it) => it.id != mapId);
    });
  };

  return { currentConfig, availableMaps, updateConfig, save, uploadNewHeightmap, setMapActive, removeMap };
});
