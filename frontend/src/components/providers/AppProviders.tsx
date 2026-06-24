"use client";

import { useRef, type ReactNode } from "react";
import { Provider } from "react-redux";
import { Toaster } from "@/components/ui/sonner";
import { makeStore, type AppStore } from "@/store/store";

// "use client" vì Redux Provider dùng React Context (chỉ chạy ở client).
// Dùng useRef để store CHỈ tạo 1 lần cho cả vòng đời app (không tạo lại mỗi render).
export function AppProviders({ children }: { children: ReactNode }) {
  const storeRef = useRef<AppStore | null>(null);
  if (!storeRef.current) {
    storeRef.current = makeStore();
  }

  return (
    <Provider store={storeRef.current}>
      {children}
      <Toaster position="top-center" richColors />
    </Provider>
  );
}
