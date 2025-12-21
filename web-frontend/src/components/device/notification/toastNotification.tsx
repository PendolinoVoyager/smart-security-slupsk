
import { useEffect } from 'react';
import { useToast } from '@/hooks/use-toast';
import { Notification } from '@/api/notification';

export default function ToastNotification({
  notification,
}: {
  notification: Notification;
}) {
  const { toast } = useToast();

  useEffect(() => {
    toast({
      title: notification.type.toUpperCase(),
      description: notification.message,
      variant: mapVariant(notification.type),
    });
  }, [notification, toast]);

  return null;
}

function mapVariant(type: Notification['type']) {
  switch (type.toLowerCase()) {
    case 'info':
      return 'default';
    case 'error':
      return 'destructive';
    case 'warning':
      return 'default';
    default:
      return 'default';
  }
}
