export class HttpError extends Error {
  constructor(public message: string, public status: number) {
    super(message);
    this.name = "HttpError";
  }
}

/**
 * Utility function preventing any throwing and forcing to check the type using TypeScript.
 */
export async function fetchSafe<T>(
  url: string,
  options?: RequestInit
): Promise<T | HttpError> {
  try {
    const res = await fetch(url, options);
    if (!res.ok) {
      const msg = await res.text();
      return new HttpError(
        msg || res.statusText || `Unknown error: code ${res.status}`,
        res.status ?? 500
      );
    }

    if (res.status === 204) return undefined as T; // No content
    return await res.json();
  } catch (e: unknown) {
    //@ts-expect-error
    return new HttpError(e?.message || "Unknown error", 500);
  }
}
