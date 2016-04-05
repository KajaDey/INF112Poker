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
public class BoardLayout {

    private Label currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel, winnerLabel;
    private long currentSmallBlind, currentBigBlind;
    private ImageView[] communityCards = new ImageView[5];

    public BoardLayout(){
    }

    /**
     * Generates a boardLayout
     *
     * @return a boardLayout
     */
    public VBox updateLayout(long smallBlind, long bigBlind) {
        DropShadow dropShadow = new DropShadow();
        this.currentSmallBlind = smallBlind;
        this.currentBigBlind = bigBlind;

        for (int i = 0; i < communityCards.length; i++) {
            communityCards[i] = ImageViewer.getEmptyImageView("player");
            communityCards[i].setEffect(dropShadow);
        }

        HBox cardLayout = new HBox();
        VBox statsLayout = new VBox();
        VBox fullLayout = new VBox();

        currentBBLabel = ObjectStandards.makeStandardLabelWhite("Current BB:", bigBlind + "$");
        currentSBLabel = ObjectStandards.makeStandardLabelWhite("Current SM:", smallBlind + "$");
        nextBBLabel = ObjectStandards.makeStandardLabelWhite("Next BB: ", bigBlind * 2 + "$");
        nextSBLabel = ObjectStandards.makeStandardLabelWhite("Next SB: ", smallBlind * 2 + "$");
        potLabel = ObjectStandards.makeStandardLabelWhite("", "");
        winnerLabel = ObjectStandards.makeStandardLabelWhite("", "");

        statsLayout.getChildren().addAll(currentBBLabel, currentSBLabel, nextBBLabel, nextSBLabel, potLabel);
        statsLayout.setSpacing(10);
        statsLayout.setAlignment(Pos.CENTER);

        cardLayout.getChildren().add(statsLayout);
        cardLayout.getChildren().addAll(communityCards);

        cardLayout.setSpacing(10);
        cardLayout.setAlignment(Pos.CENTER);

        fullLayout.getChildren().setAll(cardLayout, winnerLabel);
        fullLayout.setAlignment(Pos.CENTER);


        return fullLayout;
    }

    public ImageView[] getCommunityCards() {
        return communityCards;
    }

    public void setWinnerLabel(String winnerLabel) {
        this.winnerLabel.setText(winnerLabel);
    }

    public void setPotLabel(String pot) {
        potLabel.setText(pot);
    }

    public void setFlop(Image card1Image, Image card2Image, Image card3Image) {
        communityCards[0].setImage(card1Image);
        communityCards[0].setVisible(true);
        communityCards[1].setImage(card2Image);
        communityCards[1].setVisible(true);
        communityCards[2].setImage(card3Image);
        communityCards[2].setVisible(true);
    }

    public void setTurn(Image turn) {
        communityCards[3].setImage(turn);
        communityCards[3].setVisible(true);
    }

    public void setRiver(Image river) {
        communityCards[4].setImage(river);
        communityCards[4].setVisible(true);
    }
}