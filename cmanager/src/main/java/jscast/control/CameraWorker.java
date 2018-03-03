package jscast.control;

import jscast.frames.FrameProcessor;
import jscast.pojos.onvif.Camera;
import jscast.devices.DeviceControl;
import jscast.devices.PositionManager;
import jscast.ui.FrameSampler;

import java.util.concurrent.Callable;

import jscast.utils.FrameTools;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import rx.Observable;

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
        this.fps = "1/0.2";
        this.logger = logger;
        this.controller = new DeviceControl(localhost, port, logger);
    }

    @Override
    public String call() throws Exception {
        //calculate main rectangles
        int width = Integer.parseInt(camera.attr.current_profile.video.encoder.resolution.width);
        int height = Integer.parseInt(camera.attr.current_profile.video.encoder.resolution.height);

        //create reference areas
        Rect mainArea = new Rect(0, 0, width, height);
        //get center of reference
        Point center = FrameTools.getCenter(mainArea);
        Rect hotArea = FrameTools.calculateHotArea(mainArea, center, 0.4);

        System.out.println("width> " + width + " height> " + height);
        System.out.println("Center of frame " + center.toString());

        //start the code in charge of collect the frames from the stream
        //and analyze each frame
        FrameProcessor frameProcessor = new FrameProcessor(source, destiny, filePattern, fps, 20, mainArea, hotArea, center, logger);
        PositionManager positionManager = new PositionManager(camera, controller, mainArea, hotArea, center, logger);

        //add observer to frame processor
        Observable<Rect[]> observable = frameProcessor.getObservable();
        observable.subscribe(positionManager::update);

        frameProcessor.prepareStream();
        frameProcessor.startFrameProcessing();

        //start x
        System.out.println("Starting GUI");
        FrameSampler frameSampler = new FrameSampler();
        Thread x = new Thread(frameSampler, camera.attr.address);
        x.start();
        //wait until the gui is fully loaded
        do {
            Thread.sleep(300);
        } while (frameSampler.getController() == null);
        frameProcessor.setGui(frameSampler.getController());

        return null;
    }
}
