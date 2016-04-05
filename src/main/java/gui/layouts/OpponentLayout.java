package gui.layouts;

import gui.GUIMain;
import gui.ImageViewer;
import gui.ObjectStandards;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by ady on 04/04/16.
 */
public class OpponentLayout extends HBox{

    DropShadow dropShadow = new DropShadow();

    private Label nameLabel, stackSizeLabel, positionLabel, lastMoveLabel;
    private ImageView leftCardImage, rightCardImage;

    public OpponentLayout(){
        super();
    }

    /**
     * Makes the layout for the opponentScreen
     *
     * @param userID
     * @param name
     * @param stackSize
     * @return a layout
     */
    public void updateLayout(int userID, String name, long stackSize, int position) {
        leftCardImage = ImageViewer.getEmptyImageView("opponent");
        rightCardImage = ImageViewer.getEmptyImageView("opponent");

        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);

        nameLabel = ObjectStandards.makeStandardLabelWhite("", name);
        stackSizeLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("Position: ","");
        lastMoveLabel = ObjectStandards.makeLobbyLabelWhite("", "");

        HBox cards = new HBox();
        VBox opponentStats = new VBox();

        if(position == 1 || position == 2){
            cards.getChildren().addAll(leftCardImage, rightCardImage);
            opponentStats.getChildren().addAll(cards,nameLabel, stackSizeLabel, positionLabel);
            this.getChildren().addAll(opponentStats, lastMoveLabel);
        }
        else if (position == 3){

            opponentStats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            cards.getChildren().addAll(leftCardImage, rightCardImage, opponentStats);
            VBox vBox = new VBox();
            vBox.getChildren().addAll(cards, lastMoveLabel);
            vBox.setAlignment(Pos.CENTER);
            this.getChildren().add(vBox);

        }
        else if(position == 4 || position == 5){

            cards.getChildren().addAll(leftCardImage, rightCardImage);
            opponentStats.getChildren().addAll(cards,nameLabel, stackSizeLabel, positionLabel);
            this.getChildren().addAll(lastMoveLabel, opponentStats);
        }
        else
            GUIMain.debugPrint("Invalid position from OpponentLayout");

        opponentStats.setSpacing(5);
        opponentStats.setAlignment(Pos.CENTER);
        cards.setSpacing(10);
        cards.setAlignment(Pos.CENTER);

        this.setAlignment(Pos.CENTER);
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

    public void removeHolecards() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.5);
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }
}
