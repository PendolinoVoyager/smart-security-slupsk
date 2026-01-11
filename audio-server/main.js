const WebSocket = require("ws");
const http = require('http');


const PORT = 8888;
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
    if (req.url.startsWith("/user")) {
        try {
            handleUserConnection(ws, req);
        }
        catch (err) {
            console.error(err);
            ws.close(1002, err);
        }
    }
    else if (req.url.startsWith("/device")) {
        try {
            handleDeviceConnection(ws, req);
        }
        catch (err) {
            console.error(err);
            ws.close(1002, err);
        }
    }
    else {
        ws.close(1008, "wrong url: /user or /device only");
    }
});
wss.on("error", () => {
    devices.forEach((d, key) => {
        d.ws.close();
    })
})

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Audio server running on port ${PORT}`);
});
