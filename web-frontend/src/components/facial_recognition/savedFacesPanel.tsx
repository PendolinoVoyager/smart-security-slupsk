import { FaceResponse, fetchFacesByDeviceId } from "@/api/faces";
import { isAuthDataValid } from "@/lib/auth";
import { getAuthData } from "@/lib/auth/server";
import { redirect } from "next/navigation";
import ManageFacesPanel from "./manageFacesPanel";

interface SavedFacesPanelProps  {
    deviceId: number;
}
export default async function SavedFacesPanel({deviceId}: SavedFacesPanelProps) {
    const authData = await getAuthData();
    if (!authData || !isAuthDataValid(authData)) {
        redirect("/login");
    }
   
    return (
        <ManageFacesPanel deviceId={deviceId} token={authData.token} />
    );
}
