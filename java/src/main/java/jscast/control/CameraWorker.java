package jscast.control;

import jscast.frames.FrameProcessor;
import jscast.pojos.onvif.Camera;
import jscast.devices.DeviceControl;
import jscast.devices.PositionManager;
import jscast.ui.FrameSampler;

import java.util.concurrent.Callable;

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
        this.filePattern = "fram%15d.jpg";
        this.fps = "1/0.5";
        this.logger = logger;
        this.controller = new DeviceControl(localhost, port, logger);
    }

    @Override
    public String call() throws Exception {
        //start x
        System.out.println("Starting GUI");
        FrameSampler frameSampler = new FrameSampler();
        Thread x = new Thread(frameSampler);
        x.setName("FrameSampler");
        x.start();

        //wait until the gui is fully loaded
        do {
            Thread.sleep(100);
        } while (frameSampler.getController() == null);

        FrameProcessor frameProcessor = new FrameProcessor(
                source,
                destiny,
                filePattern,
                fps,
                20,
                frameSampler.getController(),
                logger
        );
        PositionManager positionManager = new PositionManager(camera, controller, logger);

        //add observer to frame processor
        frameProcessor.addObserver(positionManager);

        frameProcessor.prepareStream();
        frameProcessor.startFrameProcessing();
        return null;
    }
}
