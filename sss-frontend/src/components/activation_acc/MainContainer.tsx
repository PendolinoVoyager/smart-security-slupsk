import { Box, Container, Typography } from "@mui/material";
import InformativePanel from "./InformativePanel";
import ActivationForm from "./ActivationForm";

const MainContainer = () => (
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
                Activate Your Account
            </Typography>
            <ActivationForm />
        </Container>
    </Box>
);

export default MainContainer;
