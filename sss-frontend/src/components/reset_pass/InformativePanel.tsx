import { Box, Typography } from "@mui/material";

const InformativePanel = () => (
    <Box
        sx={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
            pt: "6rem",
            alignItems: "center",
            color: (theme) => theme.palette.primary.contrastText,
            background: (theme) =>
                `linear-gradient(135deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main})`,
        }}
    >
        <Typography variant="h3" component="h1" sx={{ fontWeight: "bold" }}>
            Reset Your Password 🔒
        </Typography>
        <Typography
            variant="body1"
            sx={{
                fontSize: "1.2rem",
                mt: 4,
                textAlign: "center",
                maxWidth: "400px",
            }}
        >
            Forgot your password? No problem! Enter your email address in the
            form on the right, and we’ll send you instructions on how to reset
            your password.
        </Typography>
    </Box>
);

export default InformativePanel;
