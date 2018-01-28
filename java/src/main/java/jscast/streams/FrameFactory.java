package jscast.streams;

import org.slf4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FrameFactory {

    private final Logger logger;

    private final String source;
    private final String destiny;
    private final String filePattern;
    private final String fps;

    private OutputStream stdin = null;

    public FrameFactory(String source, String destiny, String filePattern, String fps, Logger logger) {
        this.source = source;
        this.destiny = destiny;
        this.filePattern = filePattern;
        this.fps = fps;
        this.logger = logger;
    }

    public void fragmentStream() throws Exception {
        if (new File(destiny).mkdirs()) {
            logger.error("It was no possible to create target folder " + destiny);
            throw new IOException();
        }

        List<String> args = Arrays.asList(
                "ffmpeg",
                "-i",
                source,
                "-vf",
                "fps=" + fps,
                destiny + File.separator + filePattern
        );

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        stdin = process.getOutputStream();

        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String l;
                while ((l = reader.readLine()) != null) {
                    logger.info(l);
                }
            } catch (IOException e) {
                logger.error("Error reading stdout", e);
            }
        });
        outThread.setDaemon(true);
        outThread.start();

        Thread controlTread = new Thread(() -> {
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
                logger.info("process finished");
                outThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted", e);
            }
            logger.info("process finished with error status: " + exitCode);
            // Process completed and read all stdout and stderr here
        });
        controlTread.setPriority(Thread.MAX_PRIORITY);
        controlTread.start();
    }

    public void stopServer() throws IOException {
        logger.info("Stop frame server");
        if (stdin != null) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            String quit = "q" + System.lineSeparator();
            writer.write(quit);
            writer.flush();
            writer.close();
        }
    }
}
