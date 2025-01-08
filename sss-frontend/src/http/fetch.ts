/**
 * Custom error class to signify an error that is directly related to an undesired status code when fetching.
 */
export class HttpError extends Error {
  public statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.name = this.constructor.name; // Set the name to the derived class's name
    this.statusCode = statusCode;
  }

  /**
   * Creates an appropriate HttpError subclass based on the HTTP response status providing a generic message.
   * @param res The response object from a fetch request.
   * @returns An instance of a specific HttpError subclass.
   */
  static async from_response(res: Response): Promise<HttpError> {
    const message = await res.text(); // Retrieve the error message from the response body

    let text = message;
    if (!text) {
      switch (res.status) {
        case 400:
          text = "A bad request has been sent to the server.";
          break;
        case 401:
          text = "You have to be logged in to access this resource.";
          break;
        case 403:
          text = "You don't have permission to this resource.";
          break;
        case 404:
          text = "The resource you're looking for doesn't exist.";
          break;
        case 500:
          text =
            "Internal server error. Please try again later or concat IT admin.";
          break;
        default:
          text = "Unexpected server error";
          break;
      }
    }
    return new HttpError(text, res.status);
  }
}

export async function fetchSafe<T>(
  uri: string,
  headers?: RequestInit
): Promise<T | HttpError> {
  try {
    const res = await fetch(uri, headers);
    if (!res.ok) throw HttpError.from_response(res);

    const data: T = await res.json();
    if (data == null) {
      return new HttpError("missing body", 500);
    }
    return data;
  } catch (err) {
    return err as HttpError;
  }
}
