import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiFetch, ApiException } from "@/lib/api";
import type { Appointment, CreateAppointmentBody } from "@/lib/types";

interface AppointmentsState {
  submitting: boolean; // đang gửi POST?
  error: string | null;
  lastCreatedId: number | null; // id lịch vừa tạo (để màn xác nhận)
}

const initialState: AppointmentsState = {
  submitting: false,
  error: null,
  lastCreatedId: null,
};

// Thunk POST làm 2 việc NỐI TIẾP:
//  1) tạo lịch (POST /appointments)
//  2) gắn dịch vụ vào lịch vừa tạo (POST /appointments/{id}/services)
// Nhận 1 OBJECT tham số { body, serviceId } vì cần nhiều giá trị.
export const createBooking = createAsyncThunk<
  Appointment, // trả về: lịch đã tạo
  { body: CreateAppointmentBody; serviceId: number }, // tham số
  { rejectValue: string }
>(
  "appointments/createBooking",
  async ({ body, serviceId }, { rejectWithValue }) => {
    try {
      // POST 1: tạo lịch → backend trả 201 + lịch mới (có id).
      const created = await apiFetch<Appointment>("/appointments", {
        method: "POST",
        body: JSON.stringify(body),
      });

      // POST 2: gắn dịch vụ vào lịch vừa tạo — DÙNG created.id từ POST 1.
      await apiFetch(`/appointments/${created.id}/services`, {
        method: "POST",
        body: JSON.stringify({ serviceId }),
      });

      return created;
    } catch (err) {
      const msg = err instanceof ApiException ? err.message : "Lỗi tạo lịch";
      return rejectWithValue(msg);
    }
  },
);

const appointmentsSlice = createSlice({
  name: "appointments",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(createBooking.pending, (state) => {
        state.submitting = true;
        state.error = null;
      })
      .addCase(createBooking.fulfilled, (state, action) => {
        state.submitting = false;
        state.lastCreatedId = action.payload.id;
      })
      .addCase(createBooking.rejected, (state, action) => {
        state.submitting = false;
        state.error = action.payload ?? "Lỗi không xác định";
      });
  },
});

export default appointmentsSlice.reducer;
