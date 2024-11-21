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
import {requestResetPassword} from "../../api/resetPasswordApi.ts";
import { useNavigate } from "react-router-dom";

const ResetPassword = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: "",
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

        const { email } = formData;

        if (!email) {
            setError("Please provide your email address.");
            return;
        }

        setIsLoading(true);

        const result = await requestResetPassword({ email });

        if (result instanceof Error) {
            setError(result.message);
        } else {
            navigate("/password-reset-confirmation");
        }
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

            {/* Formularz Resetowania Hasła */}
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
                            "Send Reset Link"
                        )}
                    </Button>
                </Box>
            </Container>
        </Box>
    );
};

export default ResetPassword;
