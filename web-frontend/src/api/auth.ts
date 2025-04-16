/**
 * This file contains all auth related functions and types.
 */

import { ENDPOINTS } from "./config";
import { fetchSafe, HttpError } from "./utils";

const LOCALSTORAGE_AUTHDATA_NAME = "auth_data";

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

export const CALLERS = {
  requestLogin: async function (
    email: string,
    password: string
  ): Promise<AuthData | HttpError> {
    const res = await fetchSafe<AuthData>(ENDPOINTS.AUTH.LOGIN, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
      }),
    });
    if (res instanceof HttpError) {
      return res;
    }

    if (res === undefined || !isAuthDataValid(res))
      return new HttpError(
        "Something went wrong with the authentication response",
        500
      );

    return res!;
  },

  requestRegister: async function ({
    name,
    last_name,
    email,
    password,
  }: {
    name: string;
    last_name: string;
    email: string;
    password: string;
  }): Promise<void | HttpError> {
    console.log(name, last_name, email, password);
    const res = await fetchSafe<void>(ENDPOINTS.AUTH.REGISTER, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name,
        last_name,
        email,
        password,
      }),
    });

    if (res instanceof HttpError) {
      console.log(res);
      return res;
    }

    return;
  },
};

///////////////////
// UTILS BLOCK
////////////////////

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
export function getToken(): string | undefined {
  return getAuthData()?.token;
}

export function getAuthData(): AuthData | undefined {
  const data = localStorage.getItem(LOCALSTORAGE_AUTHDATA_NAME);
  if (!data) return undefined;
  // wrapping in try catch while parsing
  try {
    const authData = JSON.parse(data);
    // If authData isn't null and has role, email and token we can return it.
    if (isAuthDataValid(authData)) return authData;
    else
      throw new Error(
        "Invalid Auth Data in localstorage. This incident will be reported."
      );
  } catch {
    // pass and return undefined
  }
  return undefined;
}
/**
 * ## Might throw with bad authData!
 */
export function setAuthData(authData: AuthData) {
  if (isAuthDataValid(authData)) {
    localStorage.setItem(LOCALSTORAGE_AUTHDATA_NAME, JSON.stringify(authData));
    return undefined;
  }
  return new Error(
    `Cannot set provided authData to local storage:\nGot ${authData}`
  );
}

export function clearAuthData() {
  localStorage.removeItem(LOCALSTORAGE_AUTHDATA_NAME);
}

/**
 * Function attaching proper credentials to a request using authUtils.
 * Mutates requestOptions
 * @param requestOptions Request options like in "fetch" 2nd parameter
 */
export function addCredentials(requestOptions: RequestInit) {
  const token = getToken();
  if (requestOptions.headers == undefined) {
    requestOptions.headers = {} as HeadersInit;
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  //@ts-expect-error
  requestOptions.headers["Authorization"] = `Bearer ${token}`;
  return requestOptions;
}
