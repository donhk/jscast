package jscast.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FrameSampler extends Application implements Runnable {

    private static FrameSamplerController controller = null;

    @Override
    public void start(Stage primaryStage) {
        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmview.fxml"));
            BorderPane root = loader.load();
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 800, 600);
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("Frame sampler");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // init the controller
            controller = loader.getController();
            controller.init();

            // set the proper behavior on closing the application
            primaryStage.setOnCloseRequest((we) -> controller.setClosed());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        launch();
    }

    public FrameSamplerController getController() {
        return controller;
    }
}
