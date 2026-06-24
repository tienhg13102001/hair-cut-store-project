"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { toast } from "sonner";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { fetchBranches } from "@/store/slices/branchesSlice";
import { fetchServices } from "@/store/slices/servicesSlice";
import { fetchBarbersByBranch } from "@/store/slices/barbersSlice";
import { createBooking } from "@/store/slices/appointmentsSlice";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ChevronLeft, Check, Star } from "lucide-react";

import { cn } from "@/lib/utils";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";

// ── Helper thuần (không dùng React) ──────────────────────────────────────────

// Trả về N ngày kể từ HÔM NAY (gồm hôm nay). Dùng để hiện dãy nút chọn ngày.
function nextDays(n: number): Date[] {
  const today = new Date();
  today.setHours(0, 0, 0, 0); // về 00:00 để so sánh ngày cho chuẩn
  return Array.from({ length: n }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() + i);
    return d;
  });
}

// "2026-06-24" theo GIỜ ĐỊA PHƯƠNG (KHÔNG dùng toISOString vì nó đổi sang UTC,
// có thể lệch 1 ngày). Tự ghép year-month-day.
function dateKey(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

const WEEKDAYS = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

// Sinh các mốc giờ từ open→close, bước stepMin phút. "08:00:00" -> ["08:00",...].
// Bỏ mốc mà bắt đầu xong + thời lượng dịch vụ vượt quá giờ đóng cửa.
function timeSlots(openingAt: string, closingAt: string, stepMin: number, durationMin: number): string[] {
  const toMin = (t: string) => {
    const [h, m] = t.split(":").map(Number);
    return h * 60 + m;
  };
  const open = toMin(openingAt);
  const close = toMin(closingAt);
  const slots: string[] = [];
  for (let t = open; t + durationMin <= close; t += stepMin) {
    const h = String(Math.floor(t / 60)).padStart(2, "0");
    const m = String(t % 60).padStart(2, "0");
    slots.push(`${h}:${m}`);
  }
  return slots;
}

// ── Component ─────────────────────────────────────────────────────────────────

export default function BookingPage() {
  const params = useParams<{ id: string; serviceId: string }>();
  const branchId = Number(params.id);
  const serviceId = Number(params.serviceId);

  const dispatch = useAppDispatch();
  const router = useRouter();
  const submitting = useAppSelector((s) => s.appointments.submitting);

  const branchesState = useAppSelector((s) => s.branches);
  const servicesState = useAppSelector((s) => s.services);
  const branch = branchesState.items.find((b) => b.id === branchId);
  const service = servicesState.items.find((s) => s.id === serviceId);

  const { items: barbers, status: barbersStatus, error } = useAppSelector((s) => s.barbers);

  // State cục bộ của wizard: 3 lựa chọn của khách.
  const [selectedBarberId, setSelectedBarberId] = useState<number | null>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");

  useEffect(() => {
    if (branchesState.status === "idle") dispatch(fetchBranches());
    if (servicesState.status === "idle") dispatch(fetchServices());
    dispatch(fetchBarbersByBranch(branchId));
  }, [branchId, branchesState.status, servicesState.status, dispatch]);

  const days = nextDays(7);
  const durationMin = service?.durationMin ?? 30;
  // Chỉ tính mốc giờ khi đã biết giờ mở/đóng của chi nhánh.
  const slots = branch ? timeSlots(branch.openingAt, branch.closingAt, 30, durationMin) : [];

  // Đã đủ 3 lựa chọn chưa? Quyết định nút "Tiếp tục" bật/tắt.
  const ready =
    selectedBarberId !== null && selectedDate !== null && selectedTime !== null && customerName.trim() !== "" && customerPhone.trim() !== "";

  // async vì có await dispatch (chờ POST xong mới biết thành/bại).
  async function handleContinue() {
    if (!ready) return;
    // Ghép ngày + giờ -> Date theo giờ địa phương -> ISO (UTC, có Z).
    // Đây chính là startAt/endAt mà backend (OffsetDateTime) nhận.
    const start = new Date(`${selectedDate}T${selectedTime}:00`);
    const end = new Date(start.getTime() + durationMin * 60_000);

    const result = await dispatch(
      createBooking({
        body: {
          branchId,
          barberId: selectedBarberId!, // ready đã đảm bảo không null
          walkInName: customerName,
          walkInPhone: customerPhone,
          startAt: start.toISOString(),
          endAt: end.toISOString(),
        },
        serviceId,
      }),
    );

    // createAsyncThunk trả 1 action; .match() phân biệt fulfilled vs rejected.
    if (createBooking.fulfilled.match(result)) {
      toast.success("Đặt lịch thành công!", {
        description: `Mã lịch #${result.payload.id} · ${start.toLocaleString("vi-VN")}`,
      });
      router.push("/"); // quay về trang chủ sau khi đặt xong
    } else {
      toast.error(result.payload ?? "Đặt lịch thất bại");
    }
  }

  return (
    <main className="flex flex-1 flex-col">
      <header className="border-b px-4 py-4">
        <Link href={`/branches/${branchId}`} className="mb-2 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
          <ChevronLeft className="size-4" />
          Đổi dịch vụ
        </Link>
        <h1 className="text-xl font-semibold">Đặt lịch</h1>
        <p className="text-sm text-muted-foreground">
          {service ? service.name : `Dịch vụ #${serviceId}`}
          {branch ? ` · ${branch.name}` : ""}
        </p>
      </header>

      <section className="flex-1 space-y-6 p-4">
        {/* ── 1. CHỌN THỢ ── */}
        <div className="space-y-3">
          <h2 className="text-sm font-medium text-muted-foreground">1. Chọn thợ</h2>

          {barbersStatus === "loading" && Array.from({ length: 2 }).map((_, i) => <Skeleton key={i} className="h-20 w-full rounded-xl" />)}

          {barbersStatus === "failed" && <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">{error}</p>}

          {barbersStatus === "succeeded" && barbers.length === 0 && <p className="text-sm text-muted-foreground">Chi nhánh này chưa có thợ nào.</p>}

          {barbers.map((barber) => {
            const isSelected = barber.id === selectedBarberId;
            return (
              <Card
                key={barber.id}
                onClick={() => setSelectedBarberId(barber.id)}
                className={cn("cursor-pointer overflow-hidden transition-colors", isSelected ? "ring-2 ring-primary" : "hover:bg-accent")}
              >
                <CardContent className="flex items-center gap-3 p-4">
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium">{barber.user.fullName}</p>
                    <div className="mt-1 flex items-center gap-2">
                      <Badge variant="secondary">{barber.tier}</Badge>
                      {barber.ratingAvg != null && (
                        <span className="flex items-center gap-0.5 text-xs text-muted-foreground">
                          <Star className="size-3 fill-current" />
                          {barber.ratingAvg}
                        </span>
                      )}
                    </div>
                  </div>
                  {isSelected && <Check className="size-5 shrink-0 text-primary" />}
                </CardContent>
              </Card>
            );
          })}
        </div>

        {/* ── 2. CHỌN NGÀY (hiện sau khi chọn thợ) ── */}
        {selectedBarberId !== null && (
          <div className="space-y-3">
            <h2 className="text-sm font-medium text-muted-foreground">2. Chọn ngày</h2>
            <div className="flex gap-2 overflow-x-auto pb-1">
              {days.map((d) => {
                const key = dateKey(d);
                const isSelected = key === selectedDate;
                return (
                  <button
                    key={key}
                    onClick={() => {
                      setSelectedDate(key);
                      setSelectedTime(null); // đổi ngày -> reset giờ đã chọn
                    }}
                    className={cn(
                      "flex min-w-14 flex-col items-center rounded-lg border px-3 py-2 text-sm transition-colors",
                      isSelected ? "border-primary bg-primary text-primary-foreground" : "hover:bg-accent",
                    )}
                  >
                    <span className="text-xs">{WEEKDAYS[d.getDay()]}</span>
                    <span className="font-semibold">{d.getDate()}</span>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* ── 3. CHỌN GIỜ (hiện sau khi chọn ngày) ── */}
        {selectedDate !== null && (
          <div className="space-y-3">
            <h2 className="text-sm font-medium text-muted-foreground">3. Chọn giờ</h2>
            {slots.length === 0 ? (
              <p className="text-sm text-muted-foreground">Không có khung giờ phù hợp.</p>
            ) : (
              <div className="grid grid-cols-4 gap-2">
                {slots.map((t) => {
                  const isSelected = t === selectedTime;
                  return (
                    <button
                      key={t}
                      onClick={() => setSelectedTime(t)}
                      className={cn(
                        "rounded-lg border py-2 text-sm transition-colors",
                        isSelected ? "border-primary bg-primary text-primary-foreground" : "hover:bg-accent",
                      )}
                    >
                      {t}
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        )}
        {selectedTime !== null && (
          <div className="space-y-3">
            <h2 className="text-sm font-medium text-muted-foreground">4. Thông tin của bạn</h2>
            <div className="space-y-1.5">
              <Label htmlFor="name">Họ tên</Label>
              <Input id="name" value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="Nguyễn Văn A" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="phone">Số điện thoại</Label>
              <Input id="phone" type="tel" value={customerPhone} onChange={(e) => setCustomerPhone(e.target.value)} placeholder="09xx xxx xxx" />
            </div>
          </div>
        )}
      </section>

      <div className="sticky bottom-0 border-t bg-background p-4">
        <Button
          className="w-full"
          disabled={!ready || submitting}
          onClick={handleContinue}
        >
          {submitting ? "Đang đặt lịch…" : "Đặt lịch"}
        </Button>
      </div>
    </main>
  );
}
