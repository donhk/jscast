package jscast.devices;

import jscast.pojos.Point;
import jscast.pojos.onvif.Camera;
import jscast.pojos.onvif.OnvifDevice;
import org.opencv.core.Rect;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class PositionManager implements Observer {

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

    @Override
    public void update(Observable o, Object arg) {
        Rect[] targets = (Rect[]) arg;

        //calculate each target
        ArrayList<Point> points = new ArrayList<>();
        for (Rect rect : targets) {
            double dx = rect.x + (rect.width / 2);
            double dy = rect.y + (rect.height / 2);
            points.add(new Point(dx, dy));
        }

        //if there is more than one target, lets calculate an avg point
        //based on all the targets found
        //get difference between target and reference
        for (Point p : points) {
            System.out.println("target point " + p);
            System.out.println("distance respect of the center " + p.distance(center));
            //for one point
            if (!hotArea.contains(new org.opencv.core.Point(p.x, p.y))) {
                cameraPosition.move(name, 0.0, -1.0, 0.0);
            }
            if (p.distance(center) > 500) {
                cameraPosition.move(name, 0.0, 1.0, 0.0);
            }
        }


    }
}
