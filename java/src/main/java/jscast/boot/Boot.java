package jscast.boot;

import jscast.frames.FrameProcessor;
import jscast.region.CameraPosition;
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

        CameraPosition cameraPosition = new CameraPosition("localhost", "6015", logger);
        cameraPosition.initDevices();

    }
}
