package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;


/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage window) throws Exception{

        window.setTitle("Texas Hold'em");

        //Make a new grid layout
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));

        //Make the objects we want in the layout
        /*Image card1 = new Image("/Clubs 1.png");
        ImageView view1 = new ImageView(card1);
        GridPane.setConstraints(view1,4,5);*/

        TextField betAmount = new TextField();
        betAmount.setPromptText("Amount to bet");
        GridPane.setConstraints(betAmount, 5, 6);

        Button bet = new Button("Place bet");
        GridPane.setConstraints(bet,6,6);



        //Add all the elements to the layout
        layout.getChildren().addAll(bet,betAmount);

        //Construct a new scene
        Scene scene = new Scene(layout,500,500);

        window.setScene(scene);
        window.show();

    }
}
