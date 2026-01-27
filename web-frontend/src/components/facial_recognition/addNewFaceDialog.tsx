"use client"

import { useEffect, useRef, useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { ExclamationTriangleIcon } from "@radix-ui/react-icons"
import { Textarea } from "../ui/textarea"
import { Input } from "../ui/input"
type AddNewFaceDialogProps = {
  open: boolean
  onClose: () => void
  onConfirm: (image: File) => void // you'll handle upload later
}

export function AddNewFaceDialog({
  open,
  onClose,
  onConfirm,
}: AddNewFaceDialogProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [stream, setStream] = useState<MediaStream | null>(null)
  const [captured, setCaptured] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isCameraDenied, setIsCameraDenied] = useState(false);
  const [name, setName] = useState<string>("New Person");
  // Start / stop webcam
  useEffect(() => {
    if (!open) {
        stream?.getTracks().forEach(track => track.stop())
        setStream(null)
        setCaptured(null);
        return
    }
    setIsCameraDenied(false);
    navigator.mediaDevices
        .getUserMedia({ video: true })
        .then(mediaStream => {
        setStream(mediaStream)
        if (videoRef.current) {
            videoRef.current.srcObject = mediaStream
        }
        }).catch(() => setIsCameraDenied(true));

    return () => {
        stream?.getTracks().forEach(track => track.stop())
        setStream(null)
    }
    }, [open, captured])


  const takePhoto = () => {
    if (!videoRef.current || !canvasRef.current) return

    const video = videoRef.current
    const canvas = canvasRef.current
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight

    const ctx = canvas.getContext("2d")
    if (!ctx) return

    ctx.drawImage(video, 0, 0)
    canvas.toBlob(blob => {
      
      if (blob) {
        setCaptured(new File([blob], name));
      }
    }, "image/jpeg")
  }

  const reset = () => {
    setCaptured(null);
    
  }

  const confirm = () => {
    if (captured) {
      onConfirm(captured)
      onClose()
      setCaptured(null)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Add new person</DialogTitle>
        </DialogHeader>

        <div className="flex justify-center">
          {!captured ? (
            !isCameraDenied ?<video
              ref={videoRef}
              autoPlay
              playsInline
              className="aspect-square w-full rounded-md object-cover"
            /> : (<div className="flex-col">
            <ExclamationTriangleIcon/>
            <h1>Camera not available. Please use existing photo.</h1>
            </div>)
          ) : (
            <img
              src={URL.createObjectURL(captured)}
              className="aspect-square w-full rounded-md object-cover"
              alt="Captured face"
            />
          )}
        </div>

        <canvas ref={canvasRef} className="hidden" />

        <DialogFooter className="gap-2">
          {!captured ? (
            <>
              <Button variant="secondary" onClick={onClose}>
                Cancel
              </Button>
              <input type="file" accept="image/*" hidden ref={fileInputRef} onChange={(e) => {
                 if (fileInputRef.current?.files?.length !== 1) {
                  return;
                }
                
                setName(fileInputRef.current.files[0].name);
                setCaptured(fileInputRef.current.files[0]);
                confirm();
              }}/>
              <Button onClick={() => {
                fileInputRef.current?.click();
              }}>
                Use existing photo
              </Button>
              <Button onClick={takePhoto}>
                Take photo
              </Button>
            </>
          ) : (
            <>
              <Input type="text" defaultValue={"New Face"} value={name} onChange={(e) => setName(e.target.value)} />
              <Button variant="secondary" onClick={reset}>
                Retake
              </Button>
              <Button onClick={confirm}>
                Use photo
              </Button>

            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
