'use client';
/**
 * This is the main file for notifications websocket for the frontend.
 */
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ENDPOINTS } from './config';
import { Notification as NotificationEntity} from './notification';

let stompClient: Client | null = null;

export const connectWebSocket = (
  onMessage: (payload: any) => void
) => {
  if (stompClient?.connected) return stompClient;

  stompClient = new Client({
    webSocketFactory: () =>
      new SockJS(ENDPOINTS.NOTIFICATIONS.WS),

    reconnectDelay: 5000,
    debug: (str) => console.log(str),

    onConnect: () => {
      stompClient!.subscribe(
        '/topic/notifications',
        (message) => {
          const payload: NotificationEntity = JSON.parse(message.body);
          onMessage(payload);
        }
      );
    },
  });

  stompClient.activate();
  return stompClient;
};

export const disconnectWebSocket = () => {
  stompClient?.deactivate();
  stompClient = null;
};
