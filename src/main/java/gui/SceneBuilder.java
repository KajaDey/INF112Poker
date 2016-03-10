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

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(GameScreen.makeBoardLayout(client));
        borderPane.setBottom(GameScreen.makePlayerLayout(client));
        borderPane.setTop(GameScreen.makeOpponentLayout(client));

    }


    public static void updateLobbyScreen(GameSettings gameSettings){
       GameLobby.createScreenForGameLobby(gameSettings);
    }


    public static Scene createSceneForInitialScreen(String imageName){
        Stage window = new Stage();
        window.setTitle("Welcome to The Game!");

        BorderPane mainScreenLayout = new BorderPane();
        mainScreenLayout.setPadding(new Insets(10,10,10,10));
        mainScreenLayout.setCenter(MainScreen.makeLayout(window));

        Scene scene = new Scene(ImageViewer.setBackground(imageName, mainScreenLayout, 1920, 1080), 1280, 720);

        return scene;
    }

}
