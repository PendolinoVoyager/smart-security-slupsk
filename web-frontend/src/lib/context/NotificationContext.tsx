'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { connectWebSocket, disconnectWebSocket } from '@/api/websocket';
import { Notification } from '@/api/notification';

type NotificationContextType = {
  notifications: Notification[];
};

const NotificationContext = createContext<NotificationContextType>({
  notifications: [],
});

export const useNotifications = () => useContext(NotificationContext);

export const NotificationProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    connectWebSocket((notification: Notification) => {
      setNotifications((prev) => [...prev, notification]);
    });

    return () => disconnectWebSocket();
  }, []);

  return (
    <NotificationContext.Provider value={{ notifications }}>
      {children}
    </NotificationContext.Provider>
  );
};
