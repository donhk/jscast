//ES6 arrow function
//dependencies
const onvif = require('node-onvif');

// Create an OnvifDevice object
let device = new onvif.OnvifDevice({
    xaddr: 'http://192.168.0.13:5000/onvif/device_service',
    user: 'admin',
    pass: '123456'
});

// Initialize the OnvifDevice object
device.init().then((info) => {
    // Show the detailed information of the device.
    console.log(JSON.stringify(info, null, '  '));

    // Get the UDP stream URL
    let url = device.getUdpStreamUrl();
    console.log(url);

    //gather device information
    let profile = device.getCurrentProfile();
    console.log(JSON.stringify(profile, null, '  '));

    let params = {
        'ProfileToken': 'IPCProfilesToken0',
        'Protocol': 'UDP',
    };

    device.services.media.getStreamUri(params).then((result) => {
        console.log(JSON.stringify(result['data'], null, '  '));
    }).catch((error) => {
        console.error(error);
    });

}).catch((error) => {
    console.error(error);
});


