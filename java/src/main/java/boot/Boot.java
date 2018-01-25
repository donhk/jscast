package boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import streams.FrameFactory;

import java.util.Date;

import static utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);

    public static void main(String[] args) {
        logger.debug("[MAIN] Current Date : {}", new Date());
        FrameFactory frameFactory = new FrameFactory("x", "y");
        try {
            frameFactory.fragmentStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
