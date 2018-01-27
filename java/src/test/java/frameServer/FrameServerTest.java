package frameServer;

import jscast.streams.FrameFactory;

import org.junit.Test;

public class FrameServerTest {


    @Test
    public void frames() {
        FrameFactory frameFactory = new FrameFactory("rtsp://192.168.0.13:554/onvif1","C:\\Users\\hkfre\\Desktop\\tmp\\fram%15d.jpg");
        System.out.println("This is a test");
    }
}
