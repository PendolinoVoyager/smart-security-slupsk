export const AUTH_COOKIE_NAME = "auth_data";

/**
 * JWT token's expiration time in seconds.
 */
export const TOKEN_EXPIRATION_TIME: number = 60 * 60 * 24 * 7;
export type AuthData = {
  role: ROLE;
  email: string;
  token: string;
};
export enum ROLE {
  USER = "USER",
  ADMIN = "ADMIN",
}
const ROLES_ARRAY = [ROLE.ADMIN, ROLE.USER];
/**
 * Helper function checking if authdata is valid - has role, email, token, isn't null etc.
 * @param authData
 */
export function isAuthDataValid(authData: AuthData): boolean {
  return !!(
    authData &&
    typeof authData == "object" &&
    ROLES_ARRAY.includes(authData?.role) &&
    authData?.email &&
    authData?.token
  );
}

export function parseAuthDataFromString(data: any): AuthData | undefined {
  if (!data) return undefined;
  // wrapping in try catch while parsing
  try {
    const authData = JSON.parse(data);
    // If authData isn't null and has role, email and token we can return it.
    if (isAuthDataValid(authData)) return authData;
  } catch {
    // pass and return undefined
  }
  return undefined;
}
