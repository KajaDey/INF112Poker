package gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * Created by ady on 05/03/16.
 */
public class ImageViewer {
    public enum Image_type { DEALER_BUTTON, CARD_BACK, PLAYER, OPPONENT }

    private static Image backImage;

    static {
        backImage = new Image("file:resources/CardSprites/_Back.png");
    }

    /**
     * Gets the name of a card, and returns the URL of the given card.
     * @param card The name of the card
     * @return The URL for the card
     */
    public static String returnURLPathForCardSprites(String card){
        String cardOut = "file:resources/CardSprites/" + card + ".png";
        return cardOut;
    }


    /**
     * Gets the name of an image, and returns the URL of the given image.
     * @param image The name of the image
     * @return The URL for the image
     */
    public static String returnURLPathForImages(String image){
        String ImageOut = "file:resources/Images/" + image + ".png";
        return ImageOut;
    }

    /**
     * Sets an image as a background for a given BorderPane
     * @param imageIn The image to be set as background
     * @param borderPane The new pane. Contains the background
     * @return The new BorderPane
     */
    public static Pane setBackground(String imageIn, Pane borderPane, int sizeX, int sizeY){
        Image image = new Image(returnURLPathForImages(imageIn));

        BackgroundSize backgroundSize = new BackgroundSize(sizeX,sizeY, false, false, true, true);

        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background background = new Background(backgroundImage);
        borderPane.setBackground(background);

        return borderPane;
    }

    /**
     * Returns an empty image view, with the correct image size.
     * @param type The type of player. Either a PLAYER or OPPONENT.
     * @return The total image view.
     */
    public static ImageView getEmptyImageView(Image_type type){
        int imageSize = 0;
        if (type == Image_type.PLAYER)
            imageSize = 130;
        else if (type == Image_type.OPPONENT)
            imageSize = 100;
        else
            return null;

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(imageSize);

        return imageView;
    }

    public static Image getChipImage(String name){
        String chipOut = "file:resources/Images/" + name + ".png";
        if (chipOut.isEmpty())
            return null;

        return new Image(chipOut);
    }

    public static Image getImage(Image_type type) {
        switch (type) {
            case DEALER_BUTTON:
                return new Image("file:resources/Images/dealer.png");
            case CARD_BACK:
                return backImage;
            default:
                return null;
        }
    }
}
