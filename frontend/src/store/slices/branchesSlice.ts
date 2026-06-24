import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiFetch, ApiException } from "@/lib/api";
import type { Branch } from "@/lib/types"; // [ĐỔI 1] Service -> Branch

// State của slice: data + cờ trạng thái async (chuẩn pattern thunk).
interface BranchesState {
  items: Branch[]; // [ĐỔI 2] Service[] -> Branch[]
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
}

const initialState: BranchesState = {
  items: [],
  status: "idle",
  error: null,
};

// createAsyncThunk tự sinh 3 action: pending / fulfilled / rejected.
export const fetchBranches = createAsyncThunk<
  Branch[], // kiểu trả về (fulfilled payload)
  void, // không cần tham số
  { rejectValue: string }
>("branches/fetchAll", async (_, { rejectWithValue }) => {
  try {
    return await apiFetch<Branch[]>("/branches"); // [ĐỔI 3] endpoint /branches
  } catch (err) {
    const msg = err instanceof ApiException ? err.message : "Lỗi tải chi nhánh";
    return rejectWithValue(msg);
  }
});

const branchesSlice = createSlice({
  name: "branches", // [ĐỔI 4] tên slice
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchBranches.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(fetchBranches.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.items = action.payload;
      })
      .addCase(fetchBranches.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload ?? "Lỗi không xác định";
      });
  },
});

export default branchesSlice.reducer;
