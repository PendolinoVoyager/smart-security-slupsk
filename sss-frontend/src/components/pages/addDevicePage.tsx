import { Box, Container, Typography, TextField, Button } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

const AddDevicePage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        uuid: "",
        address: "",
        deviceName: "",
    });

    const handleChange = (e: { target: { name: any; value: any; }; }) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        // Replace with API call to save the device
        console.log("Device added:", formData);
        navigate("/devices");
    };

    return (
        <Box
            sx={{
                display: "flex",
                height: "100vh",
            }}
        >
            <Container
                maxWidth="xs"
                sx={{
                    flex: 1,
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    alignItems: "center",
                }}
            >
                <Typography
                    variant="h5"
                    component="h2"
                    sx={{ fontWeight: "bold", mb: 3 }}
                >
                    Add New Device
                </Typography>
                <form onSubmit={handleSubmit} style={{ width: "100%" }}>
                    <TextField
                        label="UUID"
                        name="uuid"
                        value={formData.uuid}
                        onChange={handleChange}
                        required
                        fullWidth
                        margin="normal"
                        variant="outlined"
                        sx={{ borderColor: "primary.main", borderWidth: 2 }}
                        InputProps={{
                            style: {
                                fontWeight: "bold",
                                borderColor: "primary.main",
                                borderWidth: 2,
                            },
                        }}
                    />
                    <TextField
                        label="Address"
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        fullWidth
                        margin="normal"
                        variant="outlined"
                    />
                    <TextField
                        label="Device Name"
                        name="deviceName"
                        value={formData.deviceName}
                        onChange={handleChange}
                        fullWidth
                        margin="normal"
                        variant="outlined"
                    />
                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        fullWidth
                        sx={{ mt: 3 }}
                    >
                        Add Device
                    </Button>
                </form>
            </Container>
        </Box>
    );
};

export default AddDevicePage;
