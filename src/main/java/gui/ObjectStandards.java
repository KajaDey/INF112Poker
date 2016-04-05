package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ady on 05/03/16.
 */
public class ObjectStandards {

    private static DropShadow dropShadow = new DropShadow();

    private static String styling = "-fx-background-color:#090a0c, " +
            "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
            "linear-gradient(#20262b, #191d22), " +
            "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
            "-fx-background-radius: 5,4,3,5; " +
            "-fx-background-insets: 0,1,2,0; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 ); " +
            "-fx-text-fill: linear-gradient(white, #d0d0d0)";


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

        button.setStyle(styling);

        return button;
    }

    /**
     * Makes a button for the lobby screen
     * @param name The name of the button
     * @return The button to be displayed
     */
    public static Button makeButtonForLobbyScreen(String name){
        Button button = new Button(name);
        Insets padding = new Insets(5,5,5,5);
        button.setPadding(padding);
        button.setFont(new Font("Areal", 15));
        button.setMinWidth(120);
        button.setMaxWidth(120);
        button.setMinHeight(50);
        button.setMaxHeight(50);

        button.setStyle(styling);

        return button;

    }

    /**
     * Makes a label for different headlines
     * @param name The name of the label
     * @return The label
     */
    public static Label makeLabelForHeadLine(String name){
        Label label = new Label(name);
        Insets padding = new Insets(20,20,20,20);

        label.setFont(new Font("Areal", 30));
        label.setPadding(padding);
        label.setTextFill(Color.web("#ffffff"));
        label.setEffect(dropShadow);

        return label;
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
        Insets standardPadding = new Insets(0,0,0,0);

        label.setFont(standardFont);
        label.setPadding(standardPadding);
        label.setTextFill(Color.web("#ffffff"));
        label.setEffect(dropShadow);

        return label;
    }

    /**
     * Makes a standard label where the text color is white
     * @param name The name of the label
     * @param value The value of the label (i.e: $)
     * @return The created label
     */
    public static Label makeLobbyLabelWhite(String name, String value){
        Label label = new Label(name + " " + value);
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(5,10,5,10);

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
     * Makes a standard text label where the text color is black
     * @param name The name of the label
     * @return The created label
     */
    public static Label makeLabelForSettingsScreen(String name){
        Label label = new Label(name + " ");
        Font standardFont = new Font("Areal",15);
        Insets standardPadding = new Insets(7,7,7,7);

        label.setFont(standardFont);
        label.setPadding(standardPadding);

        return label;
    }

    /**
     * Makes a text field for the main screen. The text field is wide, and has styling.
     * @param promptText The text for the prompt in the text field
     * @return The text field to be displayed
     */
    public static TextField makeTextFieldForMainScreen(String promptText){
        Insets padding = new Insets(5,5,5,5);

        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setFont(new Font("Areal", 15));
        textField.setMinWidth(150);
        textField.setMaxWidth(150);
        textField.setPadding(padding);

        return textField;
    }

    /**
     * Makes a text field for the settings screen. The text field is narrow, and has no styling.
     * @return The text field to be displayed.
     */
    public static TextField makeTextFieldForSettingsScreen(){
        Insets padding = new Insets(5,5,8,5);

        TextField textField = new TextField();
        textField.setFont(new Font("Areal", 15));
        textField.setMaxWidth(75);
        textField.setPadding(padding);

        return textField;
    }

    /**
     * Makes a text field for the game screen. The text field is wide, and has styling.
     * @param promptText The prompt text to be shown
     * @return The text field to be displayed.
     */
    public static TextField makeTextFieldForGameScreen(String promptText){
        Insets padding = new Insets(5,5,5,5);

        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setFont(new Font("Areal", 15));
        textField.setMaxWidth(150);
        textField.setPadding(padding);

        return textField;
    }
}
