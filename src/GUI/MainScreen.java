package GUI;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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

        Scene scene = new Scene(ImageViewer.setBackground(imageName, mainScreenLayout), 800, 800);

        window.setScene(scene);
        window.show();

        return scene;
    }

    public static void method2(){

    }

    public static void method3(){

    }
}
