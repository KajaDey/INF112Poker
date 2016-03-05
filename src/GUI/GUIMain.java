package GUI;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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

        //Make a new grid layout
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));

        //Make the objects we want in the layout
        Button bet = new Button("Place bet");
        GridPane.setConstraints(bet,6,6);

        TextField betAmount = new TextField();
        betAmount.setPromptText("Amount to bet");
        GridPane.setConstraints(betAmount, 5, 6);

        //Add all the elements to the layout
        layout.getChildren().addAll(bet,betAmount);

        //Construct a new scene
        Scene scene = new Scene(layout,500,500);

        window.setScene(scene);
        window.show();

    }
}
