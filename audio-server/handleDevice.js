const {checkDeviceConnected, devices} = require("./main")

class DeviceConnection {
    constructor(deviceId, ws) {
        this.deviceId = deviceId;
        this.busy = false;
        this.ws = ws;
    }
    
}

function authenticateDevice(token) {
    return true;
}
/**
 * Params required for device to send in first connection:
 * - token
 * - deviceId
 */
module.exports.handleDeviceConnection = function(ws, req) {

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

    if (!authenticateDevice(token)) {
        throw new Error("Failed to authenticate device");
    }

    if (checkDeviceConnected(deviceId)) {
        throw new Error("Device already connected");
    }

    const device = new DeviceConnection(deviceId, ws);
    devices.set(deviceId, device);
    
    device.ws.on("close", () => {
        devices.delete(deviceId);
    })
}