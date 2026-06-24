import { useDispatch, useSelector, useStore } from "react-redux";
import type { AppDispatch, AppStore, RootState } from "./store";

// Hook có sẵn type — KHÔNG dùng useDispatch/useSelector trần nữa.
// useAppDispatch hiểu thunk; useAppSelector biết kiểu RootState.
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
export const useAppStore = useStore.withTypes<AppStore>();
