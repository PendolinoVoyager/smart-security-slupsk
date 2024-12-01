import { TextField, Button, Box, CircularProgress, Alert, Typography, Link } from "@mui/material";
import { useState, useContext } from "react";
import { AuthContext } from "../../store/authStore";
import { useNavigate } from "react-router-dom";

const LoginForm = ({ onLoginSuccess }: { onLoginSuccess: () => void }) => {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsLoading(true);

        const result = await login(email, password);

        setIsLoading(false);

        if (result instanceof Error) {
            setError(result.message);
            return;
        }

        onLoginSuccess();
    };

    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{
                display: "flex",
                flexDirection: "column",
                gap: 2,
                width: "300px",
                padding: 2,
                textAlign: "center", // Wyśrodkowanie napisu
            }}
        >
            {/* Nagłówek formularza */}
            <Typography variant="h5" sx={{ mb: 2 }}>
                Login
            </Typography>

            <TextField
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />
            <TextField
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            {error && <Alert severity="error">{error}</Alert>}
            <Button
                type="submit"
                fullWidth
                variant="contained"
                disabled={isLoading}
            >
                {isLoading ? <CircularProgress size={24} sx={{ color: "white" }} /> : "Login"}
            </Button>

            <Box sx={{ textAlign: "center", mt: 2 }}>
                <Typography variant="body2" sx={{ mb: 1 }}>
                    <Link
                        href="#"
                        onClick={() => navigate("/reset-password")}
                        underline="hover"
                        sx={{ cursor: "pointer" }}
                    >
                        Forgotten Password?
                    </Link>
                </Typography>
                <Typography variant="body2">
                    Don’t have an account?{" "}
                    <Link
                        href="#"
                        onClick={() => navigate("/register")}
                        underline="hover"
                        sx={{ cursor: "pointer" }}
                    >
                        Create one now
                    </Link>
                </Typography>
            </Box>
        </Box>
    );
};

export default LoginForm;
