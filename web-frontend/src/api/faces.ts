import { addCredentials } from "@/lib/auth/client";
import { fetchSafe, HttpError } from "./utils";
import { ENDPOINTS } from "./config";

export const MAX_IMAGE_SIZE: number = 3e6; 
export type FaceResponse = {
    id: number;
    name: string;
    imageUrl: string;   
};

export const fetchFacesByDeviceId = async function (
  token: string,
  deviceId: number
): Promise<FaceResponse[] | HttpError> {
  const res = await fetchSafe<FaceResponse[]>(
    `${ENDPOINTS.FACES.GET_BY_DEVICE}/${deviceId}`,
    addCredentials({}, token)
  );
  return res;
};

export const postFace = async function (
  token: string,
  deviceId: number,
  faceName: string,
  file: File) {
  const formData = new FormData();
  formData.append("deviceId", deviceId.toString());
  formData.append("faceName", faceName);
  formData.append("file", file);
  
  const res = await fetchSafe<unknown>(
    ENDPOINTS.FACES.POST,
    addCredentials({
      method: "POST",
      body: formData
    }, token)
  );
  console.debug(res);
  return res;
};
export const postFaceFormData = async function (
  token: string,
  formData: FormData) {
  
  const res = await fetchSafe<unknown>(
    ENDPOINTS.FACES.POST,
    addCredentials({
      method: "POST",
      body: formData
    }, token)
  );
  console.debug(res);
  return res;
};

export const renameFace = async function (
  token: string,
  faceId: number,
  newFaceName: string) {
  
  const res = await fetchSafe<FaceResponse>(
    `${ENDPOINTS.FACES.PATCH}/${faceId}`,
    addCredentials({
      method: "PATCH",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ newFaceName })
    }, token)
  );
  return res;
}

export const deleteFace = async function (
  token: string,
  faceId: number) {
  
  const res = await fetchSafe<FaceResponse>(
    `${ENDPOINTS.FACES.DELETE}/${faceId}`,
    addCredentials({
      method: "DELETE"
    }, token)
  );
  return res;
}