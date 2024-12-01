import { useEffect, useState } from "react";
import { fetchDevices } from "../../api/devicesApi.ts";
import { Link } from "react-router-dom";

const DevicePage = () => {
    const [devices, setDevices] = useState<{ id: string }[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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

    return (
        <div>
            <h1>Device Page</h1>
            {isLoading && <p>Loading devices...</p>}
            {error && <p style={{ color: "red" }}>Error: {error}</p>}
            {!isLoading && !error && (
                <ul>
                    {devices.map((device) => (
                        <li key={device.id}>
                            <Link to={`/devices/${device.id}`}>{device.id}</Link>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default DevicePage;
