import React from "react";
import { NotificationResponse } from "../../api/notificationsApi";
import { Avatar, ListItem, ListItemAvatar, ListItemText } from "@mui/material";
import { CheckCircle, Notifications } from "@mui/icons-material";

interface NotificationComponentProps {
  notification: NotificationResponse;
}
const NotificationComponent: React.FC<NotificationComponentProps> = function ({
  notification,
}) {
  return (
    <ListItem
      key={Math.random().toString() + notification.type}
      sx={{
        mb: 1,
        bgcolor:
          notification.has_seen === "true"
            ? "background.paper"
            : "primary.dark",
        color:
          notification.has_seen === "true" ? "text.primary" : "common.white",
        borderRadius: 2,
      }}
    >
      <ListItemAvatar>
        <Avatar>
          {notification.has_seen === "true" ? (
            <CheckCircle color="success" />
          ) : (
            <Notifications color="primary" />
          )}
        </Avatar>
      </ListItemAvatar>
      <ListItemText
        primary={notification.type}
        secondary={notification.message}
      />
    </ListItem>
  );
};
export default NotificationComponent;
