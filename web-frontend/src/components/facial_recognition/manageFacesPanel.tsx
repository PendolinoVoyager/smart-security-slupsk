"use client"


import { FaceResponse, fetchFacesByDeviceId } from "@/api/faces";

import { useEffect, useState } from "react";
import { FacesGrid } from "./facesGrid";
import { AddNewFaceButton } from "./addNewFaceButton";
import { Separator } from "radix-ui";

interface SavedFacesPanelProps {
    deviceId: number;
    token: string;
}
export default function ManageFacesPanel({deviceId, token, ...props}: SavedFacesPanelProps) {
    const [faces, setFaces] = useState<FaceResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | undefined>();
    useEffect(() => {
        const fetchFaces = async () => {
            const fetchedFaces = await fetchFacesByDeviceId(token, deviceId);
            if (fetchedFaces instanceof Error) {
                console.error("Error fetching faces:", fetchedFaces);
                setLoading(false);
                setError(fetchedFaces.message);
            } else {
                setLoading(false);
                setFaces(fetchedFaces);
            }            
        }
        fetchFaces();
    }, [token, deviceId]);

    return (
        <>
        <AddNewFaceButton deviceId={deviceId} />
        <Separator.Root className="SeparatorRoot mt-3 mb-3" 
        decorative
        />
        <FacesGrid faces={faces} loading={loading} error={error} />
        </>
    );
}    
    