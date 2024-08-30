import { defineStore } from "pinia";
import { ref } from "vue";
import type { ClientSettings, MapGenerationResult, SaveSetting, Setting } from "@/models/Config";
import useConfigService from "@/services/ConfigService";
import type { PlaygroundMap, PlaygroundMapAttributes, Tile } from "@/models/Map";
import { WebSocketTopic } from "@/services/WebsocketService";

export const useConfigStore = defineStore("configStore", () => {
  const configService = useConfigService();
  const websocketService = ref<{ initWebsocket: Function; registerForTopicCallback: Function } | undefined>();

  const currentConfig = ref<Map<string, Setting>>(new Map());
  const availableMaps = ref<{ maps: PlaygroundMap[] }>({ maps: [] });
  const clientSettings = ref<ClientSettings>({ globalNotificationText: "", enableWebRobotControl: true, enableUserRegistration: false });

  const initWebsocket = (webSocketInstance: { initWebsocket: Function; registerForTopicCallback: Function }) => {
    websocketService.value = webSocketInstance;
    websocketService.value.registerForTopicCallback(WebSocketTopic.CLIENT_SETTINGS_TOPIC, updateClientConfigTo);
  };

  const updateConfig = () => {
    configService.getAllSettings().then((response) => {
      currentConfig.value = new Map(response.map((setting) => [setting.key, setting]));
    });
  };

  const updateMaps = (): Promise<void> => {
    return configService.getAvailableMaps().then((response) => {
      availableMaps.value.maps = response;
    });
  };

  const updateClientConfig = () => {
    configService.getClientSettings().then((newSettings) => updateClientConfigTo(newSettings));
  };

  const updateClientConfigTo = (newSettings: ClientSettings) => {
    clientSettings.value = newSettings;
  };

  const save = (setting: SaveSetting): Promise<void> => {
    return configService.saveSetting(setting).then((response) => {
      currentConfig.value.set(response.key, response);
    });
  };

  const setGlobalNotificationText = (text: string): Promise<void> => {
    return configService.setGlobalNotificationText(text).then(() => {
      clientSettings.value.globalNotificationText = text;
    });
  };

  const uploadNewHeightmap = (file: File): Promise<MapGenerationResult> => {
    return configService.uploadNewHeightmap(file).then((result) => {
      updateMaps();
      return result;
    });
  };

  const setMapActive = (mapId: number, active: boolean): Promise<void> => {
    return configService.setMapActive(mapId, active).then((changedMap) => {
      const index = availableMaps.value.maps.findIndex((map) => map.id == mapId);
      availableMaps.value.maps[index] = changedMap;
    });
  };

  const setMapAttributes = (attributes: PlaygroundMapAttributes): Promise<void> => {
    return configService.setMapAttributes(attributes.id, attributes).then((changedMap) => {
      const index = availableMaps.value.maps.findIndex((map) => map.id == attributes.id);
      availableMaps.value.maps[index] = changedMap;
    });
  };

  const setMapTile = (mapId: number, tile: Tile): Promise<void> => {
    return configService.setMapTile(mapId, tile).then((changedMap) => {
      const index = availableMaps.value.maps.findIndex((map) => map.id == mapId);
      availableMaps.value.maps[index] = changedMap;
    });
  };

  const removeMap = (mapId: number): Promise<void> => {
    return configService.removeMap(mapId).then(() => {
      availableMaps.value.maps = availableMaps.value.maps.filter((it) => it.id != mapId);
    });
  };

  const exportMap = (map: PlaygroundMap): Promise<HTMLAnchorElement> => {
    return configService.exportMap(map.id).then((response) => {
      const blob = new Blob([response], { type: "image/png" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `${map.mapName}.png`;
      setTimeout(() => URL.revokeObjectURL(link.href), 5000);
      return link;
    });
  };

  return {
    currentConfig,
    availableMaps,
    updateConfig,
    updateMaps,
    save,
    uploadNewHeightmap,
    setMapActive,
    removeMap,
    setMapAttributes,
    setMapTile,
    exportMap,
    clientSettings,
    updateClientConfig,
    initWebsocket,
    setGlobalNotificationText,
  };
});
