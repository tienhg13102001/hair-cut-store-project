import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiFetch, ApiException } from "@/lib/api";
import type { BarberProfile } from "@/lib/types";

interface BarbersState {
  items: BarberProfile[];
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
}

const initialState: BarbersState = {
  items: [],
  status: "idle",
  error: null,
};

// Tải thợ theo chi nhánh. Thunk này CÓ THAM SỐ (branchId: number) — khác mọi
// thunk trước (void). Tham số 1 của hàm async giờ là branchId (thay cho `_`).
export const fetchBarbersByBranch = createAsyncThunk<
  BarberProfile[], // kiểu trả về
  number, // [MỚI] kiểu THAM SỐ truyền vào: branchId
  { rejectValue: string }
>("barbers/fetchByBranch", async (branchId, { rejectWithValue }) => {
  try {
    return await apiFetch<BarberProfile[]>(
      `/barber-profiles?branchId=${branchId}`,
    );
  } catch (err) {
    const msg = err instanceof ApiException ? err.message : "Lỗi tải thợ";
    return rejectWithValue(msg);
  }
});

const barbersSlice = createSlice({
  name: "barbers",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchBarbersByBranch.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(fetchBarbersByBranch.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.items = action.payload;
      })
      .addCase(fetchBarbersByBranch.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload ?? "Lỗi không xác định";
      });
  },
});

export default barbersSlice.reducer;
