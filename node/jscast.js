//ES6 arrow function
//dependencies


const onvif = require('node-onvif');
const express = require('express');
const bodyParser = require('body-parser');
const uniqid = require('uniqid');

const app = express();

//You must make sure that you define all configurations BEFORE defining routes.
app.use(bodyParser.json()); // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({extended: true}));

app.listen(process.env.PORT || 6015, () => {
    console.log('listening on 6015')
});

//cameras on network
let cameras = [];

app.get('/getDevices', (req, res) => {
    console.log('Start the discovery process.');
    onvif.startProbe().then((device_info_list) => {
        console.log(device_info_list.length + ' devices were found.');
        res.json(device_info_list);
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

//Content-type application/json
app.post('/move', (req, res) => {
    console.log(req.body);
    let x = parseFloat(req.body.x);
    let y = parseFloat(req.body.y);
    let z = parseFloat(req.body.z);
    move(req.body.target, x, y, z);
    res.json('done');
});

//TODO temp code
app.get('/ptzx', (req, res) => {
    move('fred', 1, 0, 0);
    res.json('done');
});

app.get('/ptzy', (req, res) => {
    move('fred', 0, 1, 0);
    res.json('done');
});

app.get('/ptzz', (req, res) => {
    move('fred', 0, 0, 1);
    res.json('done');
});

app.get('/ptz-x', (req, res) => {
    move('fred', -1, 0, 0);
    res.json('done');
});

app.get('/ptz-y', (req, res) => {
    move('fred', 0, -1, 0);
    res.json('done');
});

app.get('/ptz-z', (req, res) => {
    move('fred', 0, 0, -1);
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
    // Move the camera
    return device.ptzMove({
        'speed': {
            x: x, // Speed of pan (in the range of -1.0 to 1.0)
            y: y, // Speed of tilt (in the range of -1.0 to 1.0)
            z: z  // Speed of zoom (in the range of -1.0 to 1.0)
        },
        'timeout': 1 // seconds
    });
}

