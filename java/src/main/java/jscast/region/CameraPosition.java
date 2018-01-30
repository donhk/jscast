package jscast.region;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import jscast.pojos.Coord;
import jscast.pojos.DeviceHead;
import jscast.pojos.onvif.Camera;
import jscast.pojos.onvif.OnvifDevice;
import org.slf4j.Logger;

public class CameraPosition {

    private final String url;
    private final Logger logger;
    private final Gson gson = new Gson();

    public CameraPosition(String host, String port, Logger logger) {
        url = "http://" + host + ":" + port;
        this.logger = logger;
    }

    public DeviceHead[] showOff() {
        //https://stackoverflow.com/questions/3763937/gson-and-deserializing-an-array-of-objects-with-arrays-in-it
        logger.info("Getting list of devices from " + url + "/getDevices");
        Client client = Client.create();
        WebResource webResource = client.resource(url + "/getDevices");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 200) {
            logger.warn("Failed : HTTP error code : " + response.getStatus());
        } else {
            logger.warn("Request finished: " + response.getStatus());
        }

        String output = response.getEntity(String.class);
        logger.warn("Deserializing response");
        DeviceHead[] deviceHeads = gson.fromJson(output, DeviceHead[].class);
        logger.info("Devices found: " + deviceHeads.length);
        StringBuilder sb = new StringBuilder(System.lineSeparator());
        for (DeviceHead dev : deviceHeads) {
            sb.append("----------------------").append(System.lineSeparator());
            sb.append("urn   ").append(dev.urn).append(System.lineSeparator());
            sb.append("name  ").append(dev.name).append(System.lineSeparator());
            sb.append("xaddrs ").append(System.lineSeparator());
            for (String add : dev.xaddrs) {
                sb.append("  adx ").append(add).append(System.lineSeparator());
            }
        }
        logger.info(sb.toString());
        return deviceHeads;
    }

    public void move(String target, String x, String y, String z) {
        logger.info("Updating position " + url + "/move");
        Client client = Client.create();
        WebResource webResource = client.resource(url + "/move");
        Coord coord = new Coord(target, x, y, z);
        String input = gson.toJson(coord);
        logger.debug(input);
        ClientResponse response
                = webResource
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, input);

        if (response.getStatus() != 200) {
            logger.warn("Failed : HTTP error code : " + response.getStatus());
        } else {
            logger.warn("Request finished: " + response.getStatus());
        }
    }

    public void move(String target, double x, double y, double z) {
        move(target, String.valueOf(x), String.valueOf(y), String.valueOf(z));
    }

    public Camera[] initDevices() {
        logger.info("Init devices " + url + "/initDevices");
        Client client = Client.create();
        WebResource webResource = client.resource(url + "/initDevices");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != 200) {
            logger.warn("Failed : HTTP error code : " + response.getStatus());
        } else {
            logger.warn("Request finished: " + response.getStatus());
        }

        String output = response.getEntity(String.class);
        logger.warn("Deserializing response");
        Camera[] cameras = gson.fromJson(output, Camera[].class);
        logger.info("Devices found: " + cameras.length);
        StringBuilder sb = new StringBuilder(System.lineSeparator());
        for (Camera cam : cameras) {
            String cname = cam.name;
            OnvifDevice onvifDevice = cam.attr;
            sb.append("camera name ").append(cname).append(System.lineSeparator());
            sb.append("onvifDevice.address ").append(onvifDevice.address).append(System.lineSeparator());
            sb.append("onvifDevice.xaddr ").append(onvifDevice.xaddr).append(System.lineSeparator());
            sb.append("onvifDevice.current_profile.name ").append(onvifDevice.current_profile.name).append(System.lineSeparator());
            sb.append("onvifDevice.current_profile.stream.udp ").append(onvifDevice.current_profile.stream.udp).append(System.lineSeparator());
        }
        logger.info(sb.toString());
        return cameras;
    }
}
