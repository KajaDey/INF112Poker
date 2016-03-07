package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by Jostein on 07.03.2016.
 */
public class MainScreen {

    public static Scene createSceneForMainScreen(String imageName){
        Stage window = new Stage();
        window.setTitle("Welcome to The Game!");

        BorderPane mainScreenLayout = new BorderPane();
        mainScreenLayout.setPadding(new Insets(10,10,10,10));
        mainScreenLayout.setCenter(makeLayout(window));

        Scene scene = new Scene(ImageViewer.setBackground(imageName, mainScreenLayout), 980, 551);

        window.setScene(scene);
        window.show();


        return scene;
    }

    public static HBox makeLayout(Stage window){

        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardButton = 75;

        HBox horisontalFull = new HBox();
        VBox verticalButtonAndChoiceBox = new VBox();

        TextField nameIn = new TextField();
        nameIn.setPromptText("Enter Name");
        nameIn.setFont(standardFont);
        nameIn.setPadding(standardPadding);
        nameIn.setMaxWidth(2*standardButton);

        TextField playersIn = new TextField();
        playersIn.setPromptText("Number of Players");
        playersIn.setFont(standardFont);
        playersIn.setPadding(standardPadding);
        playersIn.setMaxWidth(2*standardButton);

        Button enter = LayoutGenerators.makeStandardButton("Enter");
        enter.setMinWidth(2*standardButton);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setMinWidth(2*standardButton);
        choiceBox.getItems().addAll("Single Player", "Multi Player");
        choiceBox.setValue("Single Player");

        enter.setOnAction(e ->{
            window.close();
            ButtonListeners.mainScreenEnterListener(nameIn.getText(), playersIn.getText(), choiceBox);
        });

        String infoText = "SnakeOil";
        Label label = LayoutGenerators.makeStandardLabel(infoText, "");
        label.setTextFill(Color.web("#ffffff"));

        verticalButtonAndChoiceBox.getChildren().addAll(choiceBox, nameIn, playersIn, enter);
        verticalButtonAndChoiceBox.setAlignment(Pos.CENTER_LEFT);

        horisontalFull.getChildren().addAll(label, verticalButtonAndChoiceBox);
        horisontalFull.setAlignment(Pos.CENTER);

        return horisontalFull;
    }
}
