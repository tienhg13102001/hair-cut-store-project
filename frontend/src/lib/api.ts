import type { ApiError } from "./types";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

// Lỗi có cấu trúc để slice/thunk bắt được code + message từ backend.
export class ApiException extends Error {
  code: string;
  status: number;
  field?: string;

  constructor(status: number, body: ApiError) {
    super(body.message);
    this.name = "ApiException";
    this.status = status;
    this.code = body.code;
    this.field = body.field;
  }
}

// Wrapper fetch dùng chung:
// - tự ghép BASE_URL + path
// - set Content-Type JSON
// - parse JSON; nếu !ok -> ném ApiException (kèm code backend)
export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  });

  // 204 No Content -> không có body để parse
  if (res.status === 204) return undefined as T;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const body: ApiError =
      data && typeof data === "object" && "code" in data
        ? (data as ApiError)
        : { code: "UNKNOWN", message: `HTTP ${res.status}` };
    throw new ApiException(res.status, body);
  }

  return data as T;
}
