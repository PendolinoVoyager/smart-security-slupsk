'use client';

import { Notification } from '@/api/notification';
import { useNotifications } from '@/lib/context/NotificationContext';
import ModalNotification from './modalNotification';
import ToastNotification from './toastNotification';

export default function OnScreenNotification() {
  const { notifications } = useNotifications();
  
  return (
    <>
      {notifications.map((n) =>
        n.type.toLowerCase() === 'visit' ? (
          <ModalNotification notification={n} key={n.id} />
        ) : (
          <ToastNotification notification={n} key={n.id} />
        )
      )}
    </>
  );
}


