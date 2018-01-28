package jscast.frames;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;

import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.imgcodecs.Imgcodecs;

import org.slf4j.Logger;
import jscast.streams.FrameFactory;
import jscast.utils.ImageConversion;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jscast.utils.Constants.FRAME_SERVER;

public class FrameProcessor {

    private static final Logger xlogger = LoggerFactory.getLogger(FRAME_SERVER);
    // the OpenCV object that performs the video capture
    private Imgcodecs imageCodecs;
    // break ups a stream into frames
    private FrameFactory frameFactory;
    private long currentFrame = 1L; //frame name starts at 1
    private int totalDigits = 0;
    private String fileNamePrefix = null;
    private String fileNameExt = null;
    private String destiny;
    // face cascade classifier
    private CascadeClassifier faceCascade = new CascadeClassifier();
    private int absoluteFaceSize;
    private ImageView originalFrame;
    private final Logger logger;
    private final String filePattern;
    private boolean captureFrames = false;
    private final long waitTime;
    private Thread capture = null;

    public FrameProcessor(String source, String destiny, String filePattern, String fps, long waitTime, Logger logger) {
        this.logger = logger;
        this.destiny = destiny;
        this.filePattern = filePattern;
        this.waitTime = waitTime;
        this.imageCodecs = new Imgcodecs();
        this.frameFactory = new FrameFactory(source, destiny, filePattern, fps, xlogger);
    }

    public void prepareStream() throws Exception {
        // load the classifier(s)
        if (!faceCascade.load("out/production/resources/haarcascades/haarcascade_frontalface_alt.xml")) {
            System.out.println("There was a problem loading the classifier");
            System.exit(1);
        }
        logger.debug("[MAIN] Calculating frames name");
        Pattern pattern = Pattern.compile("(\\w+)%([0-9]+)d(\\.\\w+)");
        Matcher matcher = pattern.matcher(filePattern);
        if (matcher.find()) {
            logger.info("Pattern found " + filePattern);
            fileNamePrefix = matcher.group(1);
            String index = matcher.group(2);
            fileNameExt = matcher.group(3);
            totalDigits = Integer.valueOf(index);
        } else {
            logger.info("Pattern not found " + filePattern);
        }
        logger.info("Prepare stream fragments");
        frameFactory.fragmentStream();
        logger.info("fragments done");
    }

    public void startFrameProcessing() {

        logger.debug("Starting frame processor");

        capture = new Thread() {
            long oldFrame = 1L;

            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    File file;
                    do {
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        file = new File(destiny, getCurrentFrameName(currentFrame));
                        System.out.println("waiting for " + file.getAbsolutePath());
                    } while (!file.canRead());

                    //was there a previous loop?
                    if (oldFrame != currentFrame) {
                        System.out.println("current frame [" + currentFrame + "] old frame[" + oldFrame + "]");
                        file = new File(destiny, getCurrentFrameName(oldFrame));
                        Mat frame = Imgcodecs.imread(file.getAbsolutePath());
                        // face detection
                        //detectAndDisplay(frame);
                        // convert and show the frame
                        //Image imageToShow = ImageConversion.mat2Image(frame);
                        //Imgcodecs.imwrite(destiny + File.separator + "p" + file.getName(), frame);
                        if (file.delete()) {
                            logger.debug("Removing frame " + oldFrame);
                        } else {
                            logger.warn("It was not possible to drop frame " + oldFrame);
                        }
                        oldFrame = currentFrame;
                    } else {
                        currentFrame++;
                    }
                }
            }
        };
        capture.start();
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        ImageConversion.onFXThread(view.imageProperty(), image);
    }

    private String getCurrentFrameName(long frame) {
        return fileNamePrefix + String.join("", Collections.nCopies((totalDigits - String.valueOf(frame).length()), "0")) + frame + fileNameExt;
    }

    /**
     * Method for face detection and tracking
     *
     * @param frame it looks for faces in this frame
     */
    private void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }
    }

    public void stopFrameProcessor() {
        if (capture != null) {
            logger.info("Stop frame processor");
            capture.interrupt();
            try {
                frameFactory.stopServer();
            } catch (IOException e) {
                //ignored
            }
            currentFrame = 0L;
        }
    }
}
