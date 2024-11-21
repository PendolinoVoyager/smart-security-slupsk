import { CssBaseline, ThemeProvider, useMediaQuery } from "@mui/material";
import * as themes from "./themes";
import "./App.css";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { FC } from "react";
import Root from "./components/pages/root";
import ErrorPage from "./components/pages/errorPage";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./store/authStore";
import RegisterPage from "./components/pages/registerPage.tsx";
import ActivationAccount from "./components/pages/activationAccount.tsx";
import ResetPassword from "./components/pages/resetPassword.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/register",
        element: <RegisterPage />,
      },
      {
        path: "/activation-account",
        element: <ActivationAccount />,
      },
      {
        path: "/reset-password",
        element: <ResetPassword />
      }
    ],
  },
]);

const App: FC = function () {
  const prefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)");
  const theme = prefersDarkMode ? themes.darkTheme : themes.lightTheme;

  const queryClient = new QueryClient();
  return (
    <AuthProvider>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <RouterProvider router={router} />
        </ThemeProvider>
      </QueryClientProvider>
    </AuthProvider>
  );
};

export default App;
