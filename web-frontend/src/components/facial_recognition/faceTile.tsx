"use client"
/**A component to display a single saved face.
 * Can see its name and rename it or delete it.
 */

import * as Card from "@/components/ui/card";
import { Card as C } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { FaceResponse } from "@/api/faces";
import { CardContent, CardFooter } from "@/components/ui/card";
import { getAuthData } from "@/lib/auth/client";
import { useToast } from "@/hooks/use-toast";
import { useEffect, useRef, useState } from "react";
import { ConfirmDeleteFaceDialog } from "./confirmDeleteFaceDialog";
import { useRouter } from "next/navigation"
import { deleteFaceAction, renameFaceAction } from "@/lib/faceActions";
import { HttpError } from "@/api/utils";
type FaceTileProps = {
  face: FaceResponse;
};
export function FaceTile({ face }: FaceTileProps) {
  const { imageUrl, name } = face;
  const toast = useToast();
  const router = useRouter();
  const authData = getAuthData();
  if (!authData) {
    throw new Error("No auth data found");
  }
  const [deleteOpen, setDeleteOpen] = useState(false)
  const ref = useRef<HTMLInputElement>(null);
  const [oldName, setOldName]  = useState<string | null>(null);
  useEffect(() => {
    if (ref.current && oldName === null) {
        setOldName(ref.current.value);
    }
  }, [ref])
  const handleDeletingFace = () => {
    const handler = async () => {
    const res = await deleteFaceAction(authData.token, face.id);
    console.log(res);
    if (res instanceof Error) {
      console.error("Error deleting face:", res);
        toast.toast({
          title: "Error deleting face",
          description: res.message,
          variant: "destructive",
        });
    }
    else {
        toast.toast({
          title: "Face deleted",
          description: "The face has been successfully deleted.",
          variant: "default",
          color: "success"
        });
        // hard reset because next.js :D
        window.location.reload();

    }}
    handler();
    }  
    
    const handleNameChange = function(e: Event) {
        const asyncNameChange = async () => {
            e.preventDefault();
            if (!ref.current) {
                return
            }
            const newName = ref.current.value;
            newName.trim();
            if (newName === "") {
                toast.toast({
                    title: "Empty face name is not allowed!",
                    variant: "destructive",
                });
                ref.current.value = oldName!;
            }
            if (newName === oldName) {
                return;
            }
            const res = await renameFaceAction(authData.token, face.id, newName);
            if (res instanceof Error) {
                toast.toast({
                title: "Error renaming face",
                description: res.message,
                variant: "destructive",
              });
              return;
            };
            ref.current.blur();
            toast.toast(
                {
                    title: "Changed face name to " + newName,
                    variant: "default",
                    color: "success"
                }
            );
            setOldName(newName);
        }
        asyncNameChange();
    }
  return (
    <>
      <C className="w-full max-w-[200px] bg-tetriary p-1">
        <Card.CardHeader className="p-0">
          <div className="aspect-square overflow-hidden rounded-t-md">
            <img
              src={imageUrl}
              alt={name}
              className="h-full w-full object-cover"
            />
          </div>
        </Card.CardHeader>

        <CardContent className="pt-3">
          <Input name="faceName" className="bg-input border-dashed focus:border-accent" onBlur={(e) => handleNameChange(e as any)}
           ref={ref}
           defaultValue={name}
           onKeyDown={(e) => e.key === "Enter" && handleNameChange(e as any)}
        />
        </CardContent>

        <CardFooter className="pt-0 mb-1">
          <Button
            variant="destructive"
            className="w-full pointerHover"
            onClick={() => setDeleteOpen(true)}
          >
            Delete
          </Button>
        </CardFooter>
      </C>

      <ConfirmDeleteFaceDialog
        open={deleteOpen}
        face={face}
        onCancel={() => setDeleteOpen(false)}
        onConfirm={() => {
          setDeleteOpen(false)
          handleDeletingFace();
        }}
      />
    </>
  );
}