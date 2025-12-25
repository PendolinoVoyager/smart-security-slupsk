import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useState } from 'react';
import { Notification } from '@/api/notification';

export default function ModalNotification({
  notification,
  imageUrls,
  onClose
}: {
  notification: Notification;
  imageUrls?: string[];
  onClose?: () => void
}) {
  const [open, setOpen] = useState(true);

  function handleClose() {
    setOpen(false);
    onClose?.();
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(newOpen) => {
        if (!newOpen) {
          handleClose();
        } else {
          setOpen(true);
        }
      }}
    >
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Notification</DialogTitle>
          <DialogDescription>
            {notification.message}
          </DialogDescription>
        </DialogHeader>
        
        {imageUrls && imageUrls.length > 0 && (
          <div className="mt-4 grid grid-cols-2 gap-3">
            {imageUrls.map((url, index) => (
              <img
                key={index}
                src={url}
                alt={`notification-image-${index}`}
                className="rounded-md object-cover w-full h-32"
              />
            ))}
          </div>
        )}

        <div className="flex justify-end pt-4">
          <Button onClick={handleClose}>
            OK
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
