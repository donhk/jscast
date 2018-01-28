package frameServer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

import jscast.streams.FrameFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class FrameServerTest {

    private Logger logger;

    @Before
    public void setup() {
        logger = createLoggerFor("testA", "build/test-results/test.log");
    }

    @Test
    public void frames() {
        FrameFactory frameFactory = new FrameFactory(
                "rtsp://192.168.0.13:554/onvif1",
                "C:\\tmp",
                "fram%15d.jpg",
                "1/0.5",
                logger
        );
        try {
            System.out.println("Starting stream server");
            frameFactory.fragmentStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger createLoggerFor(String string, String file) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(file);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(string);
        //logger.addAppender(fileAppender);
        logger.addAppender(consoleAppender);

        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }
}
