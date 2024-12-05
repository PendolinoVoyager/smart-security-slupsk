import React, { useState } from "react";
import {
    TextField,
    Button,
    Box,
    CircularProgress,
    Alert,
} from "@mui/material";
import { requestResetPassword } from "../../api/resetPasswordApi.ts";
import { useNavigate } from "react-router-dom";

const ResetPasswordForm = () => {
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

        setIsLoading(false);

        if (result instanceof Error) {
            setError(result.message);
        } else {
            navigate("/password-reset-confirmation");
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
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
    );
};

export default ResetPasswordForm;
