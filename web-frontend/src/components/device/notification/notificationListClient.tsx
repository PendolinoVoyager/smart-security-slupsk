"use client";
import React, { PropsWithChildren, useEffect, useState } from "react";
import {
  fetchByUserPaginated,
  fetchNotificationImages,
  Notification,
  NotificationResponse,
  PAGE_SIZE,
} from "@/api/notification";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import { Alert, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
} from "@/components/ui/pagination";
import { useNotifications } from "@/lib/context/NotificationContext";
import { HttpError } from "@/api/utils";
import ModalNotification from "./modalNotification";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Input } from "@/components/ui/input";

interface NotificationListClientProps {
  token: string;
  deviceUuid: string;
}

/**
 * Client-side component handling pagination and display of notifications.
 */
export default function NotificationListClient({
  token,
  deviceUuid,
}: NotificationListClientProps) {
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [modalOpen, setModalOpen] = useState(false);

  const [page, setPage] = useState(0);
  const [data, setData] = useState<NotificationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  async function loadPage(p: number) {
    try {
      setError(null);
      const result = await fetchByUserPaginated(token, p);
      
      if (result instanceof Error) throw result;
      setData(result);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch notifications.");
    } finally {
      setLoading(false);
    }
  }
  
  const {notifications: newNotifications } = useNotifications();
  useEffect(() => {
    loadPage(page);
  }, [page]);
  useEffect(() => {
    loadPage(0);
  }, [newNotifications])

  if (error) {
    return (
      <Alert variant="default">
        <AlertTitle>{error}</AlertTitle>
      </Alert>
    );
  }

  async function handleNotificationClick(notification: Notification) {
    try {
      const images = await fetchNotificationImages(token, notification.id);
      setSelectedNotification(notification);
      if (!(images instanceof HttpError)) {
        setImageUrls(images);
      }
      setModalOpen(true);
    } catch (e) {
      console.error("Failed to load notification images", e);
    }
  }

  console.log(data)
  return (
    <div className="lg:col-span-1 flex flex-col">
      <Card className="flex-1 flex flex-col">
        <CardHeader>
          <CardTitle>Notifications</CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col gap-4">
          {loading && (
            <p className="text-sm text-muted-foreground">Loading...</p>
          )}

          {!loading && data && (
            <>
              {data.notifications.length === 0 ? (
                <p className="text-muted-foreground text-sm">
                  No notifications yet.
                </p>
              ) : (
                <>
                  <PaginationContent />
                  <PaginationControl data={data} onPageChange={setPage}/>
                  <ScrollArea className="h-80 pr-2">
                    <div className="space-y-3">
                      {data.notifications.map((notif) => (
                        <NotificationListItem notification={notif} key={notif.id} onClick={() => handleNotificationClick(notif)} />
                      ))}
                    </div>
                  </ScrollArea>

                </>
              )}
            </>
          )}
        </CardContent>
      </Card>
      {modalOpen && selectedNotification && (
      <ModalNotification
        notification={selectedNotification}
        imageUrls={imageUrls}
        onClose={() => {setModalOpen(false); setSelectedNotification(null)}}
      />
      )}

    </div>
  );
}

interface PaginationControlProps extends PropsWithChildren{
  onPageChange: (p: number) => void;
  data: NotificationResponse;
}
const PaginationControl: React.FC<PaginationControlProps> = function({data, onPageChange, ...props}) {
    return(<>
        {/* Pagination */}
        {data.total > 1 && (
          <Pagination className="mt-2">
          <PaginationContent className="flex items-center justify-center gap-2">
            {/* Previous */}
            <PaginationItem>
              <Button
                variant="outline"
                size="icon"
                disabled={data.page === 0}
                onClick={() => {
                  const newPage = Math.max(data.page - 1, 0);
                  onPageChange(newPage);
                }}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
            </PaginationItem>

            {/* Page input */}
            <PaginationItem className="flex items-center gap-1">
              <Input
                type="number"
                min={1}
                max={data.total}
                value={data.page + 1}
                onChange={(e) => {
                  const value = Number(e.target.value)
                  if (Number.isNaN(value)) return

                  // clamp + convert to 0-based
                  const nextPage = Math.min(
                    Math.max(value - 1, 0),
                    data.total - 1
                  )

                  onPageChange(nextPage)
                }}
                className="h-8 w-16 text-center"
              />
              <span className="text-sm text-muted-foreground">
                / {data.total}
              </span>
            </PaginationItem>

            {/* Next */}
            <PaginationItem>
              <Button
                variant="outline"
                size="icon"
                disabled={data.page + 1 >= data.total}
                onClick={() => onPageChange(Math.min(data.page + 1, data.total - 1))}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </PaginationItem>
          </PaginationContent>
        </Pagination>

    )}
    </>);
}

function NotificationListItem({
  notification,
  onClick,
}: {
  notification: Notification;
  onClick?: () => void;
}) {
    const badgeClassMap: Record<string, string> = {
    critical: "bg-red-600 text-white",
    warning: "bg-orange-500 text-white",
    info: "bg-blue-500 text-white",
    visit: "bg-lime-500 text-black",
    default: "bg-gray-400 text-black",
  };

  const badgeClass =
    badgeClassMap[notification.type.toLowerCase()] ??
    badgeClassMap.default;

  return (
    <div
      onClick={onClick}
      className="p-3 border rounded-lg shadow-sm bg-card cursor-pointer hover:bg-accent transition"
    >
      <div className="flex justify-between items-center">
        <Badge
          variant={notification.has_seen ? "secondary" : "default"}
          className={`badge-lg font-semibold ${badgeClass}`}
        >
          {notification.type.toUpperCase()}
        </Badge>
        <span className="text-xs text-muted-foreground">
          {new Date(notification.timestamp).toLocaleString()}
        </span>
      </div>
      <Separator className="my-2" />
      <p className="text-sm">{notification.message}</p>
    </div>
  );
}
