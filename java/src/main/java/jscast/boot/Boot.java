package jscast.boot;

import jscast.frames.FrameProcessor;
import jscast.pojos.onvif.Camera;
import jscast.region.CameraPosition;
import jscast.region.PositionManager;
import jscast.ui.FrameSampler;
import jscast.ui.FrameSamplerController;
import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jscast.utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);


    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            //scan read looking for cameras
            CameraPosition cameraPosition = new CameraPosition("localhost", "6015", logger);
            Camera[] cameras = cameraPosition.initDevices();
            //Test code
            Camera testCam = cameras[0];

            String source = testCam.attr.current_profile.stream.udp;
            String destiny = "C:\\tmp";
            String filePattern = "fram%15d.jpg";
            String fps = "1/1";

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
            PositionManager positionManager = new PositionManager(testCam, logger);

            //add observer to frame processor
            frameProcessor.addObserver(positionManager);

            frameProcessor.prepareStream();
            frameProcessor.startFrameProcessing();

            //wait for a while before stop
            Thread.sleep(1000 * 60 * 50);
            frameProcessor.stopFrameProcessor();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
