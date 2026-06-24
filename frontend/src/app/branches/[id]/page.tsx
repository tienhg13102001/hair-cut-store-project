"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { fetchBranches } from "@/store/slices/branchesSlice";
import { fetchServices } from "@/store/slices/servicesSlice";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { ChevronLeft, Clock } from "lucide-react";

export default function BranchDetailPage() {
  // useParams trả { id: "3" } cho route /branches/[id]. Param luôn là STRING
  // (lấy từ URL) → đổi sang number để so với branch.id.
  const params = useParams<{ id: string }>();
  const branchId = Number(params.id);

  const dispatch = useAppDispatch();

  // Lấy chi nhánh từ store đã tải (nếu vào thẳng URL thì store rỗng → fetch lại).
  const branchesState = useAppSelector((s) => s.branches);
  const branch = branchesState.items.find((b) => b.id === branchId);

  // Tái dùng servicesSlice đã có — không cần slice mới.
  const { items: services, status: svcStatus, error } = useAppSelector(
    (s) => s.services,
  );

  useEffect(() => {
    if (branchesState.status === "idle") dispatch(fetchBranches());
    if (svcStatus === "idle") dispatch(fetchServices());
  }, [branchesState.status, svcStatus, dispatch]);

  return (
    <main className="flex flex-1 flex-col">
      <header className="border-b px-4 py-4">
        {/* Nút quay lại trang chọn chi nhánh */}
        <Link
          href="/"
          className="mb-2 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
        >
          <ChevronLeft className="size-4" />
          Đổi chi nhánh
        </Link>
        <h1 className="text-xl font-semibold">
          {branch ? branch.name : `Chi nhánh #${branchId}`}
        </h1>
        {branch && (
          <p className="text-sm text-muted-foreground">{branch.address}</p>
        )}
      </header>

      <section className="flex-1 space-y-3 p-4">
        <h2 className="text-sm font-medium text-muted-foreground">
          Chọn dịch vụ
        </h2>

        {svcStatus === "loading" &&
          Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full rounded-xl" />
          ))}

        {svcStatus === "failed" && (
          <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">
            {error}
          </p>
        )}

        {svcStatus === "succeeded" && services.length === 0 && (
          <p className="text-sm text-muted-foreground">Chưa có dịch vụ nào.</p>
        )}

        {services.map((service) => (
          <Link
            key={service.id}
            href={`/branches/${branchId}/book/${service.id}`}
          >
            <Card className="overflow-hidden transition-colors hover:bg-accent">
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
          </Link>
        ))}
      </section>
    </main>
  );
}
