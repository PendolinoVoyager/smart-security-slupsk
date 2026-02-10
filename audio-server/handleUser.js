const {checkDeviceBusy, checkDeviceConnected, devices, users, BACKEND_URL} = require("./main")

/**
 * Only construct this if you're sure the device with corresponding id is owned by the user.
 */
class UserConnection {
    constructor(deviceId, userId) {
        this.deviceId = deviceId;
        this.userId = userId;
    }
}

/**
 * returns email if valid, otherwise throws error
 */
/**
 * returns email if valid, otherwise throws error
 */
function authenticateUser(token, deviceId) {
    return fetch(BACKEND_URL + "/api/v1/auth/audio-server", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            token,
            deviceId
        })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Failed to authenticate user: " + res.status);
        }
        return res.json(); // IMPORTANT
    })
    .then(data => {

        if (!data.valid || !data.email) {
            throw new Error("Invalid response");
        }

        return data.email;
    });
}

/**
 * Params required for user to send in first connection:
 * - token
 * - deviceId
 */
module.exports.handleUserConnection = function (ws, req) {
    try {
        const params = new URL("ws://localhost" + req.url).searchParams;
        const token = params.get("token");
        const deviceId = Number.parseInt(params.get("deviceId"), 10);

        if (!token) {
            throw new Error("please provide token in query params");
        }
        if (!Number.isInteger(deviceId)) {
            console.debug("Wrong device id:", deviceId);
            throw new Error("Wrong deviceId");
        }

        console.debug("User trying to connect to device " + deviceId);

        authenticateUser(token, deviceId)
            .then(email => {
                console.debug("User authenticated with email:", email);

                if (!checkDeviceConnected(deviceId)) {
                    throw new Error("Cannot send audio - device not connected to audio server.");
                }

                if (checkDeviceBusy(deviceId)) {
                    throw new Error("Cannot send audio - device is already receiving audio.");
                }

                const connection = new UserConnection(deviceId, email);
                users.set(email, connection);

                const deviceConnection = devices.get(deviceId);
                deviceConnection.busy = true;

                ws.on("message", (msg) => {
                    deviceConnection.ws.send(msg, (err) => {
                        if (err) {
                            ws.close(1008, "device disconnected: " + err);
                        }
                    });
                });

                ws.on("close", () => {
                    deviceConnection.busy = false;
                    console.log(`User ${email} disconnected.`)
                    users.delete(email);
                });
            })
            .catch(err => {
                console.error("Authentication / connection failed:", err);
                ws.close(1008, err.message);
            });

    } catch (err) {
        // synchronous errors (bad params, etc.)
        console.error("Connection setup error:", err);
        ws.close(1008, err.message);
    }
};
