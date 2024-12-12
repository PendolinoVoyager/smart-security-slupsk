import { Box, Container, Typography } from "@mui/material";
import InformativePanel from "../register/InformativePanel.tsx";
import RegistrationForm from "../register/RegistrationForm.tsx";
import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {getAuthData} from "../../authUtils.ts";

const RegisterPage = () => {
    const navigate = useNavigate();
    useEffect(() => {
        if (getAuthData()?.email) {
            navigate("/");
        }
    }, [navigate]);
    return (<Box
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
                Create Your Account
            </Typography>
            <RegistrationForm />
        </Container>
    </Box>
);
}

export default RegisterPage;
