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
    return configService.uploadNewHeightmap(file);
  };

  return { currentConfig, availableMaps, updateConfig, save, uploadNewHeightmap };
});
