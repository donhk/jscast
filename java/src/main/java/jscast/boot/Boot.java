package jscast.boot;

import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jscast.utils.Constants.FRAME_SERVER;
import static jscast.utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);
    private static final Logger frameLogger = LoggerFactory.getLogger(FRAME_SERVER);

    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        logger.debug("test1 {}");
        logger.info("test2 {}");
        frameLogger.debug("Test");

    }
}
