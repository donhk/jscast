package jscast.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FrameTools {
    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        } catch (Exception e) {
            System.err.println("Cannot convert the Mat object: " + e);
            return null;
        }
    }

    public static void profileTarget(Mat frame, Rect target) {
        Point center = getCenter(target);
        //horizontal
        Point l1p1 = new Point(target.x, center.y);
        Point l1p2 = new Point(((double) target.width) + target.x, center.y);
        //vertical
        Point l2p1 = new Point(center.x, target.y);
        Point l2p2 = new Point(center.x, ((double) target.height) + target.y);
        //refresh frame
        Imgproc.line(frame, l1p1, l1p2, new Scalar(255, 0, 255), 2);
        Imgproc.line(frame, l2p1, l2p2, new Scalar(255, 0, 255), 2);
        Imgproc.circle(frame, center, 2, new Scalar(255, 255, 0), 8);
    }

    public static void measurePoints(Mat frame, Point a, Point b) {
        Imgproc.line(frame, a, b, new Scalar(51, 255, 153), 2);
        Imgproc.putText(
                frame,
                String.valueOf(distanceBetweenPoints(a, b)),
                middlePoint(a, b),
                1,
                4,
                new Scalar(51, 255, 153),
                5
        );
    }

    /**
     * TODO KdTree to search
     * http://pointclouds.org/documentation/tutorials/kdtree_search.php
     *
     * @param targets
     * @return
     */
    public static Point keyPoint(Rect[] targets) {
        Point point = null;
        for (Rect target : targets) {
            if (point == null) {
                point = getCenter(target);
            } else {
                point = middlePoint(point, getCenter(target));
            }
        }
        return point;
    }

    public static double distanceBetweenPoints(Point a, Point b) {
        double xDiff = a.x - b.x;
        double yDiff = a.y - b.y;
        return Math.round(Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)));
    }

    public static Point middlePoint(Point a, Point b) {
        double mx = (a.x + b.x) / 2;
        double my = (a.y + b.y) / 2;
        return new Point(mx, my);
    }

    public static Rect calculateHotArea(Rect target, Point center, double pct) {
        //calculate rectangle used to measure relative position
        double hotW = target.width * pct;
        double hotH = target.height * pct;
        double correctionX = hotW * 0.5;
        double correctionY = hotH * 0.5;
        double rx = center.x - correctionX;
        double ry = center.y - correctionY;
        return new Rect((int) rx, (int) ry, (int) hotW, (int) hotH);
    }

    public static Point getCenter(Rect target) {
        return new Point((target.width / 2) + target.x, (target.height / 2) + target.y);
    }

    /**
     * Support for the  mat2image() method
     *
     * @param original the {@link Mat} object in BGR or grayscale
     * @return the corresponding {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}
