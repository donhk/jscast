package boot;

import frames.FrameProcessor;
import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);

    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //MDC.put("logFileName", "head1");
        System.setProperty("logBase", System.getProperty("user.dir"));
        System.setProperty("logFileName", GLOBAL_LOGGER);
        logger.debug("[MAIN] Current Date : {}", new Date());
        FrameProcessor frameProcessor = new FrameProcessor("x", "y");
        frameProcessor.prepareStream();
        try {
            frameProcessor.startFrameProcessing();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
