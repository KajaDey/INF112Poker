package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    public static void run(String[] args){
        launch(args);
    }

    public void start(Stage window) throws Exception{
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable"));
    }
}
