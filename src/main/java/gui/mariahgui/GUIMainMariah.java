package main.java.gui.mariahgui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMainMariah extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{

        window.setTitle("Texas Hold'em");

        BorderPane completeLayout = new BorderPane();

        //Construct a new scene
        completeLayout.setBottom(LayoutGeneratorsMariah.makePlayerLayout());
        completeLayout.setTop(LayoutGeneratorsMariah.makeOpponentLayout());
        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();

    }
}
