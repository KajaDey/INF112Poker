package main.java.gui;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by ady on 08/03/16.
 */
public class SceneBuilder {

    static Stage window = new Stage();

    /**
     * Displays a given layout
     * @param pane The scene to display
     * @param titleIn The title of the scene
     */
    public static void showCurrentScene(Pane pane, String titleIn){

        window.close();
        window = new Stage();
        window.setTitle(titleIn);
        window.setScene(new Scene(ImageViewer.setBackground("PokerTable", pane, 1920, 1080),1280,720));
        window.show();
    }

    /**
     * Displays a given scene. Only used for GameScreen
     * @param scene The scene to display
     * @param titleIn The title of the scene
     */
    public static void showCurrentScene(Scene scene, String titleIn){

        window.close();
        window = new Stage();
        window.setTitle(titleIn);
        window.setScene(scene);
        window.show();
    }
}
