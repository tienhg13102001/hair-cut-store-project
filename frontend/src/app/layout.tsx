import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AppProviders } from "@/components/providers/AppProviders";
import { BottomNav } from "@/components/BottomNav";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Salon Booking",
  description: "Đặt lịch cắt tóc",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="vi"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full bg-muted">
        <AppProviders>
          {/* Khung mobile-first: căn giữa, giới hạn ~ chiều rộng điện thoại */}
          <div className="mx-auto flex min-h-dvh w-full max-w-md flex-col bg-background shadow-sm">
            {children}
            <BottomNav />
          </div>
        </AppProviders>
      </body>
    </html>
  );
}
