"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { fetchBranches } from "@/store/slices/branchesSlice";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { MapPin, Clock, ChevronRight } from "lucide-react";

// Cắt "08:00:00" -> "08:00" cho gọn (bỏ phần giây).
function hhmm(time: string) {
  return time.slice(0, 5);
}

export default function HomePage() {
  const dispatch = useAppDispatch();
  const { items, status, error } = useAppSelector((s) => s.branches);

  useEffect(() => {
    if (status === "idle") {
      dispatch(fetchBranches());
    }
  }, [status, dispatch]);

  return (
    <main className="flex flex-1 flex-col">
      <header className="border-b px-4 py-4">
        <h1 className="text-xl font-semibold">Chọn chi nhánh</h1>
        <p className="text-sm text-muted-foreground">
          Salon gần bạn để đặt lịch
        </p>
      </header>

      <section className="flex-1 space-y-3 p-4">
        {status === "loading" &&
          Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}

        {status === "failed" && (
          <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">
            {error}
          </p>
        )}

        {status === "succeeded" && items.length === 0 && (
          <p className="text-sm text-muted-foreground">Chưa có chi nhánh nào.</p>
        )}

        {/* Mỗi card bọc trong <Link> -> bấm vào sang /branches/{id} */}
        {items.map((branch) => (
          <Link key={branch.id} href={`/branches/${branch.id}`}>
            <Card className="overflow-hidden transition-colors hover:bg-accent">
              <CardContent className="flex items-center gap-3 p-4">
                <div className="min-w-0 flex-1 space-y-2">
                  <div className="flex items-center justify-between gap-3">
                    <p className="truncate font-medium">{branch.name}</p>
                    <Badge variant="secondary" className="shrink-0 gap-1">
                      <Clock className="size-3" />
                      {hhmm(branch.openingAt)}–{hhmm(branch.closingAt)}
                    </Badge>
                  </div>
                  <p className="flex items-start gap-1.5 text-sm text-muted-foreground">
                    <MapPin className="mt-0.5 size-4 shrink-0" />
                    <span className="truncate">{branch.address}</span>
                  </p>
                </div>
                <ChevronRight className="size-5 shrink-0 text-muted-foreground" />
              </CardContent>
            </Card>
          </Link>
        ))}
      </section>
    </main>
  );
}
