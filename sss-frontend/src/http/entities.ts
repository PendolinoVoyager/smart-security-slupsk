import { ROLE } from "../authUtils";

export type User = {
  id: number;
  email: string;
  name: string;
  lastName: string;
  role: ROLE;
  createdAt: string;
};
