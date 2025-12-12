const {checkDeviceBusy, checkDeviceConnected, devices, users} = require("./main")

/**
 * Only construct this if you're sure the device with corresponding id is owned by the user.
 */
class UserConnection {
    constructor(deviceId, userId) {
        this.deviceId = deviceId;
        this.userId = userId;
    }
}

function authenticateUser(token) {
    return true;
}
function checkDeviceOwnership(userId, deviceId) {
    return true;
}

/**
 * Params required for user to send in first connection:
 * - token
 * - deviceId
 */
module.exports.handleUserConnection = function(ws, req) {
    const params = new URL("ws://localhost" + req.url).searchParams;
    const token = params.get("token");
    const deviceId = Number.parseInt(params.get("deviceId"));

    if (!token) {
        throw new Error("please provide token in query params");
    }
    if (!deviceId || !Number.isInteger(deviceId)) {
        console.debug("Wrong device id: " + deviceId)
        throw new Error("Wrong deviceId");
    }
    if (!authenticateUser(token)) {
        throw new Error("Failed to authenticate user.");
    }

    const userId = 10;


    if (!checkDeviceOwnership(userId, deviceId)) {
        throw new Error ("User does not own this device.");
    }

    if (!checkDeviceConnected(deviceId)) {
        throw new Error("Cannot send audio - device not connected to audio server.");
    }

    if (checkDeviceBusy(deviceId)) {
        throw new Error("Cannot send audio - device is already receiving audio.")
    }

    const connection = new UserConnection(deviceId, userId);
    users.set(userId, connection);
    const deviceConnection = devices.get(deviceId);
    deviceConnection.busy = true;

    ws.on("message", (msg) => {
        deviceConnection.ws.send(msg, (err) => {
            err && ws.close(1008, "device disconnected: " + err);
        });

    });
    ws.on("close", () => {
        deviceConnection.busy = false;
        users.delete(userId);
    });
}