package jscast.boot;

import jscast.frames.FrameProcessor;
import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jscast.utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);


    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FrameProcessor frameProcessor = new FrameProcessor(
                "rtsp://192.168.0.13:554/onvif1",
                "C:\\tmp",
                "fram%15d.jpg",
                "1/0.2",
                40,
                logger
        );

        try {
            frameProcessor.prepareStream();
            frameProcessor.startFrameProcessing();
            Thread.sleep(1000 * 60 * 50);
            frameProcessor.stopFrameProcessor();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
