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
public class OpponentLayout extends HBox implements IPlayerLayout{

    private DropShadow dropShadow = new DropShadow();
    private DropShadow glow = new DropShadow();

    private Label nameLabel, stackSizeLabel, positionLabel, lastMoveLabel;
    private ImageView leftCardImage, rightCardImage, chipImage, dealerButtonImage;
    private boolean isBust = false;
    private boolean folded = false;

    public OpponentLayout(String name, long stackSize, int position) {
        leftCardImage = ImageViewer.getEmptyImageView(ImageViewer.Image_type.OPPONENT);
        rightCardImage = ImageViewer.getEmptyImageView(ImageViewer.Image_type.OPPONENT);

        Image backOfCards = ImageViewer.getImage(ImageViewer.Image_type.CARD_BACK);

        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        chipImage = new ImageView();
        chipImage.setImage(ImageViewer.getChipImage(null));
        chipImage.setPreserveRatio(true);
        chipImage.setFitWidth(35);

        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);
        chipImage.setVisible(true);

        glow.setColor(Color.web("cyan", 0.5));
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
        dealerButtonImage.setImage(ImageViewer.getChipImage(null));
        dealerButtonImage.setPreserveRatio(true);
        dealerButtonImage.setFitWidth(38);
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
        }
        else if (position == 3){
            dealerButtonBox.setAlignment(Pos.TOP_RIGHT);
            chipBox.setAlignment(Pos.BASELINE_RIGHT);
            lastMoveBox.setAlignment(Pos.CENTER_RIGHT);
            lastMoveButtonChipBox.getChildren().addAll(dealerButtonBox, lastMoveBox,chipBox);
            lastMoveButtonChipBox.setMinSize(150,110);

            chipBox.getChildren().addAll(chipImage);
            chipBox.setPadding(new Insets(3, 3, 3, 3));

            opponentStats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            cards.getChildren().addAll(lastMoveButtonChipBox,leftCardImage, rightCardImage, opponentStats);

            this.getChildren().add(cards);
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
            stats.getChildren().addAll(nameLabel, stackSizeLabel, positionLabel);
            stats.setAlignment(Pos.TOP_RIGHT);
            opponentStats.getChildren().addAll(cards, stats);
            cards.getChildren().addAll(lastMoveButtonChipBox,leftCardImage, rightCardImage);

            this.getChildren().addAll(opponentStats);

            cards.setAlignment(Pos.CENTER_RIGHT);
            this.setAlignment(Pos.CENTER_RIGHT);
        }
        else
            GUIMain.debugPrint("Invalid position from OpponentLayout");

        nameLabel.setMinWidth(140);
        stackSizeLabel.setMinWidth(140);
        positionLabel.setMinWidth(140);

        this.setMinWidth(300);
        this.setMaxWidth(300);
        opponentStats.setSpacing(5);
        opponentStats.setAlignment(Pos.CENTER);
        cards.setSpacing(1);

    }

    /**
     * Set last move label for opponent (e.g.: "CALL 50" or "ALL IN")
     * Set the chip image (or null) for this move
     *
     * @param lastMove The complete string the last move label should be set to
     */
    @Override
    public void setLastMove(String lastMove, Image chipImage){
        lastMoveLabel.setText(lastMove);
        this.chipImage.setImage(chipImage);
    }

    /**
     * Sets stack size label for this opponent
     * @param s The complete string the label will be set to
     */
    @Override
    public void setStackLabel(String s){
        stackSizeLabel.setText(s);
    }

    /**
     * Sets the position label for this opponent to the given string.
     * @param s
     */
    @Override
    public void setPositionLabel(String s, Image buttonImage){
        positionLabel.setText(s);
        this.dealerButtonImage.setImage(buttonImage);
    }


    /**
     * Sets this opponents hole cards to the images provided
     * Used at showdown and when hands are being dealt
     * @param leftCard
     * @param rightCard
     */
    @Override
    public synchronized void setCardImage(Image leftCard,Image rightCard) {
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
    @Override
    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }


    /**
     * Gray out cards
     */
    @Override
    public synchronized void foldPlayer() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.5);
        folded = true;
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }

    /**
     * What happens when a player is bust
     *
     * @param bustedText
     */
    @Override
    public synchronized void bustPlayer(String bustedText) {
        isBust = true;
        setLastMove("", null);
        setStackLabel(bustedText);
        setPositionLabel("", null);
        setCardImage(null, null);
    }

    @Override
    public boolean isBust() {
        return isBust;
    }

    public synchronized void highlightTurn(boolean highlight) {
        if (!(leftCardImage != null && rightCardImage != null))
            return;

        if (highlight) {
            leftCardImage.setEffect(glow);
            rightCardImage.setEffect(glow);
        } else {
            leftCardImage.setEffect(dropShadow);
            rightCardImage.setEffect(dropShadow);
        }
    }

    /**
     *  Return the X-layout of an opponent layout based on his position on the table
     * @param pos    The players position
     * @param width  Screen width
     * @return The xLayout of this opponentLayout
     */
    public static double getLayoutX(int pos, double width) {
        switch (pos){
            case 1:case 2:
                return 20.0;
            case 3:
                return width / 3;
            case 4:case 5:
                return width - 320;
            default:
                GUIMain.debugPrintln("** Cannot place opponent in pos " + pos + " **\n");
                return 0.0;
        }
    }

    /**
     *  Return the Y-layout of an opponent layout based on his position on the table
     * @param pos       The opponents position
     * @param height    The height of the screen
     * @return  The y-layout of this opponentLayout
     */
    public static double getLayoutY(int pos, double height) {
        final String os = System.getProperty("os.version");

        switch (pos) {
            case 1: case 5:
                return height / 2;
            case 2: case 4:
                return height / 6;
            case 3:
                return !os.isEmpty() ? 20 : !os.startsWith("Mac") ? 30 : 20;
            default:
                GUIMain.debugPrintln("** Cannot place opponent in pos " + pos + " **\n");
                return 0.0;
        }
    }
}
