import React from "react";
import { Card, CardContent, Typography, List } from "@mui/material";
import NotificationComponent from "./notification";
import { NotificationResponseItem } from "../../api/notificationsApi";

type NotificationListProps = {
  notifications: NotificationResponseItem[];
};

const NotificationList: React.FC<NotificationListProps> = ({
  notifications,
}) => {
  return (
    <Card sx={{ margin: "1rem auto", boxShadow: 3 }}>
      <CardContent>
        <Typography variant="h6" component="div" gutterBottom>
          Notifications
        </Typography>
        <List>
          {notifications.map((notification) => (
            <NotificationComponent
              key={notification.id}
              notification={notification}
            />
          ))}
        </List>
      </CardContent>
    </Card>
  );
};

export default NotificationList;
