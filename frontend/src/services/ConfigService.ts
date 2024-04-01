import type { SaveSetting, Setting } from "@/models/Config";
import axiosWithAuth from "@/config/axiosWithAuth";

export default function useConfigService() {
  const baseConfigUrl = "/api/config";

  const getAllSettings = (): Promise<Setting[]> => {
    return axiosWithAuth.get(`${baseConfigUrl}`).then((response) => response.data);
  };

  const saveSetting = (setting: SaveSetting): Promise<Setting> => {
    return axiosWithAuth.post(`${baseConfigUrl}`, setting).then((response) => response.data);
  };

  const uploadNewHeightmap = (file: File): Promise<void> => {
    const data = new FormData();
    data.append("heightmap", file);

    return axiosWithAuth.post(`${baseConfigUrl}/map/heightmap`, data, {
      headers: { "Content-Type": `multipart/form-data` },
    });
  };

  return { getAllSettings, saveSetting, uploadNewHeightmap };
}
