package gui.layouts;

import gui.GUIMain;
import gui.ImageViewer;
import gui.ObjectStandards;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by ady on 04/04/16.
 */
public class OpponentLayout extends HBox{

    private DropShadow dropShadow = new DropShadow();
    private DropShadow glow = new DropShadow();

    private Label nameLabel, stackSizeLabel, positionLabel, lastMoveLabel;
    private ImageView leftCardImage, rightCardImage, chipImage, dealerButtonImage;
    private int position;
    private boolean isBust = false;
    private boolean folded = false;

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
        chipImage.setImage(ImageViewer.getChipAndButtonImage(null));
        chipImage.setPreserveRatio(true);
        chipImage.setFitWidth(35);

        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);
        chipImage.setVisible(true);

        glow.setColor(Color.WHITE);
        glow.setHeight(50);
        glow.setWidth(50);

        nameLabel = ObjectStandards.makeStandardLabelWhite("", name);
        nameLabel.setFont(Font.font("Areal", FontWeight.BOLD, 15));
        stackSizeLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSize + "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("Position: ","");
        lastMoveLabel = ObjectStandards.makeLobbyLabelWhite("", "");

        HBox cards = new HBox();
        VBox opponentStats = new VBox();
        VBox lastMoveButtonChipBox = new VBox();
        VBox chipBox = new VBox();
        VBox lastMoveBox = new VBox();
        lastMoveBox.getChildren().addAll(lastMoveLabel);

        dealerButtonImage = new ImageView();
        dealerButtonImage.setImage(ImageViewer.getChipAndButtonImage(null));
        dealerButtonImage.setPreserveRatio(true);
        dealerButtonImage.setFitWidth(32);
        HBox dealerButtonBox = new HBox();
        dealerButtonBox.getChildren().addAll(dealerButtonImage);
        dealerButtonBox.setMinSize(32,35);

        if(position == 1 || position == 2){
            dealerButtonBox.setAlignment(Pos.TOP_LEFT);
            chipBox.setAlignment(Pos.BASELINE_LEFT);
            lastMoveButtonChipBox.getChildren().addAll(dealerButtonBox,lastMoveBox,chipBox);
            lastMoveButtonChipBox.setMinSize(150,110);

            chipBox.getChildren().addAll(chipImage);
            chipBox.setPadding(new Insets(0,6,0,6));

            VBox stats = new VBox();
            stats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            stats.setAlignment(Pos.TOP_LEFT);
            opponentStats.getChildren().addAll(cards, stats);
            cards.getChildren().addAll(leftCardImage, rightCardImage, lastMoveButtonChipBox);

            this.getChildren().addAll(opponentStats);

            cards.setAlignment(Pos.CENTER_LEFT);
            this.setAlignment(Pos.CENTER_LEFT);
            this.setMinWidth(300);

        }
        else if (position == 3){
            dealerButtonBox.setAlignment(Pos.TOP_RIGHT);
            chipBox.setAlignment(Pos.BASELINE_RIGHT);
            lastMoveBox.setAlignment(Pos.CENTER_RIGHT);
            lastMoveButtonChipBox.getChildren().addAll(dealerButtonBox, lastMoveBox,chipBox);
            lastMoveButtonChipBox.setMinSize(150,110);

            chipBox.getChildren().addAll(chipImage);
            chipBox.setPadding(new Insets(3, 3, 3, 3));

            nameLabel.setMinWidth(150);
            stackSizeLabel.setMinWidth(150);
            positionLabel.setMinWidth(150);
            opponentStats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            cards.getChildren().addAll(lastMoveButtonChipBox,leftCardImage, rightCardImage, opponentStats);

            this.getChildren().add(cards);
            this.setMinWidth(300);

        }
        else if(position == 4 || position == 5){
            dealerButtonBox.setAlignment(Pos.TOP_RIGHT);
            chipBox.setAlignment(Pos.BASELINE_RIGHT);
            lastMoveBox.setAlignment(Pos.CENTER_RIGHT);
            lastMoveButtonChipBox.getChildren().addAll(dealerButtonBox, lastMoveBox, chipBox);
            lastMoveButtonChipBox.setMinSize(150, 110);

            chipBox.getChildren().addAll(chipImage);
            chipBox.setPadding(new Insets(0, 6, 0, 6));

            VBox stats = new VBox();
            nameLabel.setMinWidth(150);
            stackSizeLabel.setMinWidth(150);
            positionLabel.setMinWidth(150);
            stats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            stats.setAlignment(Pos.TOP_RIGHT);
            opponentStats.getChildren().addAll(cards, stats);
            cards.getChildren().addAll(lastMoveButtonChipBox,leftCardImage, rightCardImage);

            this.getChildren().addAll(opponentStats);

            cards.setAlignment(Pos.CENTER_RIGHT);
            this.setAlignment(Pos.CENTER_RIGHT);
            this.setMinWidth(300);
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
     * @param lastMove
     */
    public void setLastMove(String lastMove, Image chipImage){
        lastMoveLabel.setText(lastMove);
        this.chipImage.setImage(chipImage);
        if(!folded) {
            leftCardImage.setEffect(dropShadow);
            rightCardImage.setEffect(dropShadow);
        }
    }

    /**
     * Set stack size for opponent
     *
     * @param s
     */

    public void setStackSizeLabel(String s){
        stackSizeLabel.setText(s);
    }

    /**
     *
     * Set position for opponent
     *
     * @param s
     */

    public void setPositionLabel(String s, Image buttonImage){
        positionLabel.setText(s);
        this.dealerButtonImage.setImage(buttonImage);
    }

    /**
     *
     * Show cards
     *
     * @param leftCard
     * @param rightCard
     */
    public void setCardImage(Image leftCard, Image rightCard) {
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
        folded = true;
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }

    /**
     * Get opponent position
     *
     * @return position
     */

    public int getPosition() {
        return position;
    }

    /**
     * Set opponent position
     *
     * @param position
     */

    public void setPosition(int position){
        this.position = position;
        folded = false;
    }

    /**
     * What happens when a player is bust
     *
     * @param bustedText
     */
    public void bustPlayer(String bustedText) {
        isBust = true;
        setLastMove("", null);
        setStackSizeLabel(bustedText);
        setPositionLabel("", null);
        setCardImage(null, null);
    }

    public boolean isBust() {
        return isBust;
    }

    public void highlightTurn() {
        if (leftCardImage != null && rightCardImage != null) {
            leftCardImage.setEffect(glow);
            rightCardImage.setEffect(glow);
        }
    }
}
