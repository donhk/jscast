package jscast.devices;

import jscast.pojos.onvif.Camera;
import jscast.pojos.onvif.OnvifDevice;
import jscast.utils.FrameTools;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

            /*
             calculate the distance of this point respect the 4 axis
             to determine which is the closest one
             this is going to be:

              0,0                W,0
                    w i d t h
                        p0
                   tgt  |          h
                    .   |          e
                 p3-----+-----p1   i
                        |          g
                        |          t
                        p2         h

             0,H                  W,H


            in order to calculate the distance we need to get the points,
            those will be derived from the mainArea

             */
            Point p0 = new Point(mainArea.width / 2, 0);
            Point p1 = new Point(mainArea.width, mainArea.height / 2);
            Point p2 = new Point(mainArea.width / 2, mainArea.height);
            Point p3 = new Point(0, mainArea.height / 2);

            //calculate distance and preserve the point
            Map<String, Double> distance = new HashMap<>();
            distance.put("p0", FrameTools.distanceBetweenPoints(target, p0));
            distance.put("p1", FrameTools.distanceBetweenPoints(target, p1));
            distance.put("p2", FrameTools.distanceBetweenPoints(target, p2));
            distance.put("p3", FrameTools.distanceBetweenPoints(target, p3));

            //look for the closest one
            Map.Entry<String, Double> closest =
                    sortByValue(distance).entrySet().iterator().next();
            System.out.println("closest: " + closest.getKey() + " -> " + closest.getValue());
            //readjust
            moveTo(closest.getKey());
        }

    }

    private void moveTo(String point) {
        switch (point) {
            case "p0"://move up
                cameraPosition.move(name, 1.0, 0.0, 0.0);
                return;
            case "p1"://move right
                cameraPosition.move(name, 0.0, -1.0, 0.0);
                return;
            case "p2"://move down
                cameraPosition.move(name, -1.0, 0.0, 0.0);
                return;
            case "p3"://move left
                cameraPosition.move(name, 0.0, 1.0, 0.0);
        }
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
