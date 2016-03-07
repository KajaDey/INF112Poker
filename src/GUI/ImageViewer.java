package GUI;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;

/**
 * Created by ady on 05/03/16.
 */
public class ImageViewer {

    /**
     * Gets the name of a card, and returns the URL of the given card.
     * @param card The name of the card
     * @return The URL for the card
     */
    public static String returnURLPathForCardSprites(String card){
        String cardOut = "file:CardSprites/" + card + ".png";
        return cardOut;
    }


    /**
     * Gets the name of an image, and returns the URL of the given image.
     * @param card The name of the image
     * @return The URL for the image
     */
    public static String returnURLPathForImages(String card){
        String cardOut = "file:CardSprites/" + card + ".png";
        return cardOut;
    }

    /**
     * Shows the cards of the opponent
     * @param card1 The first card
     * @param card2 The second card
     * @return The new layout which contains the shown cards.
     */
    public static Scene showOpponentCards(String card1, String card2){
        String card1Out = returnURLPathForCardSprites(card1);
        String card2Out = returnURLPathForCardSprites(card2);

        return LayoutGenerators.makeSceneForOpponentCards(card1Out, card2Out);
    }

    /**
     * Show community cards
     */
    public void showCommunityCard(){
        //TODO: Make it possible for the community cards to be shown
    }


    /**
     * Sets an image as a background for a given BorderPane
     * @param imageIn The image to be set as background
     * @param borderPane The new pane. Contains the background
     * @return The new BorderPane
     */
    public static BorderPane setBackground(String imageIn, BorderPane borderPane){
        Image image = new Image(returnURLPathForImages(imageIn));
        BackgroundImage backgroundImage = new BackgroundImage(image, null, null, null, null);
        Background background = new Background(backgroundImage);
        borderPane.setBackground(background);

        return borderPane;
    }

    /**
     * Method for setting the image of a given card
     * @param player A string containing info of the player. Is either "player" or "opponent"
     * @param imageName The name of the card
     * @return An ImageView containing the image of the card.
     */
    public static ImageView setCardImage(String player, String imageName){
        int imageSize = 0;
        if (player.equals("player"))
            imageSize = 130;
        else if (player.equals("opponent"))
            imageSize = 100;

        Image image = new Image(returnURLPathForCardSprites(imageName));
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(imageSize);

        return imageView;
    }
}
