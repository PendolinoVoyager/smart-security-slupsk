import { useEffect, useState } from "react";
import { fetchDevices } from "../../api/devicesApi.ts";
import { useNavigate } from "react-router-dom";
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Box,
    Button,
    Typography,
    CircularProgress,
    Alert,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField,
} from "@mui/material";

type Device = {
    id: string;
    address: string;
    deviceName: string;
    uuid: string;
};

const DevicePage = () => {
    const [devices, setDevices] = useState<Device[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [open, setOpen] = useState(false);
    const [newDevice, setNewDevice] = useState({
        uuid: "",
        address: "",
        deviceName: "",
    });
    const navigate = useNavigate();

    useEffect(() => {
        const loadDevices = async () => {
            setIsLoading(true);
            setError(null);
            const result = await fetchDevices();
            if (!result) {
                setError("Failed to fetch devices. Please try again later.");
            } else {
                setDevices(result);
            }
            setIsLoading(false);
        };

        loadDevices();
    }, []);

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setNewDevice({ ...newDevice, [name]: value });
    };

    const handleSubmit = () => {
        // Replace with API call to save the device
        console.log("Device added:", newDevice);
        setDevices([...devices, { id: (devices.length + 1).toString(), ...newDevice }]);
        setNewDevice({ uuid: "", address: "", deviceName: "" });
        handleClose();
    };

    return (
        <Box sx={{ padding: 2 }}>
            <Typography variant="h4" gutterBottom>
                Your Devices ⚙️
            </Typography>
            <Box display="flex" justifyContent="space-between" alignItems="center" marginBottom={2}>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleOpen}
                >
                    Add New Device
                </Button>
            </Box>

            {isLoading && (
                <Box display="flex" justifyContent="center" alignItems="center" height="100px">
                    <CircularProgress />
                </Box>
            )}
            {error && <Alert severity="error">{error}</Alert>}
            {!isLoading && !error && (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>ID</TableCell>
                                <TableCell>Device Name</TableCell>
                                <TableCell>Address</TableCell>
                                <TableCell>UUID</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {devices.map((device) => (
                                <TableRow key={device.id}>
                                    <TableCell>{device.id}</TableCell>
                                    <TableCell>{device.deviceName || "N/A"}</TableCell>
                                    <TableCell>{device.address || "N/A"}</TableCell>
                                    <TableCell>{device.uuid || "N/A"}</TableCell>
                                    <TableCell>
                                        <Button
                                            variant="outlined"
                                            color="info"
                                            onClick={() => navigate(`/devices/${device.id}`)}
                                        >
                                            View
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            <Dialog open={open} onClose={handleClose}>
                <DialogTitle>Add New Device</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="UUID"
                        name="uuid"
                        value={newDevice.uuid}
                        onChange={handleChange}
                        fullWidth
                        required
                    />
                    <TextField
                        margin="dense"
                        label="Address"
                        name="address"
                        value={newDevice.address}
                        onChange={handleChange}
                        fullWidth
                    />
                    <TextField
                        margin="dense"
                        label="Device Name"
                        name="deviceName"
                        value={newDevice.deviceName}
                        onChange={handleChange}
                        fullWidth
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={handleSubmit} color="primary">
                        Add
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default DevicePage;