export interface Setting {
  key: string;
  value: string;
  type: SettingType;
}

export enum SettingType {
  INT = "INT",
  DURATION = "DURATION",
  BOOLEAN = "BOOLEAN",
}

export interface SaveSetting {
  key: string;
  value: string;
}

export interface MapGenerationResult {
  warnings: string[];
}

export interface ClientSettings {
  globalNotificationText: string;
  useFogOfWar: boolean;
  enableWebRobotControl: boolean;
}
