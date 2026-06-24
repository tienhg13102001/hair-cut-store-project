import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiFetch, ApiException } from "@/lib/api";
import type { Service } from "@/lib/types";

// State của slice: data + cờ trạng thái async (chuẩn pattern thunk).
interface ServicesState {
  items: Service[];
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
}

const initialState: ServicesState = {
  items: [],
  status: "idle",
  error: null,
};

// createAsyncThunk tự sinh 3 action: pending / fulfilled / rejected.
// Tham số 1: tên action ("services/fetchAll").
// Tham số 2: hàm async trả về payload (hoặc rejectWithValue khi lỗi).
export const fetchServices = createAsyncThunk<
  Service[], // kiểu trả về (fulfilled payload)
  void, // kiểu tham số truyền vào (không cần)
  { rejectValue: string } // kiểu của rejectWithValue
>("services/fetchAll", async (_, { rejectWithValue }) => {
  try {
    return await apiFetch<Service[]>("/services");
  } catch (err) {
    const msg = err instanceof ApiException ? err.message : "Lỗi tải dịch vụ";
    return rejectWithValue(msg);
  }
});

const servicesSlice = createSlice({
  name: "services",
  initialState,
  reducers: {}, // chưa cần action đồng bộ
  // extraReducers: phản ứng với 3 action do thunk sinh ra.
  extraReducers: (builder) => {
    builder
      .addCase(fetchServices.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(fetchServices.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.items = action.payload;
      })
      .addCase(fetchServices.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload ?? "Lỗi không xác định";
      });
  },
});

export default servicesSlice.reducer;
