import { Box, CircularProgress } from "@mui/material";
import { ROLE, useProtectedResource } from "../../authUtils.ts";
import { useEffect, useState } from "react";
import fetchNotifications, {
  NotificationResponse,
} from "../../api/notificationsApi.ts";
import NotificationList from "../notification/notificationList.tsx";

const NotificationsPage = function () {
  // redirect
  useProtectedResource(ROLE.USER);
  const [isLoaded, setLoaded] = useState(false);
  const [notifications, setNotifications] = useState<NotificationResponse[]>(
    []
  );
  useEffect(() => {
    (async () => {
      const res = await fetchNotifications(100);
      setLoaded(true);
      if (res instanceof Error) {
        throw res;
      }
      setNotifications(res);
    })();
  }, []);

  if (!isLoaded) {
    return <CircularProgress size={24} sx={{ color: "white" }} />;
  }
  return (
    <Box style={{ width: "100%", padding: "1rem" }}>
      {notifications.length === 0 ? (
        <h1>You have no notifications yet.</h1>
      ) : (
        <NotificationList notifications={notifications} />
      )}
    </Box>
  );
};

export default NotificationsPage;
