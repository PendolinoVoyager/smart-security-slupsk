import React, { useState } from "react";
import {
    TextField,
    Button,
    Box,
    CircularProgress,
    Alert,
} from "@mui/material";
import { requestActivationAccount } from "../../api/activationAccountApi.ts";
import { useNavigate } from "react-router-dom";

const ActivationForm = () => {
    const navigate = useNavigate();
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

        const result = await requestActivationAccount({ email, activationToken });

        if (result instanceof Error) {
            setError(result.message);
        } else {
            navigate("/");
        }

        setIsLoading(false);
    };

    return (
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
    );
};

export default ActivationForm;
