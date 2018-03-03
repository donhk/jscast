package jscast.devices;

import jscast.pojos.onvif.Camera;
import jscast.pojos.onvif.OnvifDevice;
import jscast.utils.FrameTools;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.slf4j.Logger;

public class PositionManager {

    private String name;
    private OnvifDevice onvifDevice;
    private Logger logger;
    private DeviceControl cameraPosition;
    private Rect mainArea;
    private Rect hotArea;
    private Point center;

    public PositionManager(Camera camera, DeviceControl cameraPosition, Rect mainArea, Rect hotArea, Point center, Logger logger) {
        this.name = camera.name;
        this.onvifDevice = camera.attr;
        this.logger = logger;
        this.cameraPosition = cameraPosition;
        this.mainArea = mainArea;
        this.hotArea = hotArea;
        this.center = center;
    }

    public void update(Rect[] targets) {

        //if there is more than one target, lets calculate an avg point
        //based on all the targets found
        //get difference between target and reference
        Point target = FrameTools.keyPoint(targets);

        if (!hotArea.contains(new org.opencv.core.Point(target.x, target.y))) {
            System.out.println("Point outside hot-area");
            //cameraPosition.move(name, 0.0, -1.0, 0.0);
        }

    }
}
