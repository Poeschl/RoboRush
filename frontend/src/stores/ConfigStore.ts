import { defineStore } from "pinia";
import { ref } from "vue";
import type { SaveSetting, Setting } from "@/models/Config";
import useConfigService from "@/services/ConfigService";

const configService = useConfigService();

export const useConfigStore = defineStore("configStore", () => {
  const currentConfig = ref<Map<string, Setting>>(new Map());

  const updateConfig = () => {
    configService.getAllSettings().then((response) => {
      currentConfig.value = new Map(response.map((setting) => [setting.key, setting]));
    });
  };

  const save = (setting: SaveSetting): Promise<void> => {
    return configService.saveSetting(setting).then((response) => {
      currentConfig.value.set(response.key, response);
    });
  };

  const uploadNewHeightmap = (file: File): Promise<void> => {
    return configService.uploadNewHeightmap(file);
  };

  return { currentConfig, updateConfig, save, uploadNewHeightmap };
});
