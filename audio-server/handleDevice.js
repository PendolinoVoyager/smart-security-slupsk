const {checkDeviceConnected, devices, DEVICE_TOKENS_ARE_IDS, BACKEND_URL} = require("./main")

class DeviceConnection {
    constructor(deviceId, ws) {
        this.deviceId = deviceId;
        this.busy = false;
        this.ws = ws;
    }
    
}

async function authenticateDevice(token) {
    const res = await fetch(BACKEND_URL + "/api/v1/auth/device/audio-server", {
        method: "POST",
        headers: {
            "Authorization": "Bearer: " + token
        }
    });
    if (!res.ok) {
        throw new Error("Cannot authorize: " + res.status);
    }
    const id = await res.text();
    return Number.parseInt(id, 10);
}
/**
 * Params required for device to send in first connection:
 * - token
 * - deviceId
 */
module.exports.handleDeviceConnection = function(ws, req) {

    const params = new URL("ws://localhost" + req.url).searchParams;
    const token = params.get("token");

    if (!token) {
        throw new Error("please provide token in query params");
    }
    if (DEVICE_TOKENS_ARE_IDS) {
        const deviceId = Number.parseInt(token, 10);
        _handleConnectionAfterAuth(deviceId, ws);
    }
    else {
        authenticateDevice(token).then(deviceId => {
            _handleConnectionAfterAuth(deviceId, ws);
        }).catch(err => {
            console.error(err.message);
            ws.close(1002, "Unauthorized: " + err.message);
        });
    }

}

function _handleConnectionAfterAuth(deviceId, ws) {
    console.log("Device " + deviceId + " connected.");
     if (checkDeviceConnected(deviceId)) {
        throw new Error("Device already connected");
    }

    const device = new DeviceConnection(deviceId, ws);
    devices.set(deviceId, device);
    
    device.ws.on("close", () => {
        devices.delete(deviceId);
    })
}