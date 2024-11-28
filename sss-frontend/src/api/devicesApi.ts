import {getToken} from "../authUtils.ts";
import {fetchSafe, HttpError} from "../http/fetch.ts";

const API_URL = "http://localhost:8080";

export async function fetchDevices(): Promise<{ id: string }[] | undefined> {
    try {
        const token = getToken();
        if (!token) {
            console.error("No token found. User might not be authenticated.");
            return undefined;
        }

        const response = await fetchSafe<{ id: string }[]>(`${API_URL}/api/v1/device/`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        console.log(response);

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
