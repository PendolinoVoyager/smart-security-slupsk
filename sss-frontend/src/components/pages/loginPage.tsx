import { TextField, Button, Typography, Box, Container, Link } from "@mui/material";
import { useState } from "react";

const LoginPage = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    return (
        <Box
            sx={{
                display: "flex",
                height: "100vh",
                backgroundColor: (theme) => theme.palette.primary.light,
            }}
        >
            {/* Sekcja lewa */}
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
                   Your{" "}
                    <Box
                        component="span"
                        sx={{
                            color: (theme) => theme.palette.secondary.main,
                            fontWeight: "bold",
                        }}
                    >AI
                    </Box>
                    -Powered Intercom! 👋
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
                    Transform your intercom experience with IoT technology and integrated{" "}
                    <Box
                        component="span"
                        sx={{
                            color: (theme) => theme.palette.secondary.main,
                            fontWeight: "bold",
                        }}
                    >
                        AI.{" "}
                    </Box>
                    Smart, secure, and effortless communication at your doorstep!
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
                    Welcome Back!
                </Typography>
                <Box
                    component="form"
                    onSubmit={(e) => {
                        e.preventDefault();
                        console.log("Email:", email, "Password:", password);
                    }}
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
                        autoFocus
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <TextField
                        margin="normal"
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
                        }}
                    >
                        Login Now
                    </Button>

                    <Typography variant="body2" sx={{ textAlign: "center", mt: 2 }}>
                        <Link href="#" underline="hover" sx={{ color: (theme) => theme.palette.primary.dark }}>
                            Forgot password? Click here
                        </Link>
                    </Typography>
                    <Typography variant="body2" sx={{ textAlign: "center", mt: 1 }}>
                        Don’t have an account?{" "}
                        <Link href="#" underline="hover" sx={{ color: (theme) => theme.palette.primary.dark }}>
                            Create a new account now
                        </Link>
                    </Typography>
                </Box>
            </Container>
        </Box>
    );
};

export default LoginPage;
