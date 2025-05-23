import { createContext, useState, useEffect, ReactNode } from "react";
import {
  ROLE,
  isAuthDataValid,
  getAuthData,
  setAuthData,
  clearAuthData,
  requestLogin,
} from "../authUtils";
import { HttpError } from "../http/fetch";

// Defining the shape of the Auth context state
type AuthContextType = {
  loggedIn: boolean;
  role: ROLE | undefined;
  email: string | undefined;
  getToken(): string | undefined;
  login: (
    email: string,
    password: string
  ) => Promise<boolean | undefined | HttpError>;
  logout: () => void;
};

// Creating the context with default values
export const AuthContext = createContext<AuthContextType>({
  loggedIn: false,
  role: undefined,
  email: undefined,
  getToken: () => undefined,
  login: async () => false,
  logout: () => undefined,
});

// Provider component that wraps your app and makes auth data available to any child component
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const prevAuthData = getAuthData();
  const [role, setRole] = useState<ROLE | undefined>(prevAuthData?.role);
  const [email, setEmail] = useState<string>(prevAuthData?.email ?? "");
  const [loggedIn, setLoggedIn] = useState<boolean>(!!prevAuthData?.role);
  useEffect(() => {
    const storedAuthData = getAuthData();

    if (storedAuthData && isAuthDataValid(storedAuthData)) {
      setRole(storedAuthData.role);
      setEmail(storedAuthData.email);
      setLoggedIn(true);
    }
  }, []);
  const login = async (
    email: string,
    password: string
  ): Promise<boolean | HttpError> => {
    const authData = await requestLogin(email, password);
    if (authData instanceof HttpError) {
      return authData;
    }
    if (authData && isAuthDataValid(authData)) {
      setRole(authData.role);
      setEmail(authData.email);
      setLoggedIn(true);
      setAuthData(authData);
    }
    return true;
  };

  const logout = () => {
    setRole(undefined);
    setEmail("");
    setLoggedIn(false);
    clearAuthData();


  };
  const getToken = () => {
    return getAuthData()?.token;
  };

  return (
    <AuthContext.Provider
      value={{ loggedIn, role, email, login, logout, getToken }}
    >
      {children}
    </AuthContext.Provider>
  );
};
