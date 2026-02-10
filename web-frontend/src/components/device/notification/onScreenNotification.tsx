'use client';

import { Notification } from '@/api/notification';
import { useNotifications } from '@/lib/context/NotificationContext';
import ModalNotification from './modalNotification';
import ToastNotification from './toastNotification';

export default function OnScreenNotification() {
  const { notifications } = useNotifications();
  const importantNotificationTypes = ['critical', 'doorbell', 'motion'];
  return (
    <>
      {notifications.map((n) =>
        importantNotificationTypes.includes(n.type.toLocaleLowerCase()) ? (
          <ModalNotification notification={n} key={n.id} />
        ) : (
          <ToastNotification notification={n} key={n.id} />
        )
      )}
    </>
  );
}


