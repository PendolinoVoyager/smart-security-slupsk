"use client";

import React, { useEffect, useState } from "react";
import {
  fetchByUserPaginated,
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
  const [page, setPage] = useState(0);
  const [data, setData] = useState<NotificationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadPage(p: number) {
    try {
      setLoading(true);
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

  useEffect(() => {
    loadPage(page);
  }, [page]);

  if (error) {
    return (
      <Alert variant="default">
        <AlertTitle>{error}</AlertTitle>
      </Alert>
    );
  }

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
              {data.total === 0 ? (
                <p className="text-muted-foreground text-sm">
                  No notifications yet.
                </p>
              ) : (
                <>
                  <ScrollArea className="h-80 pr-2">
                    <div className="space-y-3">
                      {data.notifications.map((notif) => (
                        <div
                          key={notif.id}
                          className="p-3 border rounded-lg shadow-sm bg-card"
                        >
                          <div className="flex justify-between items-center">
                            <Badge
                              variant={notif.has_seen ? "secondary" : "default"}
                            >
                              {notif.type}
                            </Badge>
                            <span className="text-xs text-muted-foreground">
                              {new Date(notif.timestamp).toLocaleString()}
                            </span>
                          </div>
                          <Separator className="my-2" />
                          <p className="text-sm">{notif.message}</p>
                        </div>
                      ))}
                    </div>
                  </ScrollArea>

                  {/* Pagination */}
                  {data.total > PAGE_SIZE && (
                    <Pagination className="mt-2">
                      <PaginationContent className="flex justify-center gap-2">
                        <PaginationItem>
                          <Button
                            variant="outline"
                            size="sm"
                            disabled={page === 0}
                            onClick={() => setPage((p) => Math.max(p - 1, 0))}
                          >
                            Previous
                          </Button>
                        </PaginationItem>

                        <PaginationItem>
                          <span className="text-sm text-muted-foreground">
                            Page {data.page + 1}
                          </span>
                        </PaginationItem>

                        <PaginationItem>
                          <Button
                            variant="outline"
                            size="sm"
                            disabled={(data.page + 1) * PAGE_SIZE >= data.total}
                            onClick={() => setPage((p) => p + 1)}
                          >
                            Next
                          </Button>
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}
                </>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
