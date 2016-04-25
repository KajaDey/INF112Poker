package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The meaning of this class is to create standard gui objects that will be used throughout the
 * entire application.
 *
 * The standard objects made in this class are:
 *  -buttons
 *  -labels
 *  -text fields
 *  -menu bar
 *
 * @author Jostein Kringlen
 * @author André Dyrstad
 */
public class ObjectStandards {

    private static DropShadow dropShadow;
    private static String styling;

    static {
        dropShadow = new DropShadow();
        styling = "-fx-background-color:#090a0c, " +
                "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
                "linear-gradient(#20262b, #191d22), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
                "-fx-background-radius: 5,4,3,5; " +
                "-fx-background-insets: 0,1,2,0; " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 ); " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0)";
    }

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
        textField.setMaxWidth(100);
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

    /**
     * Creates a menu bar for the application.
     * If the user is using Windows, there program will create a menu bar inside the application,
     * but if he/she is using another OS like OSX or one of the Linux distros, the menu bar
     * will be integrated into the standard system menu bar (i.e. the top line on OSX)
     * @return The created menu bar
     */
    public static MenuBar createMenuBar(){
        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");

        //Parent menus
        Menu file = new Menu("File");
        Menu about = new Menu("About");

        //Sub menus and menu items
        Menu licenses = new Menu("Licenses");
        MenuItem softwareLicense = new MenuItem("Software License");
        MenuItem cardSpriteLicense = new MenuItem("Card Sprite License");
        MenuItem aboutTexasHoldem = new MenuItem("Texas Hold'em");

        MenuItem quit = new MenuItem("Quit");
        MenuItem restart = new MenuItem("Restart");
        MenuItem mute = new MenuItem("Mute Sound");

        //Adding sub menus and items to parent menus
        licenses.getItems().addAll(softwareLicense,cardSpriteLicense);
        about.getItems().addAll(licenses, aboutTexasHoldem);
        file.getItems().addAll(quit,restart, mute);

        //Adding all menus to the menu bar
        menuBar.getMenus().addAll(file,about);

        if (os != null) {
            if (!os.startsWith("Mac")) {
                menuBar.setUseSystemMenuBar(false);
                menuBar.setMinWidth(1280);
                menuBar.setMinWidth(1280);
                menuBar.setMinHeight(25);
                menuBar.setMaxHeight(25);
                menuBar.getStylesheets().addAll("file:resources/windowsMenuBarStyling.css");
            }
            else
                menuBar.setUseSystemMenuBar(true);
        }

        //Event listeners for the menu items
        quit.setOnAction(event -> System.exit(0));

        MenuBarScreens menuBarScreens = new MenuBarScreens();
        mute.setOnAction(event ->{
            new SoundPlayer().muteSound();
            if (new SoundPlayer().getMutedValue())
                mute.setText("Unmute Sound");
            else mute.setText("Mute Sound");
        });


        softwareLicense.setOnAction(event -> {
            try {
                menuBarScreens.createScreenForLicense(softwareLicense.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cardSpriteLicense.setOnAction(event -> {
            try {
                menuBarScreens.createScreenForLicense(cardSpriteLicense.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        aboutTexasHoldem.setOnAction(event -> {
            assert os != null;
            if (os.contains("nix") || os.contains("nux")){
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("xdg-open https://en.wikipedia.org/wiki/Texas_hold_%27em");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/Texas_hold_%27em"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        restart.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        mute.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN));

        File[] files = new File(System.getProperty("user.dir")).listFiles();
        restart.setOnAction(event -> {
            try {
                startNewInstanceOfGame(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return menuBar;
    }

    /**
     * When restarting the game, it will search for the jar-file in either target/ (if run in intellij)
     * or in the directory of the jar (if the jar is run directly).
     * It will then start new process containing the jar file, and exit the already running program
     * by using System.exit(0)
     */
    public static void startNewInstanceOfGame(File[] files) throws IOException {
        Process process;
        for (File file : files) {
            if (file.isDirectory()) {
                startNewInstanceOfGame(file.listFiles());
            } else if (file.getName().contains("Poker.jar")){
                process = Runtime.getRuntime().exec("java -jar " + file.getParent() + "/" + file.getName());
                System.exit(0);
            }
        }

    }
}
