"use server"

import { revalidatePath } from "next/cache"
import {deleteFace, renameFace, postFace, postFaceFormData } from "@/api/faces";
import { getAuthData } from "./auth/server";
import { isAuthDataValid } from "./auth";
import { HttpError } from "@/api/utils";

export async function deleteFaceAction(token: string, faceId: number) {
  const res = await deleteFace(token, faceId);
  if (res instanceof Error) {
    console.error("Error deleting face:", res);
    return res;
  }
  revalidatePath("/devices/manage-access")
  return res;
}

export async function renameFaceAction(token: string, faceId: number, newFaceName: string) {
    const res = await renameFace(token, faceId, newFaceName);
  if (res instanceof Error) {
    console.error("Error renaming face:", res);
    return res;
  }
  revalidatePath("/devices/manage-access")
  return res;
}

export async function uploadFaceAction(formData: FormData): Promise<string | Error> {
  const authData= await getAuthData();
  if (!authData || !isAuthDataValid(authData)) {
    throw new HttpError("Invalid token: " + authData, 403);
  }
  const res = await postFaceFormData(authData.token, formData);
  if (res instanceof Error) {
    console.error("Error deleting face:", res);
    return res;
  }
  revalidatePath("/devices/manage-access")
  return "Person saved succesfully!";
}
