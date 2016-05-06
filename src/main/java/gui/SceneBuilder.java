package gui;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * This class displays all the layouts to the screen
 *
 * @author André Dyrstad
 */
public class SceneBuilder {

    private static Stage window;

    static {
        window = new Stage();
    }

    /**
     * Displays a given layout
     * @param pane The scene to display
     * @param titleIn The title of the scene
     */
    public static void showCurrentScene(Pane pane, String titleIn){

        window.close();
        window = new Stage();
        window.setOnCloseRequest(e -> System.exit(0));
        window.setTitle(titleIn);
        window.setScene(new Scene(ImageViewer.setBackground("table&background", pane, 1920, 1080),1280,720));
        window.setResizable(false);
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
        window.setOnCloseRequest(e -> System.exit(0));
        window.setTitle(titleIn);
        window.setScene(scene);
        window.setResizable(false);
        window.show();
    }
}
