"use client";

import { useEffect } from "react";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { fetchServices } from "@/store/slices/servicesSlice";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Clock } from "lucide-react";

export default function HomePage() {
  const dispatch = useAppDispatch();
  // Lấy 3 mảnh state từ slice services (có type nhờ useAppSelector).
  const { items, status, error } = useAppSelector((s) => s.services);

  // Khi mount: nếu chưa tải (idle) thì dispatch thunk fetchServices.
  useEffect(() => {
    if (status === "idle") {
      dispatch(fetchServices());
    }
  }, [status, dispatch]);

  return (
    <main className="flex flex-1 flex-col">
      <header className="border-b px-4 py-4">
        <h1 className="text-xl font-semibold">Salon Booking</h1>
        <p className="text-sm text-muted-foreground">Chọn dịch vụ để đặt lịch</p>
      </header>

      <section className="flex-1 space-y-3 p-4">
        {/* Trạng thái loading: hiện skeleton */}
        {status === "loading" &&
          Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full rounded-xl" />
          ))}

        {/* Trạng thái lỗi */}
        {status === "failed" && (
          <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">
            {error}
          </p>
        )}

        {/* Thành công nhưng rỗng */}
        {status === "succeeded" && items.length === 0 && (
          <p className="text-sm text-muted-foreground">Chưa có dịch vụ nào.</p>
        )}

        {/* Danh sách dịch vụ */}
        {items.map((service) => (
          <Card key={service.id} className="overflow-hidden">
            <CardContent className="flex items-center justify-between gap-3 p-4">
              <div className="min-w-0">
                <p className="truncate font-medium">{service.name}</p>
                {service.description && (
                  <p className="truncate text-sm text-muted-foreground">
                    {service.description}
                  </p>
                )}
              </div>
              <Badge variant="secondary" className="shrink-0 gap-1">
                <Clock className="size-3" />
                {service.durationMin} phút
              </Badge>
            </CardContent>
          </Card>
        ))}
      </section>
    </main>
  );
}
