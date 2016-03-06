package GUI;

import javafx.scene.Scene;

/**
 * Created by ady on 05/03/16.
 */
public class ImageViewer {

    public static String findSpritePath(String card){
        String cardOut = "file:CardSprites/" + card + ".png";

        return cardOut;
    }

    public static Scene showOpponentCards(String card1, String card2){
        //TODO: Make code
        String card1Out = findSpritePath(card1);
        String card2Out = findSpritePath(card2);

        return LayoutGenerators.makeScene(card1Out, card2Out);
    }

    public void method2(){
        //TODO: Make code
    }

    public void method3(){
        //TODO: Make code
    }
}
