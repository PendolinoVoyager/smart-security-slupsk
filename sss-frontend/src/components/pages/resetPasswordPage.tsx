import { Box, Container, Typography } from "@mui/material";
import InformativePanel from "../reset_pass/InformativePanel";
import ResetPasswordForm from "../reset_pass/RessetPasswordForm.tsx";

const ResetPasswordPage = () => (
    <Box
        sx={{
            display: "flex",
            height: "100vh",
            backgroundColor: (theme) => theme.palette.primary.light,
        }}
    >
        <InformativePanel />

        <Container
            maxWidth="xs"
            sx={{
                flex: 1,
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                backgroundColor: "#fff",
            }}
        >
            <Typography
                variant="h5"
                component="h2"
                sx={{ fontWeight: "bold", mb: 3 }}
            >
                Reset Password
            </Typography>
            <ResetPasswordForm />
        </Container>
    </Box>
);

export default ResetPasswordPage;
