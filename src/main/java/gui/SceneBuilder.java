package main.java.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by ady on 08/03/16.
 */
public class SceneBuilder {

    static Stage window = new Stage();

    public static void showCurrentScene(Scene scene, String titleIn){

        window.close();
        window = new Stage();
        window.setTitle(titleIn);
        window.setScene(scene);
        window.show();

    }

    public static void updateGameScreen(GUIClient client){



    }


    public static Scene createSceneForInitialScreen(String imageName,GUIClient client){
        Stage window = new Stage();
        window.setTitle("Welcome to The Game!");

        BorderPane mainScreenLayout = new BorderPane();
        mainScreenLayout.setPadding(new Insets(10,10,10,10));
        mainScreenLayout.setCenter(MainScreen.makeLayout(window,client));

        Scene scene = new Scene(ImageViewer.setBackground(imageName, mainScreenLayout, 1920, 1080), 1280, 720);

        return scene;
    }

}
