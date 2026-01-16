/**
 * This file contains all auth related API calls.
 */

import { AuthData, isAuthDataValid, ROLE } from "@/lib/auth";
import { ENDPOINTS } from "./config";
import { fetchSafe, HttpError } from "./utils";

export const requestLogin = async function (
  email: string,
  password: string
): Promise<AuthData | HttpError> {
  console.log(ENDPOINTS.AUTH.LOGIN);
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
};

export const requestRegister = async function ({
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
    console.error(res);
    return res;
  }

  return;
};
/**
 *
 * @returns Error - request failed OR null success
 */
export const requestActivateAccount = async function (
  email: string,
  activationToken: string
): Promise<Error | null> {
  const response = await fetchSafe<void>(ENDPOINTS.AUTH.ACTIVATE, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      activationToken,
    }),
  });

  if (response instanceof HttpError) {
    console.error("Account activation failed:", response.message);
    return response;
  }

  console.debug("Activation successful!");
  return null;
};

/**
 *
 * @returns Error - request failed OR null success
 */
export const requestResetPassword = async function (
  email: string
): Promise<Error | null> {
  const response = await fetchSafe<void>(ENDPOINTS.AUTH.RESET, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
    }),
  });

  if (response instanceof HttpError) {
    return response;
  }

  return null;
};
