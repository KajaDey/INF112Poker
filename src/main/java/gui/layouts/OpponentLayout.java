package gui.layouts;

import gui.ImageViewer;
import gui.ObjectStandards;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by ady on 04/04/16.
 */
public class OpponentLayout {

    DropShadow dropShadow = new DropShadow();

    private Label nameLabel, stackSizeLabel, positionLabel, lastMoveLabel;
    private ImageView leftCardImage, rightCardImage;


    public OpponentLayout(){

    }

    /**
     * Makes the layout for the opponentScreen
     *
     * @param userID
     * @param name
     * @param stackSize
     * @return a layout
     */
    public VBox updateLayout(int userID, String name, long stackSize) {
        leftCardImage = ImageViewer.getEmptyImageView("opponent");
        rightCardImage = ImageViewer.getEmptyImageView("opponent");

        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);

        nameLabel = ObjectStandards.makeStandardLabelWhite("Name:", name);
        stackSizeLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("Position: ","");
        lastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");

        HBox cardsAndStats = new HBox();
        VBox opponentStats = new VBox();
        VBox fullBox = new VBox();

        opponentStats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
        opponentStats.setSpacing(5);
        opponentStats.setAlignment(Pos.CENTER);
        cardsAndStats.getChildren().addAll(leftCardImage, rightCardImage, opponentStats);
        cardsAndStats.setSpacing(10);
        cardsAndStats.setAlignment(Pos.CENTER);
        fullBox.getChildren().addAll(cardsAndStats, lastMoveLabel);
        fullBox.setAlignment(Pos.CENTER);

        return fullBox;
    }

    public void setLastMoveLabel(String s){
        lastMoveLabel.setText(s);
    }

    public void setStackSizeLabel(String s){
        stackSizeLabel.setText(s);
    }

    public void setPositionLabel(String s){
        positionLabel.setText(s);
    }

    public void setCardImage(Image leftCard,Image rightCard) {
        leftCardImage.setImage(leftCard);
        rightCardImage.setImage(rightCard);

        leftCardImage.setEffect(dropShadow);
        rightCardImage.setEffect(dropShadow);
        leftCardImage.setVisible(true);
        rightCardImage.setVisible(true);
    }

    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }
}
