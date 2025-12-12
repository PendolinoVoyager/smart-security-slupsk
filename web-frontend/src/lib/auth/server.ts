"use server";
import { cookies } from "next/headers";
import {
  AUTH_COOKIE_NAME,
  AuthData,
  parseAuthDataFromString,
  TOKEN_EXPIRATION_TIME,
} from ".";
import { redirect } from "next/navigation";
import {
  requestActivateAccount,
  requestLogin,
  requestRegister,
  requestResetPassword,
} from "@/api/auth";
import { z } from "zod";
import { ResponseCookies } from "next/dist/compiled/@edge-runtime/cookies";

export async function getAuthData(): Promise<AuthData | undefined> {
  const cookieStore = await cookies();
  const authData = cookieStore.get(AUTH_COOKIE_NAME)?.value;
  return parseAuthDataFromString(authData);
}

export async function logoutAction() {
  (await cookies()).delete(AUTH_COOKIE_NAME);
}

export async function loginAction(_currentState: any, form: FormData) {
  console.log(form);
  const email = form?.get("email")?.toString() ?? "";
  const password = form?.get("password")?.toString() ?? "";
  const result = await requestLogin(email, password);
  console.log(result);

  if (result instanceof Error) {
    console.log("were returning false");
    return { success: false, message: `Failed to login: ${result.message}` };
  }
  const c = await cookies();
  ResponseCookies;
  c.set(AUTH_COOKIE_NAME, JSON.stringify(result), {
    httpOnly: true,
    secure: true,
    expires: new Date(Date.now() + TOKEN_EXPIRATION_TIME * 1000),
    sameSite: "lax",
    path: "/",
  });
  (await cookies()).set(AUTH_COOKIE_NAME, JSON.stringify(result));
  return { success: true };
}

const formSchema = z.object({
  name: z.string().min(1, "Name is required."),
  last_name: z.string().min(1, "Last name is required."),
  email: z.string().email("Invalid email address."),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters long.")
    .regex(/[A-Z]/, "Password must contain at least one capital letter.")
    .regex(
      /[^A-Za-z0-9]/,
      "Password must contain at least one special character."
    ),
  confirmPassword: z.string(),
});

export async function registerAction(
  currentState: any,
  formData: FormData
): Promise<{ success: boolean; message: string }> {
  const data = {
    name: formData.get("name") as string,
    last_name: formData.get("last_name") as string,
    email: formData.get("email") as string,
    password: formData.get("password") as string,
    confirmPassword: formData.get("confirmPassword") as string,
  };

  // Validate
  const parsed = formSchema.safeParse(data);
  if (!parsed.success) {
    const firstError = parsed.error.errors[0]?.message || "Invalid form data.";
    return { success: false, message: firstError };
  }

  const { password, confirmPassword } = data;

  if (password !== confirmPassword) {
    return { success: false, message: "Passwords do not match." };
  }

  // At this point, it's valid
  const payload = {
    name: data.name,
    last_name: data.last_name,
    email: data.email,
    password: data.password,
  };

  try {
    const result = await requestRegister(payload); // <-- You need to import this
    if (result instanceof Error) {
      return { success: false, message: result.message };
    }
    return { success: true, message: "Registration successful." };
  } catch (error: any) {
    return { success: false, message: error.message || "An error occurred." };
  }
}
export async function activateAccountAction(
  currentState: any,
  formData: FormData
): Promise<{ success: boolean; message: string }> {
  const data = {
    email: formData.get("email") as string,
    activationToken: formData.get("activationToken") as string,
  };
  const result = await requestActivateAccount(data.email, data.activationToken);
  if (result instanceof Error) {
    return {
      success: false,
      message: `Failed to activate your account: ${
        result.message || "unknown issue"
      }`,
    };
  }
  return { success: true, message: "Successfully activated your account!" };
}

export async function resetPasswordAction(
  currentState: any,
  formData: FormData
): Promise<{ success: boolean; message: string }> {
  const data = {
    email: formData.get("email") as string,
  };
  const result = await requestResetPassword(data.email);
  if (result instanceof Error) {
    return {
      success: false,
      message: `Failed to reset your password: ${
        result.message || "unknown issue"
      }`,
    };
  }
  return { success: true, message: "Successfully activated your account!" };
}
