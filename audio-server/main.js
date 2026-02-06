const WebSocket = require("ws");
const http = require('http');

const HOST = process.env.HOST || '0.0.0.0';
const PORT = Number(process.env.PORT || 8088);
const BACKEND_URL = process.env.BACKEND_URL || 'http://127.0.0.1:8080';

const users = new Map();
const devices = new Map();

function checkDeviceConnected(deviceId) {
    return devices.has(deviceId);
}
function checkDeviceBusy(deviceId) {
    return !!devices.get(deviceId)?.busy;

}
module.exports = {checkDeviceBusy, checkDeviceConnected, users, devices}

const { handleUserConnection } = require("./handleUser");
const { handleDeviceConnection } = require("./handleDevice");


const server = http.createServer();
const wss = new WebSocket.Server({ server });



wss.on("connection", (ws, req) => {
    if (req.url.startsWith("/audio-server/v1/user")) {
        try {
            handleUserConnection(ws, req);
        }
        catch (err) {
            console.error(err);
            ws.close(1002, err);
        }
    }
    else if (req.url.startsWith("/audio-server/v1/device")) {
        try {
            handleDeviceConnection(ws, req);
        }
        catch (err) {
            console.error(err);
            ws.close(1002, err);
        }
    }
    else {
        ws.close(1008, "wrong url: /audio-server/v1 + /user or /device only");
    }
});

wss.on("error", () => {
    devices.forEach((d, key) => {
        d.ws.close();
    })
})


server.listen(PORT, HOST, () => {
  console.log(`Audio server running on host ${HOST} and port ${PORT}`);
});
