package jscast.devices;

import jscast.pojos.Point;
import jscast.pojos.Wrapper;
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

    public PositionManager(Camera camera, DeviceControl cameraPosition, Logger logger) {
        this.name = camera.name;
        this.onvifDevice = camera.attr;
        this.logger = logger;
        this.cameraPosition = cameraPosition;
    }

    @Override
    public void update(Observable o, Object arg) {
        Wrapper wrapper = (Wrapper) arg;
        //get center of reference
        Point center = new Point(wrapper.getReference().width / 2, wrapper.getReference().height / 2);
        System.out.println("Center of frame " + center.toString());

        //calculate rectangle used to measure relative position
        double pct = 0.4;
        double hotW = wrapper.getReference().width * pct;
        double hotH = wrapper.getReference().height * pct;
        double correctionX = hotW * 0.5;
        double correctionY = hotH * 0.5;
        double rx = center.x - correctionX;
        double ry = center.y - correctionY;

        //TODO move this outside this method
        Rect hotArea = new Rect((int) rx, (int) ry, (int) hotW, (int) hotH);

        System.out.println("hot area x " + hotArea.x + " y " + hotArea.y + " w " + hotArea.width + " h " + hotArea.height);

        //calculate each target
        ArrayList<Point> points = new ArrayList<>();
        for (Rect rect : wrapper.getTargets()) {
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
