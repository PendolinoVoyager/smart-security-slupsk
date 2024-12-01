import { addCredentials } from "../authUtils.ts";
import { fetchSafe, HttpError } from "../http/fetch.ts";

const API_URL = "http://localhost:8080";

export async function fetchDevices(): Promise<{ id: string }[] | undefined> {
    try {
        const requestOptions: RequestInit = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        };

        addCredentials(requestOptions);

        const response = await fetchSafe<{ id: string }[]>(`${API_URL}/api/v1/device/`, requestOptions);

        if (response instanceof HttpError) {
            console.error("Fetching devices failed:", response.message);
            return undefined;
        }

        return response;
    } catch (error) {
        console.error("Unexpected error occurred:", error);
        return undefined;
    }
}
