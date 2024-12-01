import { useParams } from "react-router-dom";

const DeviceDetails = () => {
    const { id } = useParams<{ id: string }>();

    return (
        <div>
            <h1>Device Details</h1>
            <p>Device ID: {id}</p>
        </div>
    );
};

export default DeviceDetails;
