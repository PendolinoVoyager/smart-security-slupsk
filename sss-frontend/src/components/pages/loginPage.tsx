// import { TextField, Button, Typography, Box, Container, Link, CircularProgress, Alert } from "@mui/material";
// import { useState, useContext } from "react";
// import { useNavigate } from "react-router-dom";
// import { AuthContext } from "../../store/authStore";
//
// const LoginPage = () => {
//     const { login } = useContext(AuthContext);
//     const [email, setEmail] = useState("");
//     const [password, setPassword] = useState("");
//     const [isLoading, setIsLoading] = useState(false);
//     const [error, setError] = useState<string | null>(null);
//     const navigate = useNavigate();
//
//     const handleSubmit = async (e: React.FormEvent) => {
//         e.preventDefault();
//         setError(null);
//         setIsLoading(true);
//
//         const result = await login(email, password);
//
//         setIsLoading(false);
//
//         if (result instanceof Error) {
//             setError(result.message);
//             return;
//         }
//
//         navigate("/");
//     };
//
//     return (
//         <Box
//             sx={{
//                 display: "flex",
//                 height: "100vh",
//                 backgroundColor: (theme) => theme.palette.primary.light,
//             }}
//         >
//             <Box
//                 sx={{
//                     flex: 1,
//                     display: "flex",
//                     flexDirection: "column",
//                     pt: "6rem",
//                     alignItems: "center",
//                     color: (theme) => theme.palette.primary.contrastText,
//                     background: (theme) =>
//                         `linear-gradient(135deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main})`,
//                 }}
//             >
//                 <Typography variant="h3" component="h1" sx={{ fontWeight: "bold" }}>
//                     Your{" "}
//                     <Box
//                         component="span"
//                         sx={{
//                             color: (theme) => theme.palette.secondary.main,
//                             fontWeight: "bold",
//                         }}
//                     >
//                         AI
//                     </Box>
//                     -Powered Intercom! 👋
//                 </Typography>
//                 <Typography
//                     variant="body1"
//                     sx={{
//                         fontSize: "1.2rem",
//                         mt: 4,
//                         textAlign: "center",
//                         maxWidth: "400px",
//                     }}
//                 >
//                     Transform your intercom experience with IoT technology and integrated{" "}
//                     <Box
//                         component="span"
//                         sx={{
//                             color: (theme) => theme.palette.secondary.main,
//                             fontWeight: "bold",
//                         }}
//                     >
//                         AI.{" "}
//                     </Box>
//                     Smart, secure, and effortless communication at your doorstep!
//                 </Typography>
//             </Box>
//
//             <Container
//                 maxWidth="xs"
//                 sx={{
//                     flex: 1,
//                     display: "flex",
//                     flexDirection: "column",
//                     justifyContent: "center",
//                     alignItems: "center",
//                     backgroundColor: "#fff",
//                 }}
//             >
//                 <Typography
//                     variant="h5"
//                     component="h2"
//                     sx={{ fontWeight: "bold", mb: 3 }}
//                 >
//                     Welcome Back!
//                 </Typography>
//                 <Box
//                     component="form"
//                     onSubmit={handleSubmit}
//                     sx={{ width: "100%" }}
//                 >
//                     <TextField
//                         margin="normal"
//                         required
//                         fullWidth
//                         id="email"
//                         label="Email Address"
//                         name="email"
//                         autoComplete="email"
//                         autoFocus
//                         value={email}
//                         onChange={(e) => setEmail(e.target.value)}
//                     />
//                     <TextField
//                         margin="normal"
//                         required
//                         fullWidth
//                         name="password"
//                         label="Password"
//                         type="password"
//                         id="password"
//                         autoComplete="current-password"
//                         value={password}
//                         onChange={(e) => setPassword(e.target.value)}
//                     />
//
//                     {error && <Alert severity="error">{error}</Alert>}
//
//                     <Button
//                         type="submit"
//                         fullWidth
//                         variant="contained"
//                         sx={{
//                             mt: 3,
//                             mb: 2,
//                             backgroundColor: (theme) => theme.palette.primary.main,
//                             "&:hover": {
//                                 backgroundColor: (theme) => theme.palette.primary.dark,
//                             },
//                             display: "flex",
//                             alignItems: "center",
//                             justifyContent: "center",
//                         }}
//                         disabled={isLoading}
//                     >
//                         {isLoading ? (
//                             <CircularProgress size={24} sx={{ color: "white" }} />
//                         ) : (
//                             "Login Now"
//                         )}
//                     </Button>
//
//                     <Typography variant="body2" sx={{ textAlign: "center", mt: 2 }}>
//                         <Link href="#" underline="hover" sx={{ color: (theme) => theme.palette.primary.dark }}>
//                             Forgot password? Click here
//                         </Link>
//                     </Typography>
//                     <Typography variant="body2" sx={{ textAlign: "center", mt: 1 }}>
//                         Don’t have an account?{" "}
//                         <Link href="#" underline="hover" sx={{ color: (theme) => theme.palette.primary.dark }}>
//                             Create a new account now
//                         </Link>
//                     </Typography>
//                 </Box>
//             </Container>
//         </Box>
//     );
// };
//
// export default LoginPage;
