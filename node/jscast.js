'use strict';
//ES6 arrow function
//dependencies
const onvif = require('node-onvif');
const express = require('express');
const app = express();

//cameras on network
var cameras = [];

app.listen(process.env.PORT || 6015, () => {
    console.log('listening on 6015')
});

app.get('/getDevices', (req, res) => {
    console.log('Start the discovery process.');
    onvif.startProbe().then((device_info_list) => {
        console.log(device_info_list.length + ' devices were found.');
        res.send(JSON.stringify(device_info_list, null, '  '));
    }).catch((error) => {
        console.error(error);
        res.json('nothing');
    });
});

app.get('/initDevices', (req, res) => {
    console.log('init devices.');
    cameras = [];
    onvif.startProbe().then((device_info_list) => {
        console.log(device_info_list.length + ' devices will be initialized.');
        let requests = device_info_list.map((info) => {
            processDevice(info)
        });
        Promise.all(requests).then(() => {
            console.log('returning cameras found ' + cameras.length);
            res.json(cameras);
        });
    }).catch((error) => {
        console.error(error);
        res.json('nothing');
    });
});

function processDevice(info) {
    // Return a new promise
    return new Promise((resolve, reject) => {
        console.log("processing " + info.xaddrs[0]);
        // Create an OnvifDevice object
        let device = new onvif.OnvifDevice({
            xaddr: info.xaddrs[0],
            user: 'admin',
            pass: '123456' //TODO add dynamic user list
        });
        console.log("init");
        device.init().then((devinfo) => {
            //if all is okay, add it to the list
            console.log("pusshing " + devinfo.Manufacturer + "-" + devinfo.SerialNumber);
            cameras.push(devinfo);
            console.log("cameras: " + cameras.length);
            resolve("Stuff worked!");
        }).catch((error) => {
            //if we hit an error, log the error TODO send error feedback
            console.log("error initializing " + info.name + " " + error);
            reject(Error("It broke"));
        });
    });
}