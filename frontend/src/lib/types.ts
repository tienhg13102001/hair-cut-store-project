// Kiểu dữ liệu khớp với entity/DTO trả về từ backend Spring Boot.
// Chỉ khai báo field FE cần dùng — không cần map 100% cột DB.

export type Tier = "JUNIOR" | "SENIOR" | "MASTER";

export type AppointmentStatus = "PENDING" | "CONFIRMED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED" | "NO_SHOW";

export type InvoiceStatus = "DRAFT" | "PAID" | "CANCELLED";

export interface Branch {
  id: number;
  name: string;
  address: string;
  phone: string | null;
  openingAt: string; // "08:00:00"
  closingAt: string; // "20:00:00"
  active: boolean;
}

// Backend trả raw entity (lồng user + branch + rác hibernate). Ở FE chỉ khai
// báo field cần dùng — bỏ qua phần thừa.
export interface BarberProfile {
  id: number;
  tier: Tier;
  bio: string | null;
  active: boolean;
  ratingAvg: number | null;
  yearsExp: number | null;
  user: { fullName: string; phone: string | null };
  branch: { id: number };
}

export interface Service {
  id: number;
  name: string;
  description: string | null;
  durationMin: number;
  active: boolean;
}

// Trùng với InvoiceResponse DTO ở backend (đã làm buổi trước).
export interface InvoiceItemResponse {
  id: number;
  serviceName: string;
  priceVnd: number;
}

export interface InvoiceResponse {
  id: number;
  appointmentId: number;
  totalVnd: number;
  status: InvoiceStatus;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
  items: InvoiceItemResponse[];
}

// Body lỗi chuẩn của backend (ErrorResponse.of).
export interface ApiError {
  code: string;
  message: string;
  field?: string;
}

// Body gửi lên khi tạo lịch (khớp CreateAppointmentRequest của backend).
// customerId tùy chọn (khách vãng lai không có) → mode walk-in dùng name+phone.
export interface CreateAppointmentBody {
  branchId: number;
  barberId: number;
  walkInName: string;
  walkInPhone: string;
  startAt: string; // ISO, vd "2026-06-25T01:00:00.000Z"
  endAt: string;
  note?: string;
}

// Lịch trả về (mình chỉ cần id để gắn dịch vụ + điều hướng).
export interface Appointment {
  id: number;
  status: AppointmentStatus;
}
