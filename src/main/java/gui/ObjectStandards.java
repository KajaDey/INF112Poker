package main.java.gui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class ObjectStandards {

    private static DropShadow dropShadow = new DropShadow();

    /**
     * A template for a button
     * @param name The name of the button
     * @return a new button
     */
    public static Button makeStandardButton(String name){
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardMinWidth = 75;

        Button button = new Button(name);
        button.setFont(standardFont);
        button.setPadding(standardPadding);
        button.setMinWidth(standardMinWidth);

        //button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> button.setEffect(dropShadow));
        //button.addEventHandler(MouseEvent.MOUSE_EXITED, event -> button.setEffect(null));

        //button.setStyle("-fx-base: #bfbfbf;");

        button.setStyle("-fx-background-color:#090a0c, " +
                "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
                "linear-gradient(#20262b, #191d22), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
                "-fx-background-radius: 5,4,3,5; " +
                "-fx-background-insets: 0,1,2,0; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 ); " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0)");

        return button;
    }

    /**
     * Makes a standard label where the text color is white
     * @param name The name of the label
     * @param value The value of the label (i.e: $)
     * @return The created label
     */
    public static Label makeStandardLabelWhite(String name, String value){
        Label label = new Label(name + " " + value);
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);

        label.setFont(standardFont);
        label.setPadding(standardPadding);
        label.setTextFill(Color.web("#ffffff"));
        label.setEffect(dropShadow);

        return label;
    }


    /**
     * Makes a standard text label where the text color is black
     * @param name The name of the label
     * @param value The value of the label text (i.e: $)
     * @return The created label
     */
    public static Label makeStandardLabelBlack(String name, String value){
        Label label = new Label(name + " " + value);
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);

        label.setFont(standardFont);
        label.setPadding(standardPadding);

        return label;
    }


    /**
     * Creates a standard text field for input
     * @return The text field
     */
    public static TextField makeStandardTextField(){
        TextField textField = new TextField();

        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,5,5,5);
        int standardButton = 75;

        textField.setFont(standardFont);
        textField.setPadding(standardPadding);
        textField.setMaxWidth(standardButton);
        //textField.setEffect(dropShadow);

        textField.setStyle("-fx-background-color:#090a0c, " +
                "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
                "linear-gradient(#20262b, #191d22), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
                "-fx-background-radius: 5,4,3,5; " +
                "-fx-background-insets: 0,1,2,0; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 ); " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0)");
        textField.setStyle("-fx-text-fill: white");

        return textField;
    }
}
