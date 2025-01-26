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
import ActivationAccountPage from "./components/pages/activationAccountPage.tsx";
import ResetPassword from "./components/pages/resetPasswordPage.tsx";
import DevicePage from "./components/pages/devicePage.tsx";
import DeviceDetails from "./components/pages/deviceDetails.tsx";
import { FlashProvider } from "./store/flashStore.tsx";
import AddDevicePage from "./components/pages/addDevicePage.tsx";
import NotificationsPage from "./components/pages/notificationsPage.tsx";
import StreamPreviewPage from "./components/pages/previewStream.tsx";

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
        element: <ActivationAccountPage />,
      },
      {
        path: "/reset-password",
        element: <ResetPassword />,
      },
      {
        path: "/devices",
        element: <DevicePage />,
      },
      {
        path: "/devices/:id",
        element: <DeviceDetails />,
      },

      {
        path: "/devices/add",
        element: <AddDevicePage />,
      },
      {
        path: "/notifications",
        element: <NotificationsPage />,
      },
      {
        path: "/stream-preview",
        element: <StreamPreviewPage />,
      },
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
          <FlashProvider>
            <CssBaseline />
            <RouterProvider router={router} />
          </FlashProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </AuthProvider>
  );
};

export default App;
