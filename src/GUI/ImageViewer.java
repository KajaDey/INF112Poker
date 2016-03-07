package GUI;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

/**
 * Created by ady on 05/03/16.
 */
public class ImageViewer {

    public static String returnURLPathForSprites(String card){
        String cardOut = "file:CardSprites/" + card + ".png";
        return cardOut;
    }

    public static Scene showOpponentCards(String card1, String card2){
        String card1Out = returnURLPathForSprites(card1);
        String card2Out = returnURLPathForSprites(card2);

        return LayoutGenerators.makeSceneForOpponentCards(card1Out, card2Out);
    }

    public void showCommunityCard(){
        //TODO: Make it possible for the community cards to be shown
    }

    public static BorderPane setBackground(){
        //TODO: Make it possible for backgrounds to be changed dynamically
        return null;
    }
}
