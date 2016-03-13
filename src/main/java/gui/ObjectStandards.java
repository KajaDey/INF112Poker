package main.java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class ObjectStandards {

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

        return textField;
    }
}
