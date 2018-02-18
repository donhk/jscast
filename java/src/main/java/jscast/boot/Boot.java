package jscast.boot;

import jscast.control.CameraWorker;
import jscast.pojos.onvif.Camera;
import jscast.devices.DeviceScanner;
import org.opencv.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static jscast.utils.Constants.GLOBAL_LOGGER;

public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(GLOBAL_LOGGER);


    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            //scan read looking for cameras
            String port = "6015";
            String localhost = "localhost";
            DeviceScanner scanner = new DeviceScanner(localhost, port, logger);
            Camera[] cameras = scanner.initDevices();
            logger.info(cameras.length + " cameras found");
            if (cameras.length == 0) {
                logger.info("There were no cameras found");
                System.exit(0);
            }
            //this should be a loop, for now let's use only one
            //Test code
            Camera testCam = cameras[0];
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(new CameraWorker(testCam, localhost, port, logger));
            executor.awaitTermination(5000, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
