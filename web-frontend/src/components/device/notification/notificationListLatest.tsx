"use server";

import { fetchByUserPaginated, Notification } from "@/api/notification";
import { Alert, AlertTitle } from "@/components/ui/alert";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { getAuthData } from "@/lib/auth/server";
import { ScrollArea } from "@radix-ui/react-scroll-area";
import { Separator } from "@radix-ui/react-separator";
import { Badge } from "lucide-react";

/**
 * A static list of latest notifications. Doesn't include WebSockets and is rendered on server side.
 */
type NotificationListLatestProps = {
  deviceUuid: string;
};
function ErrorMessage({ msg }: { msg: string }) {
  return (
    <Alert variant="default">
      <AlertTitle>{msg}</AlertTitle>
    </Alert>
  );
}
const NotificationListLatest: React.FC<NotificationListLatestProps> =
  async function ({ deviceUuid }) {
    const authData = await getAuthData();
    if (!authData) {
      return (
        <ErrorMessage msg="You cannot see notifications for this device." />
      );
    }
    const notifications = await fetchByUserPaginated(authData.token, 0);
    if (notifications instanceof Error) {
      return <ErrorMessage msg="Failed to fetch notifications." />;
    }

    return (
      <div className="lg:col-span-1 flex flex-col">
        <Card className="flex-1 flex flex-col">
          <CardHeader>
            <CardTitle>Notifications</CardTitle>
          </CardHeader>
          <CardContent className="flex-1 overflow-y-auto">
            <ScrollArea className="h-80 pr-2">
              <div className="space-y-3">
                {notifications.total === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    No notifications yet.
                  </p>
                ) : (
                  notifications.notifications.map((notif) => (
                    <div
                      key={notif.id}
                      className="p-3 border rounded-lg shadow-sm bg-white dark:bg-gray-800"
                    >
                      <div className="flex justify-between items-center">
                        <Badge color={notif.has_seen ? "secondary" : "default"}>
                          {notif.type}
                        </Badge>
                        <span className="text-xs text-muted-foreground">
                          {new Date(notif.timestamp).toLocaleString()}
                        </span>
                      </div>
                      <Separator className="my-2" />
                      <p className="text-sm">{notif.message}</p>
                    </div>
                  ))
                )}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    );
  };
export default NotificationListLatest;
