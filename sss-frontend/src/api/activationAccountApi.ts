import { fetchSafe, HttpError } from "../http/fetch.ts";

const AUTH_PROVIDER_URL = "http://192.168.10.47:8080";
const ACTIVATE_ACCOUNT_URL = `${AUTH_PROVIDER_URL}/api/v1/activation-token/verify`;

export async function requestActivationAccount(
    {
       email,
       activationToken,
}: {
    email: string;
    activationToken: string;
}): Promise<void | HttpError> {
    try {
        const response = await fetchSafe<void>(ACTIVATE_ACCOUNT_URL, {
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
            console.error("Activation failed:", response.message);
            return response;
        }

        console.log("Activation successful!");
        return;
    } catch (error) {
        console.error("Unexpected error occurred:", error);
        throw new Error("Failed to activate account. Please try again later.");
    }
}
