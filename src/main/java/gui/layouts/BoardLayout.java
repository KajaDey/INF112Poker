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
public class BoardLayout extends VBox {

    private Label currentBBLabel, currentSBLabel,  potLabel, winnerLabel;
    private ImageView[] communityCards = new ImageView[5];
    private long currentBigBlind;

    public BoardLayout(long smallBlind, long bigBlind){
        DropShadow dropShadow = new DropShadow();

        for (int i = 0; i < communityCards.length; i++) {
            communityCards[i] = ImageViewer.getEmptyImageView(ImageViewer.Image_type.PLAYER);
            communityCards[i].setEffect(dropShadow);
        }

        HBox cardLayout = new HBox();
        VBox statsLayout = new VBox();

        this.currentBigBlind = bigBlind;
        currentBBLabel = ObjectStandards.makeStandardLabelWhite("Current BB: ", bigBlind + "$");
        currentSBLabel = ObjectStandards.makeStandardLabelWhite("Current SB: ", smallBlind + "$");
        potLabel = ObjectStandards.makeStandardLabelWhite("", "");
        currentBBLabel.setMinWidth(115);
        currentSBLabel.setMinWidth(115);
        potLabel.setMinWidth(115);
        winnerLabel = ObjectStandards.makeStandardLabelWhite("", "");

        statsLayout.getChildren().addAll(currentBBLabel, currentSBLabel, potLabel);
        statsLayout.setSpacing(10);
        statsLayout.setAlignment(Pos.CENTER);

        cardLayout.getChildren().add(statsLayout);
        cardLayout.getChildren().addAll(communityCards);

        cardLayout.setSpacing(10);
        cardLayout.setAlignment(Pos.CENTER);

        this.getChildren().setAll(cardLayout, winnerLabel);
        this.setAlignment(Pos.CENTER);
    }

    /**
     *
     * @return list of community cards
     */
    public ImageView[] getCommunityCards() {
        return communityCards;
    }

    public void setWinnerLabel(String winnerLabel) {
        this.winnerLabel.setText(winnerLabel);
    }


    /**
     * Set the pot
     */
    public void setPotLabel(String pot) {
        potLabel.setText(pot);
    }

    /**
     * Sets the flop and make it visible
     */
    public void showFlop(Image card1Image, Image card2Image, Image card3Image) {
        communityCards[0].setImage(card1Image);
        communityCards[0].setVisible(true);
        communityCards[1].setImage(card2Image);
        communityCards[1].setVisible(true);
        communityCards[2].setImage(card3Image);
        communityCards[2].setVisible(true);
    }

    /**
     * Set and display turn
     */
    public void showTurn(Image turn) {
        communityCards[3].setImage(turn);
        communityCards[3].setVisible(true);
    }

    /**
     * Set and display river
     */
    public void showRiver(Image river) {
        communityCards[4].setImage(river);
        communityCards[4].setVisible(true);
    }

    public void newHand() {
        Image backImage = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));
        for (ImageView imageview : communityCards) {
            imageview.setImage(backImage);
            imageview.setVisible(false);
        }

        setWinnerLabel("");
    }

    public void setBigBlindLabel(long bigBlind) {
        this.currentBigBlind = bigBlind;
        this.currentBBLabel.setText("Current BB: " + bigBlind + "$");
    }

    public void setSmallBlindLabel(long smallBlind) {
        this.currentSBLabel.setText("Current SB: " + smallBlind + " $");
    }

    public long getBB() { return currentBigBlind; }

}
