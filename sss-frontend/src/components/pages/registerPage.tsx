import React, { useState } from "react";
import {
    TextField,
    Button,
    Typography,
    Box,
    Container,
    CircularProgress,
    Alert,
    MenuItem,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { requestRegister, ROLE } from "../../api/authApi.ts";

const RegistrationPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: "",
        last_name: "",
        email: "",
        role: ROLE.USER,
        password: "",
        confirmPassword: "",
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

        const { password, confirmPassword, ...registrationData } = formData;

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setIsLoading(true);

        const result = await requestRegister({
            ...registrationData,
            password,
        });

        setIsLoading(false);

        if (result instanceof Error) {
            setError(result.message);
        } else {

            navigate("/login"); // TODO: Przekierowac na strone aktywacji konta ale to jutro
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
                    Join Our{" "}
                    <Box
                        component="span"
                        sx={{
                            color: (theme) => theme.palette.secondary.main,
                            fontWeight: "bold",
                        }}
                    >
                        AI
                    </Box>
                    -Powered Community! 🎉
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
                    Register today to experience seamless communication with the power of{" "}
                    <Box
                        component="span"
                        sx={{
                            color: (theme) => theme.palette.secondary.main,
                            fontWeight: "bold",
                        }}
                    >
                        AI
                    </Box>{" "}
                    at your fingertips.
                </Typography>
            </Box>

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
                <Box
                    component="form"
                    onSubmit={handleSubmit}
                    sx={{ width: "100%" }}
                >
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="name"
                        label="First Name"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="last_name"
                        label="Last Name"
                        name="last_name"
                        value={formData.last_name}
                        onChange={handleChange}
                    />
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
                        select
                        margin="normal"
                        required
                        fullWidth
                        id="role"
                        label="Role"
                        name="role"
                        value={formData.role}
                        onChange={handleChange}
                    >
                        <MenuItem value={ROLE.USER}>User</MenuItem>
                        <MenuItem value={ROLE.ADMIN}>Admin</MenuItem>
                    </TextField>
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        autoComplete="new-password"
                        value={formData.password}
                        onChange={handleChange}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="confirmPassword"
                        label="Confirm Password"
                        type="password"
                        id="confirmPassword"
                        autoComplete="new-password"
                        value={formData.confirmPassword}
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
                            "Register Now"
                        )}
                    </Button>
                </Box>
            </Container>
        </Box>
    );
};

export default RegistrationPage;
