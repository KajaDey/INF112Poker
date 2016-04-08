package gui.layouts;

import gui.GUIMain;
import gui.ImageViewer;
import gui.ObjectStandards;
import javafx.application.Platform;
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
    private ImageView leftCardImage, rightCardImage, chipImage, dealerButtonImage;
    private int position;
    private boolean isBust = false;

    public OpponentLayout(){
        super();
    }

    /**
     * Makes the layout for the opponentScreen
     *
     * @param name
     * @param stackSize
     * @return a layout
     */
    public void updateLayout(String name, long stackSize) {
        leftCardImage = ImageViewer.getEmptyImageView("opponent");
        rightCardImage = ImageViewer.getEmptyImageView("opponent");

        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        chipImage = new ImageView();
        chipImage.setImage(ImageViewer.getChipImage("poker1"));
        chipImage.setFitHeight(40);
        chipImage.setFitWidth(40);

        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);
        chipImage.setVisible(false);

        nameLabel = ObjectStandards.makeStandardLabelWhite("", name);
        stackSizeLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("Position: ","");
        lastMoveLabel = ObjectStandards.makeLobbyLabelWhite("", "");

        HBox cards = new HBox();
        VBox opponentStats = new VBox();
        VBox moveInfo = new VBox();

        if(position == 1 || position == 2){
            cards.getChildren().addAll(leftCardImage, rightCardImage);
            opponentStats.getChildren().addAll(cards, nameLabel, stackSizeLabel, positionLabel);
            Runnable task = () -> moveInfo.getChildren().addAll(lastMoveLabel,chipImage);
            Platform.runLater(task);
            this.getChildren().addAll(opponentStats, moveInfo);

            moveInfo.setAlignment(Pos.CENTER);
            cards.setAlignment(Pos.CENTER_LEFT);
            this.setAlignment(Pos.CENTER_LEFT);

        }
        else if (position == 3){

            HBox moveInfoH = new HBox();

            opponentStats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            cards.getChildren().addAll(leftCardImage, rightCardImage, opponentStats);
            VBox vBox = new VBox();
            moveInfoH.getChildren().addAll(lastMoveLabel,chipImage);
            vBox.getChildren().addAll(cards,moveInfoH);

            vBox.setAlignment(Pos.CENTER);
            this.getChildren().add(vBox);

        }
        else if(position == 4 || position == 5){

            cards.getChildren().addAll(leftCardImage, rightCardImage);
            opponentStats.getChildren().addAll(cards,nameLabel, stackSizeLabel, positionLabel);
            Runnable task = () -> moveInfo.getChildren().addAll(lastMoveLabel,chipImage);
            Platform.runLater(task);
            this.getChildren().addAll(moveInfo, opponentStats);
            this.setMinWidth(250);


            cards.setAlignment(Pos.CENTER_RIGHT);
            this.setAlignment(Pos.CENTER_RIGHT);
        }
        else
            GUIMain.debugPrint("Invalid position from OpponentLayout");

        opponentStats.setSpacing(5);
        opponentStats.setAlignment(Pos.CENTER);
        cards.setSpacing(10);

    }

    /**
     * Set last move for opponent
     *
     * @param s
     */
    public void setLastMoveLabel(String s){
        Runnable task = () -> {
            lastMoveLabel.setText(s);
            if (s.equals(""))
                chipImage.setVisible(false);
            else
                chipImage.setVisible(true);
        };
        Platform.runLater(task);
    }

    /**
     * Set stack size for opponent
     *
     * @param s
     */

    public void setStackSizeLabel(String s){
        Platform.runLater(() -> stackSizeLabel.setText(s));
    }

    /**
     *
     * Set position for opponent
     *
     * @param s
     */

    public void setPositionLabel(String s){
        Platform.runLater(() -> positionLabel.setText(s));
    }

    /**
     *
     * Show cards
     *
     * @param leftCard
     * @param rightCard
     */
    public void setCardImage(Image leftCard,Image rightCard) {
        leftCardImage.setImage(leftCard);
        rightCardImage.setImage(rightCard);

        leftCardImage.setEffect(dropShadow);
        rightCardImage.setEffect(dropShadow);
        leftCardImage.setVisible(true);
        rightCardImage.setVisible(true);
    }

    /**
     * Sets name for opponent
     *
     * @param name
     */

    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }


    /**
     * Gray out cards
     */

    public void removeHolecards() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.5);
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }

    /**
     *
     * Get opponent position
     *
     * @return position
     */

    public int getPosition() {
        return position;
    }

    /**
     *
     * Set opponent position
     *
     * @param position
     */

    public void setPosition(int position){
        this.position = position;
    }

    public void bustPlayer(String bustedText) {
        isBust = true;
        setLastMoveLabel("");
        setStackSizeLabel(bustedText);
        setPositionLabel("");

        setCardImage(null, null);
    }

    public boolean isBust() {
        return isBust;
    }
}
