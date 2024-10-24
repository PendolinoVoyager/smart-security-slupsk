import { createTheme } from "@mui/material";

export const darkTheme = createTheme({
  colorSchemes: {
    dark: true,
  },

  palette: {
    mode: "dark",
    primary: {
      main: "#9d4edd",
      light: "#c77dff",
      dark: "#7f2fc5",
      contrastText: "#fff",
    },
    secondary: {
      main: "#fae588",
      light: "#fcefb4",
      dark: "#f9dc5c",
    },

    background: {
      default: "#121212", // Dark background color
      paper: "#1e1e1e", // Paper component background
    },
    text: {
      primary: "#fff", // Ensure primary text is white in dark mode
      secondary: "#b0b0b0", // Lighter text color for secondary text
    },
  },
});

export const lightTheme = createTheme({
  palette: {
    primary: {
      main: "#c77dff",
      light: "#e7c6ff",
      dark: "#9d4edd",
      contrastText: "#fff",
    },

    secondary: {
      main: "#efe809",
      light: "#efcd22",
      dark: "#e9cc4c",
      contrastText: "#444444",
    },
    background: {
      default: "#fefefe",
      paper: "#efefef",
    },
  },
});
