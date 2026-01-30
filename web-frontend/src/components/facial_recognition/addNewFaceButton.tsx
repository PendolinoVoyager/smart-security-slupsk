"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { AddNewFaceDialog } from "./addNewFaceDialog"
import { PlusCircle } from "lucide-react"
import { MAX_IMAGE_SIZE } from "@/api/faces"
import { useToast } from "@/hooks/use-toast"
import { uploadFaceAction } from "@/lib/faceActions"
import { useRouter } from "next/navigation"

interface AddNewFaceButtonProps  {
  deviceId: number;
}
export function AddNewFaceButton({deviceId}: AddNewFaceButtonProps) {
  const [open, setOpen] = useState(false)
  const router = useRouter();
  const toast = useToast();
  return (
    <>
      <Button className="pointerHover" onClick={() => setOpen(true)}>
        <PlusCircle />
        Add new person
      </Button>

      <AddNewFaceDialog
        open={open}
        onClose={() => setOpen(false)}
        onConfirm={(image) => {
          if (image.size > MAX_IMAGE_SIZE) {
            toast.toast({
              title: "File too large!",
              description: "The image you provided is too large, please use another one.",
              color: "orange"
            });
            return;
          }
          console.log("Captured image:", image)
          const formData = new FormData();
          formData.set("file", image);
          formData.set("faceName", image.name.split(".")[0]);
          formData.set("deviceId", String(deviceId));
          const handler = async () =>{
            const res = await uploadFaceAction(formData);
            if (res instanceof Error) {
              toast.toast({
              title: res.name,
              description: res.message,
              variant: "destructive"
            });
            }
            else {
              window.location.reload();
              toast.toast({
                title: res,
              });
            }

          }
          handler();

        }}
      />
    </>
  )
}
