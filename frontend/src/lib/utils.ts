import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

// cn = gộp class có điều kiện (clsx) + xử lý xung đột Tailwind (twMerge).
// VD: cn("p-2", isActive && "bg-blue-500", "p-4") -> "bg-blue-500 p-4"
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
