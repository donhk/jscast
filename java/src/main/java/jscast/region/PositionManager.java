package jscast.region;

import jscast.pojos.Wrapper;
import jscast.pojos.onvif.Camera;
import jscast.pojos.onvif.OnvifDevice;
import org.slf4j.Logger;

import java.util.Observable;
import java.util.Observer;

public class PositionManager implements Observer {

    private String name;
    private OnvifDevice onvifDevice;
    private Logger logger;

    public PositionManager(Camera camera, Logger logger) {
        this.name = camera.name;
        this.onvifDevice = camera.attr;
        this.logger = logger;
    }

    @Override
    public void update(Observable o, Object arg) {
        Wrapper wrapper = (Wrapper) arg;
        System.out.println("Frame found! " + wrapper.getReference().width + " x " + wrapper.getReference().height);
        System.out.println("targets: " + wrapper.getTargets().length);
    }
}
