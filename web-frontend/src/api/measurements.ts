import { addCredentials } from "@/lib/auth/client";
import { ENDPOINTS } from "./config";
import { fetchSafe, HttpError } from "./utils";

export type MeasurementEntity = {
  id: number;
  measurementType: string;
  value: number;
  timestamp: string;
};
export type MeasurementResponsePaginated = {
  content: MeasurementEntity[];
  empty: boolean;
  firts: boolean;
  last: boolean;
  number: number;
  numberOfElements: 0;
  pageable: {
    pageNumber: number;
    pageSize: number;
    offset: number;
    paged: boolean;
  };
  size: number;
  sort: { sorted: boolean; empty: boolean; unsorted: boolean };
  totalElements: number;
  totalPages: number;
};

export const fetchMeasurementsByDevicePaginated = async function (
  token: string,
  deviceId: number
): Promise<MeasurementResponsePaginated | HttpError> {
  const res = await fetchSafe<MeasurementResponsePaginated>(
    `${ENDPOINTS.MEASUREMENTS.PAGINATED}${deviceId}?page=0&size=10`,
    addCredentials({}, token)
  );
  console.debug(res);
  return res;
};
