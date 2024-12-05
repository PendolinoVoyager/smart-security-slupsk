import { fetchSafe, HttpError } from "../http/fetch.ts";

const AUTH_PROVIDER_URL = "http://localhost:8080";
const RESET_PASSWORD_URL = `${AUTH_PROVIDER_URL}/api/v1/auth/reset-password`;

export async function requestResetPassword(
    {
        email,
}: {
    email: string;
}): Promise<void | HttpError> {
    try {
        const response = await fetchSafe<void>(RESET_PASSWORD_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ mail: email }),
        });

        if (response instanceof HttpError) {
            console.error("Reset password failed:", response.message);
            return response;
        }

        console.log("Password reset link sent successfully!");
        return;
    } catch (error) {
        console.error("Unexpected error occurred:", error);
        throw new Error("Failed to reset password. Please try again later.");
    }
}
