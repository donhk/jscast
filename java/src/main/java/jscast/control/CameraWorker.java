package jscast.control;

import jscast.frames.FrameProcessor;
import jscast.pojos.Point;
import jscast.pojos.onvif.Camera;
import jscast.devices.DeviceControl;
import jscast.devices.PositionManager;
import jscast.ui.FrameSampler;

import java.util.concurrent.Callable;

import org.opencv.core.Rect;
import org.slf4j.Logger;

public class CameraWorker implements Callable<String> {
    private final Camera camera;
    private String source;
    private String destiny;
    private String filePattern;
    private String fps;
    private Logger logger;
    private DeviceControl controller;

    public CameraWorker(Camera camera, String localhost, String port, Logger logger) {
        this.camera = camera;
        this.source = camera.attr.current_profile.stream.udp;
        this.destiny = "C:\\tmp";
        this.filePattern = camera.name + "fra%15d.jpg";
        this.fps = "1/0.5";
        this.logger = logger;
        this.controller = new DeviceControl(localhost, port, logger);
    }

    @Override
    public String call() throws Exception {
        //calculate main rectangles
        int width = Integer.parseInt(camera.attr.current_profile.video.encoder.resolution.width);
        int height = Integer.parseInt(camera.attr.current_profile.video.encoder.resolution.height);

        System.out.println("width> " + width + " height> " + height);
        //get center of reference
        Point center = new Point(width / 2, height / 2);
        System.out.println("Center of frame " + center.toString());

        //calculate rectangle used to measure relative position
        double pct = 0.7;
        double hotW = width * pct;
        double hotH = height * pct;
        double correctionX = hotW * 0.5;
        double correctionY = hotH * 0.5;
        double rx = center.x - correctionX;
        double ry = center.y - correctionY;

        //create reference areas
        Rect mainArea = new Rect(0, 0, width, height);
        Rect hotArea = new Rect((int) rx, (int) ry, (int) hotW, (int) hotH);

        //start the code in charge of collect the frames from the stream
        //and analyze each frame
        FrameProcessor frameProcessor = new FrameProcessor(source, destiny, filePattern, fps, 20, mainArea, hotArea, center, logger);
        PositionManager positionManager = new PositionManager(camera, controller, mainArea, hotArea, center, logger);

        //add observer to frame processor
        frameProcessor.addObserver(positionManager);
        frameProcessor.prepareStream();
        frameProcessor.startFrameProcessing();

        //start x
        System.out.println("Starting GUI");
        FrameSampler frameSampler = new FrameSampler();
        Thread x = new Thread(frameSampler, camera.attr.address);
        x.start();
        //wait until the gui is fully loaded
        do {
            Thread.sleep(100);
        } while (frameSampler.getController() == null);
        frameProcessor.setGui(frameSampler.getController());

        return null;
    }
}
