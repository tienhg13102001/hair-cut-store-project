"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Home, CalendarDays, User } from "lucide-react";
import { cn } from "@/lib/utils";

// Data-driven: khai báo các tab ở 1 nơi, render bằng .map().
// Muốn thêm tab sau này → chỉ thêm 1 dòng vào mảng này.
const NAV_ITEMS = [
  { href: "/", label: "Trang chủ", icon: Home },
  { href: "/appointments", label: "Lịch của tôi", icon: CalendarDays },
  { href: "/account", label: "Tài khoản", icon: User },
];

export function BottomNav() {
  // Đường dẫn hiện tại (vd "/appointments") để biết tab nào đang active.
  const pathname = usePathname();

  return (
    <nav className="sticky bottom-0 z-10 border-t bg-background">
      <ul className="flex items-stretch">
        {NAV_ITEMS.map((item) => {
          // Tab "/" chỉ active khi đứng đúng trang chủ (vì URL nào cũng bắt đầu
          // bằng "/"). Các tab khác active khi pathname bắt đầu bằng href của tab
          // → khớp luôn cả route con (vd /appointments/123).
          const isActive =
            item.href === "/"
              ? pathname === "/"
              : pathname.startsWith(item.href);

          // Icon là một component → gán vào biến viết hoa để render <Icon/>.
          const Icon = item.icon;

          return (
            <li key={item.href} className="flex-1">
              <Link
                href={item.href}
                className={cn(
                  "flex flex-col items-center gap-1 py-2 text-xs transition-colors",
                  isActive
                    ? "text-primary"
                    : "text-muted-foreground hover:text-foreground",
                )}
              >
                <Icon className="size-5" />
                <span>{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
