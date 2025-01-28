import { Box, CircularProgress } from "@mui/material";
import { ROLE, useProtectedResource } from "../../authUtils.ts";
import { ChangeEvent, useEffect, useState } from "react";
import fetchNotifications, {
  NotificationResponseItem,
} from "../../api/notificationsApi.ts";
import NotificationList from "../notification/notificationList.tsx";
import Pagination from "@mui/material/Pagination";

const NotificationsPage = function () {
  // redirect
  useProtectedResource(ROLE.USER);

  const [isLoaded, setLoaded] = useState(false);
  const [notifications, setNotifications] = useState<
    NotificationResponseItem[]
  >([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    (async () => {
      const res = await fetchNotifications(page);
      setLoaded(true);
      if (res instanceof Error) {
        throw res;
      }
      setNotifications(res.notifications);
      setTotalPages(res.total - 1); // total pages is count but indexing from 0 so minus one
    })();
  }, [page]);

  const handlePageChange = (_: ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  if (!isLoaded) {
    return <CircularProgress size={24} sx={{ color: "white" }} />;
  }

  return (
    <Box style={{ width: "100%", padding: "1rem" }}>
      {notifications.length === 0 ? (
        <h1>You have no notifications yet.</h1>
      ) : (
        <>
          <NotificationList notifications={notifications} />
          <Pagination
            count={totalPages}
            page={page}
            onChange={handlePageChange}
            sx={{ marginTop: "1rem" }}
          />
        </>
      )}
    </Box>
  );
};

export default NotificationsPage;
