'use strict';
//ES6 arrow function
//dependencies
const onvif = require('node-onvif');
const express = require('express');
const bodyParser = require('body-parser');
const uniqid = require('uniqid');

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
            return processDevice(info);
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

app.get('/ptzx', (req, res) => {
    move('fred', 0, 0, 0);
    res.json('done');
});

function processDevice(info) {
    let device = new onvif.OnvifDevice({
        xaddr: info.xaddrs[0],
        user: 'admin',
        pass: '123456' //TODO add dynamic user list
    });
    return device.init().then(() => {
        console.log("init done");
        const camObj = {name: 'fred', attr: device}; //uniqid()
        cameras.push(camObj);
    });
}

function move(target, x, y, z) {
    const camObj = cameras.filter((obj) => {
        return obj.name === target;
    });
    const device = camObj[0].attr; //ugly but works
    console.log("we have the device now :)");
}