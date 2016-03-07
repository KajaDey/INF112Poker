package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{

        window.setTitle("Texas Hold'em");

        BorderPane completeLayout = new BorderPane();
        completeLayout.setPadding(new Insets(10,10,10,10));

        //Construct a new scene
        completeLayout.setBottom(LayoutGenerators.makePlayerLayout());

        completeLayout.setTop(LayoutGenerators.makeOpponentLayout("file:CardSprites/_Back.png", "file:CardSprites/_Back.png"));

        /*Image image = new Image("file:CardSprites/_Info.png");
        BackgroundImage backgroundImage = new BackgroundImage(image, null, null, null, null);
        Background background = new Background(backgroundImage);
        completeLayout.setBackground(background);*/
        Scene scene = new Scene(completeLayout,800,800);

        window.setScene(scene);
        window.show();

    }
}
