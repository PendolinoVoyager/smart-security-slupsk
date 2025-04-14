import { fetchSafe, HttpError } from "../http/fetch.ts";

const AUTH_PROVIDER_URL = "http://192.168.10.47:8080";
const REGISTER_URL = `${AUTH_PROVIDER_URL}/api/v1/auth/register`;

export enum ROLE {
    DEVICE = "DEVICE",
    USER = "USER",
    ADMIN = "ADMIN",
}

export async function requestRegister({
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
    const res = await fetchSafe<void>(REGISTER_URL, {
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
}
