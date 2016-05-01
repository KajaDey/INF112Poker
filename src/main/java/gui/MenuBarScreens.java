package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;

/**
 * Created by Jostein on 09.04.2016.
 */
public class MenuBarScreens {

    private static String styling;

    static {
        styling = "-fx-background-color:#090a0c, " +
                "linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%), " +
                "linear-gradient(#20262b, #191d22), " +
                "radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0)); " +
                "-fx-text-fill: white; " +
                "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 ); " +
                "-fx-text-fill: linear-gradient(white, #d0d0d0)";
    }

    public void createScreenForLicense(String licenseType) throws IOException {
        Stage window = new Stage();
        Pane pane = new Pane();
        pane.styleProperty().setValue(styling);
        Label label = new Label("");
        Scene scene = null;

        if (licenseType.startsWith("Software")) {
            scene = new Scene(pane, 615, 500);
            label = ObjectStandards.makeStandardLabelWhite(licenseReader("resources/softwareLicense.txt"), "");
            window.setTitle("Software License");
        }
        else if (licenseType.startsWith("Card")){
            scene = new Scene(pane, 615, 275);
            label = ObjectStandards.makeStandardLabelWhite(licenseReader("resources/cardSpriteLicense.txt"), "");
            window.setTitle("Card Sprite License");
        }

        label.setPadding(new Insets(10,10,10,10));
        pane.getChildren().add(label);

        window.setResizable(false);
        window.setAlwaysOnTop(true);
        window.setScene(scene);
        window.initModality(Modality.APPLICATION_MODAL);
        window.show();

    }

    public String licenseReader(String fileName) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String out = "";
        while (true){
            String line = bufferedReader.readLine();
            if (line == null){
                break;
            }
            out += line + "\n";
        }
        return out;
    }
}
