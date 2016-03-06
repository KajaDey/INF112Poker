package GUI.JosteinGui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

/**
 * Created by Jostein on 06.03.2016.
 */
public class TempMain extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{

        window.setTitle("Texas Hold'em");

        BorderPane completeLayout = new BorderPane();



        //Construct a new scene
        completeLayout.setBottom(LayoutGeneratorsTemp.makePlayerLayout());
        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();

    }
}
