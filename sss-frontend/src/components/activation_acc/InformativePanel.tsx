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
            Activate your account! 👏
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
            To complete your registration, activate your account using the form
            on the right. Enter your email and the activation token you received
            via email.
        </Typography>
    </Box>
);

export default InformativePanel;
