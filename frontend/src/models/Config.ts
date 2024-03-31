export interface Setting {
  key: string;
  value: string;
  type: SettingType;
}

export enum SettingType {
  INT = "INT",
  DURATION = "DURATION",
}

export interface SaveSetting {
  key: string;
  value: string;
}
