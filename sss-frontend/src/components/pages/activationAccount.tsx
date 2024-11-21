import React, { useState } from "react";
import {
    TextField,
    Button,
    Typography,
    Box,
    Container,
    CircularProgress,
    Alert,
} from "@mui/material";

const ActivationAccount = () => {
    const [formData, setFormData] = useState({
        email: "",
        activationToken: "",
    });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({ ...prevData, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        const { email, activationToken } = formData;

        if (!email || !activationToken) {
            setError("Please provide both email and activation token.");
            return;
        }

        setIsLoading(true);

        console.log("dziala");
    };

    return (
        <Box
            sx={{
                display: "flex",
                height: "100vh",
                backgroundColor: (theme) => theme.palette.primary.light,
            }}
        >
            {/* Lewe Pole Informacyjne */}
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

            {/* Formularz Aktywacji */}
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
                <Box
                    component="form"
                    onSubmit={handleSubmit}
                    sx={{ width: "100%" }}
                >
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Email Address"
                        name="email"
                        autoComplete="email"
                        value={formData.email}
                        onChange={handleChange}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="activationToken"
                        label="Activation Token"
                        name="activationToken"
                        value={formData.activationToken}
                        onChange={handleChange}
                    />

                    {error && <Alert severity="error">{error}</Alert>}

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{
                            mt: 3,
                            mb: 2,
                            backgroundColor: (theme) => theme.palette.primary.main,
                            "&:hover": {
                                backgroundColor: (theme) => theme.palette.primary.dark,
                            },
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                        }}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <CircularProgress size={24} sx={{ color: "white" }} />
                        ) : (
                            "Activate Account"
                        )}
                    </Button>
                </Box>
            </Container>
        </Box>
    );
};

export default ActivationAccount;
