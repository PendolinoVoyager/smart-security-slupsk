import { Box, Typography } from "@mui/material";

const InformativePanel = () => (
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
);

export default InformativePanel;
