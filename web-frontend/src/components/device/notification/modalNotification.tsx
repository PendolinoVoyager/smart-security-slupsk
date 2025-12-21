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
}: {
  notification: Notification;
}) {
  const [open, setOpen] = useState(true);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Notification</DialogTitle>
          <DialogDescription>
            {notification.message}
          </DialogDescription>
        </DialogHeader>

        <div className="flex justify-end pt-4">
          <Button onClick={() => setOpen(false)}>
            OK
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
