import { configureStore } from "@reduxjs/toolkit";
import servicesReducer from "./slices/servicesSlice";
import branchesReducer from "./slices/branchesSlice";
import barbersReducer from "./slices/barbersSlice";
import appointmentsReducer from "./slices/appointmentsSlice";

// makeStore = factory tạo store mới mỗi request (chuẩn cho Next.js SSR —
// tránh share state giữa các user). Client chỉ tạo 1 lần.
export const makeStore = () =>
  configureStore({
    reducer: {
      services: servicesReducer,
      branches: branchesReducer,
      barbers: barbersReducer,
      appointments: appointmentsReducer,
      // thêm slice mới ở đây: invoice...
    },
  });

export type AppStore = ReturnType<typeof makeStore>;
// RootState = kiểu của toàn bộ state cây (suy ra tự động từ reducer).
export type RootState = ReturnType<AppStore["getState"]>;
// AppDispatch = kiểu dispatch (hiểu được thunk).
export type AppDispatch = AppStore["dispatch"];
