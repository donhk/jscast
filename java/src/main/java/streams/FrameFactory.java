package streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static utils.Constants.GLOBAL_LOGGER;

public class FrameFactory {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);
    private static final Logger outLogger = LoggerFactory.getLogger("OUT");
    private static final Logger errLogger = LoggerFactory.getLogger("ERR");

    private final String source;
    private final String destiny;

    public FrameFactory(String source, String destiny) {
        this.source = source;
        this.destiny = destiny;
    }

    public void fragmentStream() throws Exception {
        List<String> args = Arrays.asList(
                "ffmpeg",
                "-i",
                "rtsp://192.168.0.13:554/onvif1",
                "-vf",
                "fps=1/0.3",
                "C:\\Users\\hkfre\\Desktop\\tmp\\fram%015d.bmp"
        );
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();
        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String l;
                while ((l = reader.readLine()) != null) {
                    outLogger.info(l);
                }
            } catch (IOException e) {
                logger.error("Error reading stdout", e);
            }
        });

        Thread errThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String l;
                while ((l = reader.readLine()) != null) {
                    errLogger.info(l);
                }
            } catch (IOException e) {
                logger.error("Error reading stderr", e);
            }
        });

        outThread.start();
        errThread.start();

        new Thread(() -> {
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
                outThread.join();
                errThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted", e);
            }
            // Process completed and read all stdout and stderr here
        }).start();
    }
}
