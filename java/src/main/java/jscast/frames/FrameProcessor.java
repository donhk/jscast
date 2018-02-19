package jscast.frames;

import javafx.scene.image.Image;
import jscast.ui.FrameSamplerController;
import org.opencv.core.*;

import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.imgcodecs.Imgcodecs;

import org.slf4j.Logger;
import jscast.streams.FrameFactory;
import jscast.utils.FrameTools;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jscast.utils.Constants.FRAME_SERVER;

public class FrameProcessor extends Observable {

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
    private final Logger logger;
    private final String filePattern;
    private final long waitTime;
    private Thread capture = null;
    private FrameSamplerController gui = null;
    private Rect hotArea;
    private Rect mainArea;
    private Point center;

    public FrameProcessor(String source,
                          String destiny,
                          String filePattern,
                          String fps,
                          long waitTime,
                          Rect mainArea,
                          Rect hotArea,
                          Point center,
                          Logger logger) {
        this.logger = logger;
        this.destiny = destiny;
        this.filePattern = filePattern;
        this.waitTime = waitTime;
        this.imageCodecs = new Imgcodecs();
        this.frameFactory = new FrameFactory(source, destiny, filePattern, fps, mainArea.width, mainArea.height, xlogger);
        this.hotArea = hotArea;
        this.mainArea = mainArea;
        this.center = center;
    }

    private String classifierPath(String classifierName) throws IOException {
        return new File(getClass().getResource("/haarcascades/" + classifierName).getFile()).getCanonicalPath();
    }

    public void prepareStream() throws Exception {
        // load the classifier(s)
        if (!faceCascade.load(classifierPath("haarcascade_frontalface_alt.xml"))) {
            System.out.println("There was a problem loading the classifier");
            System.exit(1);
        }
        if (!faceCascade.load(classifierPath("haarcascade_frontalface_alt_tree.xml"))) {
            System.out.println("There was a problem loading the classifier");
            System.exit(1);
        }
        if (!faceCascade.load(classifierPath("haarcascade_frontalface_alt2.xml"))) {
            System.out.println("There was a problem loading the classifier");
            System.exit(1);
        }
        if (!faceCascade.load(classifierPath("haarcascade_frontalface_default.xml"))) {
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

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    File file = new File(destiny, getCurrentFrameName(currentFrame));
                    while (!file.canRead()) {
                        logger.debug("waiting for " + file.getAbsolutePath());
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        file = new File(destiny, getCurrentFrameName(currentFrame));
                    }

                    //was there a previous loop?
                    if (oldFrame != currentFrame) {
                        logger.debug("current frame [" + currentFrame + "] old frame[" + oldFrame + "]");
                        file = new File(destiny, getCurrentFrameName(oldFrame));
                        Mat frame = Imgcodecs.imread(file.getAbsolutePath());

                        //put some marks
                        freeDraw(frame);
                        // face detection
                        detectAndDisplay(frame);
                        // convert and show the frame
                        Image imageToShow = FrameTools.mat2Image(frame);
                        //Imgcodecs.imwrite(destiny + File.separator + "p" + file.getName(), frame);
                        if (gui != null) {
                            gui.updateImageView(imageToShow);
                        }
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
        capture.setName("FrameCapture");
        capture.start();
    }

    private String getCurrentFrameName(long frame) {
        return fileNamePrefix + String.join("", Collections.nCopies((totalDigits - String.valueOf(frame).length()), "0")) + frame + fileNameExt;
    }

    private void freeDraw(Mat frame) {
        Imgproc.rectangle(frame, hotArea.tl(), hotArea.br(), new Scalar(255, 0, 102), 3);
        Imgproc.rectangle(frame, mainArea.tl(), mainArea.br(), new Scalar(0, 255, 0), 3);
        FrameTools.profileTarget(frame, mainArea);
    }

    /**
     * Method for face detection and tracking
     *
     * @param frame it looks for faces in this frame
     */
    private void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        Rect fullArea = new Rect(0, 0, frame.width(), frame.height());

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
        faceCascade.detectMultiScale(
                grayFrame,
                faces,
                1.3,
                2,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size()
        );

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();

        for (Rect face : facesArray) {
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 3);
            FrameTools.profileTarget(frame, face);
            FrameTools.measurePoints(frame, FrameTools.getCenter(face), center);
        }

        //only for 1 for now
        if (facesArray.length > 0) {
            setChanged();
            notifyObservers(facesArray);
        }
    }

    public void stopFrameProcessor() {
        if (capture != null) {
            logger.info("Stop frame processor");
            capture.interrupt();
            frameFactory.stopServer();
            currentFrame = 0L;
        }
    }

    public FrameSamplerController getGui() {
        return gui;
    }

    public void setGui(FrameSamplerController gui) {
        this.gui = gui;
    }
}
