import React from "react";
import { Card, CardContent, Typography, List } from "@mui/material";
import { NotificationResponse } from "../../api/notificationsApi";
import NotificationComponent from "./notification";

type NotificationListProps = {
  notifications: NotificationResponse[];
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
            <NotificationComponent notification={notification} />
          ))}
        </List>
      </CardContent>
    </Card>
  );
};

export default NotificationList;
