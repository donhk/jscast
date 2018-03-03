package jscast.devices;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import jscast.pojos.Coord;
import org.slf4j.Logger;

public class DeviceControl {

    private final String url;
    private final Logger logger;
    private final Gson gson = new Gson();

    public DeviceControl(String host, String port, Logger logger) {
        url = "http://" + host + ":" + port;
        this.logger = logger;
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

}
