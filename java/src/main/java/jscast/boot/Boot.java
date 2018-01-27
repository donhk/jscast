package jscast.boot;

import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static jscast.utils.Constants.FRAME_SERVER;
import static jscast.utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);
    private static final Logger frameLogger = LoggerFactory.getLogger(FRAME_SERVER);

    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //MDC.put("logFileName", "head1");
        System.setProperty("logBase", System.getProperty("user.dir"));
        System.setProperty("logFileName", GLOBAL_LOGGER);
        logger.debug("test1 {}", new Date());
        logger.info("test2 {}", new Date());
        frameLogger.debug("Test");


    }
}
