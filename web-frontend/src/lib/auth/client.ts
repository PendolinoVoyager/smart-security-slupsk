import { AUTH_COOKIE_NAME, AuthData, isAuthDataValid } from "../auth";
import Cookies from "js-cookie";

export function getToken(): string | undefined {
  return getAuthData()?.token;
}

export function getAuthData(): AuthData | undefined {
  const data = Cookies.get(AUTH_COOKIE_NAME);
  if (!data) return undefined;
  try {
    const authData = JSON.parse(data);
    if (isAuthDataValid(authData)) return authData;
    else
      throw new Error(
        "Invalid Auth Data in cookie. This incident will be reported."
      );
  } catch {
    // Could log here if desired
  }
  return undefined;
}

/**
 * ## Might throw with bad authData!
 */
export function setAuthData(authData: AuthData) {
  if (isAuthDataValid(authData)) {
    Cookies.set(AUTH_COOKIE_NAME, JSON.stringify(authData), {
      expires: 7, // 7 days
      secure: true,
      sameSite: "Strict",
    });
    return undefined;
  }
  return new Error(
    `Cannot set provided authData to cookie:\nGot ${JSON.stringify(authData)}`
  );
}

export function clearAuthData() {
  Cookies.remove(AUTH_COOKIE_NAME);
}
/**
 * Function attaching proper credentials to a request using authUtils.
 * Mutates requestOptions
 * @param requestOptions Request options like in "fetch" 2nd parameter
 */
export function addCredentials(
  requestOptions: RequestInit,
  tokenOverride?: string
) {
  const token = getToken();
  if (requestOptions.headers == undefined) {
    requestOptions.headers = {} as HeadersInit;
  }

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  //@ts-expect-error
  requestOptions.headers["Authorization"] = `Bearer ${tokenOverride || token}`;
  return requestOptions;
}
