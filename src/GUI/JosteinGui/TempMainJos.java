package GUI.JosteinGui;

import GUI.LayoutGenerators;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

/**
 * Created by Jostein on 06.03.2016.
 */
public class TempMainJos extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{
        //hsifhsd
        window.setTitle("Texas Hold'em");

        BorderPane completeLayout = new BorderPane();

        //Construct a new scene
        completeLayout.setBottom(LayoutGeneratorsJos.makePlayerLayout());
        completeLayout.setTop(LayoutGeneratorsJos.makeOpponentLayout("file:CardSprites/_Back.png"));
        //Scene scene = new Scene(completeLayout,1000,1000);



        completeLayout.setTop(LayoutGeneratorsJos.makeOpponentLayout(ImageViewerJos.findSprite("Clubs 1")));
        Scene scene = new Scene(completeLayout,1000,1000);

        window.setScene(scene);
        window.show();

    }
}
