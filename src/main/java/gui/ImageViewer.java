package main.java.gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

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
     * @param image The name of the image
     * @return The URL for the image
     */
    public static String returnURLPathForImages(String image){
        String ImageOut = "file:Images/" + image + ".png";
        return ImageOut;
    }

    /**
     * Shows the community cards
     * @param cardPlacement The table placement of the cards
     * @param cardName The name of the card
     */
    public void showCommunityCard(int cardPlacement, String cardName){
        //TODO: Make it possible for the community cards to be shown
        ImageView card = ImageViewer.setCardImage("player", cardName);

    }


    /**
     * Sets an image as a background for a given BorderPane
     * @param imageIn The image to be set as background
     * @param borderPane The new pane. Contains the background
     * @return The new BorderPane
     */
    public static BorderPane setBackground(String imageIn, BorderPane borderPane, int sizeX, int sizeY){
        Image image = new Image(returnURLPathForImages(imageIn));

        BackgroundSize backgroundSize = new BackgroundSize(sizeX,sizeY, false, false, true, true);

        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
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

    /**
     * Returns an empty image view, with the correct image size.
     * @param player The type of player. Either a player or an opponent.
     * @return The total image view.
     */
    public static ImageView getEmptyImageView(String player){
        int imageSize = 0;
        if (player.equals("player"))
            imageSize = 130;
        else if (player.equals("opponent"))
            imageSize = 100;

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(imageSize);

        return imageView;
    }
}
