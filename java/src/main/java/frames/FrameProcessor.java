package frames;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;

import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.imgcodecs.Imgcodecs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import streams.FrameFactory;
import utils.ImageConversion;

import java.io.File;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Constants.GLOBAL_LOGGER;

public class FrameProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);
    // the OpenCV object that performs the video capture
    private Imgcodecs imageCodecs;
    // break ups a stream into frames
    private FrameFactory frameFactory;
    private long currentFrame = 1L; //frame name starts at 1
    private int totalDigits = 0;
    private String fileNamePrefix = null;
    private String fileNameExt = null;
    private String path = "C:\\Users\\hkfre\\Desktop\\tmp";
    // face cascade classifier
    private CascadeClassifier faceCascade = new CascadeClassifier();
    private int absoluteFaceSize;
    private ImageView originalFrame;

    public FrameProcessor(String source, String destiny) {
        this.imageCodecs = new Imgcodecs();
        this.frameFactory = new FrameFactory(source, destiny);
    }

    public void prepareStream() {
        // load the classifier(s)
        if (!faceCascade.load("out/production/resources/haarcascades/haarcascade_frontalface_alt.xml")) {
            System.out.println("There was a problem loading the classifier");
            System.exit(1);
        }
        logger.debug("[MAIN] Calculating frames name");
        String filename = "fram%15d.jpg";
        Pattern pattern = Pattern.compile("(\\w+)%([0-9]+)d(\\.jpg)");
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            fileNamePrefix = matcher.group(1);
            String index = matcher.group(2);
            fileNameExt = matcher.group(3);
            totalDigits = Integer.valueOf(index);
        }
        logger.debug("[MAIN] Preparing stream");
        Thread stream = new Thread(() -> {
            try {
                frameFactory.fragmentStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        logger.debug("Starting stream tool");
        //stream.start();
    }

    public void startFrameProcessing() throws InterruptedException {
        logger.debug("Starting frame processor");
        while (currentFrame < 50000000L) {
            File file;
            do {
                Thread.sleep(70);
                file = new File(path, getCurrentFrameName());
                logger.debug("waiting for frame " + path + File.separator + getCurrentFrameName() + " to be created");
            } while (!file.canRead());
            System.out.println("current frame name " + file.getAbsolutePath() + " " + file.length());
            // start the video capture
            Mat frame = Imgcodecs.imread(file.getAbsolutePath());
            // face detection
            detectAndDisplay(frame);
            // convert and show the frame
            Image imageToShow = ImageConversion.mat2Image(frame);
            Imgcodecs.imwrite(path + File.separator + "p" + file.getName(), frame);
            //updateImageView(originalFrame, imageToShow);
            currentFrame++;
            file.delete();
        }
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

    private String getCurrentFrameName() {
        return fileNamePrefix + String.join("", Collections.nCopies((totalDigits - String.valueOf(currentFrame).length()), "0")) + currentFrame + fileNameExt;
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
}
