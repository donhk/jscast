package boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        logger.debug("[MAIN] Current Date : {}", new Date());
    }
}
