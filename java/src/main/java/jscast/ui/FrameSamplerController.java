package jscast.ui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FrameSamplerController {

    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;

    /**
     * Init the controller, at start time
     */
    public void init() {
        // set a fixed width for the frame
        originalFrame.setFitWidth(600);
        // preserve image ratio
        originalFrame.setPreserveRatio(true);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    public void setClosed() {
        System.exit(0);
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param image the {@link Image} to show
     */
    public void updateImageView(Image image) {
        originalFrame.setImage(image);
    }

}
