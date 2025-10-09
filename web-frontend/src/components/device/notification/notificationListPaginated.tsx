"use server";

import { getAuthData } from "@/lib/auth/server";
import NotificationListClient from "./notificationListClient";
import { Alert, AlertTitle } from "@/components/ui/alert";

interface NotificationListPaginatedProps {
  deviceUuid: string;
}

function ErrorMessage({ msg }: { msg: string }) {
  return (
    <Alert variant="default">
      <AlertTitle>{msg}</AlertTitle>
    </Alert>
  );
}

/**
 * Wrapper that authenticates user and renders paginated client-side notifications.
 */
export default async function NotificationListPaginated({
  deviceUuid,
}: NotificationListPaginatedProps) {
  const authData = await getAuthData();
  if (!authData) {
    return <ErrorMessage msg="You cannot see notifications for this device." />;
  }

  return (
    <NotificationListClient token={authData.token} deviceUuid={deviceUuid} />
  );
}
