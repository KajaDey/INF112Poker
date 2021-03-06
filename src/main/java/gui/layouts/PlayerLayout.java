package gui.layouts;

import gamelogic.Decision;
import gui.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * This class contains all the objects to make a player layout
 *
 * @author André Dyrstad
 */

public class PlayerLayout extends VBox implements IPlayerLayout {
    private Label stackLabel, positionLabel, lastMoveLabel, nameLabel, bestHand, percentLabel;
    private ImageView leftCardImage, rightCardImage, chipImage, dealerButtonImage;
    private Slider slider = new Slider(0,0,0);
    private TextField amountTextField;
    private RemainingTimeBar progressBar;

    private Button betRaiseButton, checkCallButton, foldButton;
    private boolean isBust = false, isActing = false;
    private long stackSize;
    public boolean showCards = false;


    public PlayerLayout(String name){
        //Make ALL the boxes
        HBox fullBox = new HBox();
        VBox fullBoxWithLastMove = new VBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        twoButtonsUnderInput.setMaxWidth(50);
        VBox twoButtonsRight = new VBox();
        VBox sliderAndBestHandBox = new VBox();
        HBox lastMoveAndChips = new HBox();

        //////Make all the elements I want to add to the playerLayout//////////
        stackLabel = ObjectStandards.makeStandardLabelWhite("", "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("", "");
        lastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");
        nameLabel = ObjectStandards.makeStandardLabelWhite("", name);
        nameLabel.setFont(Font.font("Areal", FontWeight.BOLD, 15));
        percentLabel = ObjectStandards.makeStandardLabelWhite("","");
        bestHand = ObjectStandards.makeStandardLabelWhite("","");
        bestHand.setFont(Font.font("Areal", FontWeight.BOLD, 15));

        Image backOfCards = ImageViewer.getImage(ImageViewer.Image_type.CARD_BACK);
        leftCardImage = ImageViewer.getEmptyImageView(ImageViewer.Image_type.PLAYER);
        rightCardImage = ImageViewer.getEmptyImageView(ImageViewer.Image_type.PLAYER);
        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);

        chipImage = new ImageView();
        chipImage.setImage(ImageViewer.getChipImage(null));
        chipImage.setPreserveRatio(true);
        chipImage.setFitWidth(35);

        amountTextField = ObjectStandards.makeTextFieldForGameScreen("Amount");

        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setBlockIncrement(0.1f);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(false);

        sliderAndBestHandBox.setMinWidth(200);
        progressBar = new RemainingTimeBar(name);
        progressBar.prefWidthProperty().bind(sliderAndBestHandBox.widthProperty().subtract(50));

        //Buttons in the VBox
        checkCallButton = ObjectStandards.makeStandardButton("Check");
        foldButton = ObjectStandards.makeStandardButton("Fold");
        betRaiseButton = ObjectStandards.makeStandardButton("Bet");
        betRaiseButton.setMinHeight(66);

        //Prevent objects from being focus traversable, meaning you can't tab them into focus
        betRaiseButton.setFocusTraversable(false);
        checkCallButton.setFocusTraversable(false);
        foldButton.setFocusTraversable(false);
        amountTextField.setFocusTraversable(false);
        slider.setFocusTraversable(false);

        //Actions
        betRaiseButton.setOnAction(e -> {
            ButtonListeners.betButtonListener(amountTextField, betRaiseButton.getText());
            updateSliderValues(stackSize);
        });

        amountTextField.setOnAction(e -> {
            ButtonListeners.betButtonListener(amountTextField, betRaiseButton.getText());
            updateSliderValues(stackSize);
        });

        checkCallButton.setOnAction(e -> {
            ButtonListeners.checkButtonListener(checkCallButton.getText());
            updateSliderValues(stackSize);
        });

        foldButton.setOnAction(e -> ButtonListeners.foldButtonListener());

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int sliderNumber = (int) slider.getValue();
            if (sliderNumber >= slider.getMax())
                amountTextField.setText("All in");
            else
                amountTextField.setText(String.valueOf(sliderNumber));
        });

        dealerButtonImage = new ImageView();
        dealerButtonImage.setImage(ImageViewer.getChipImage(null));
        dealerButtonImage.setPreserveRatio(true);
        dealerButtonImage.setFitWidth(38);
        HBox dealerButtonBox = new HBox();
        dealerButtonBox.getChildren().addAll(dealerButtonImage);
        dealerButtonBox.setAlignment(Pos.CENTER);
        dealerButtonBox.setMinSize(32,32);
        dealerButtonBox.setPadding(new Insets(0,0,5,0));


        //Add objects to the boxes
        stats.getChildren().addAll(dealerButtonBox, nameLabel, stackLabel, positionLabel, percentLabel);
        stats.setAlignment(Pos.CENTER);
        stats.setMinWidth(175);

        twoButtonsUnderInput.getChildren().addAll(checkCallButton, foldButton);

        inputAndButtons.getChildren().addAll(amountTextField, twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);

        twoButtonsRight.getChildren().addAll(betRaiseButton);
        twoButtonsRight.setAlignment(Pos.CENTER);

        slider.setMaxWidth(150);

        sliderAndBestHandBox.getChildren().addAll(bestHand,slider, progressBar);
        sliderAndBestHandBox.setAlignment(Pos.CENTER);
        sliderAndBestHandBox.setPadding(new Insets(0,0,0,8));

        fullBox.getChildren().addAll(stats, leftCardImage, rightCardImage, inputAndButtons, twoButtonsRight, sliderAndBestHandBox);
        fullBox.setAlignment(Pos.CENTER);

        VBox chipBox = new VBox();
        chipBox.getChildren().addAll(chipImage);
        chipBox.setPadding(new Insets(0,0,0,10));

        VBox lastMoveBox = new VBox();
        lastMoveBox.getChildren().addAll(lastMoveLabel);
        lastMoveBox.setMinHeight(50);

        lastMoveAndChips.getChildren().addAll(lastMoveBox,chipBox);
        lastMoveAndChips.setMinWidth(150);
        lastMoveAndChips.setAlignment(Pos.CENTER);
        lastMoveAndChips.setMinHeight(37);

        fullBoxWithLastMove.getChildren().addAll(lastMoveAndChips, fullBox);
        fullBoxWithLastMove.setAlignment(Pos.CENTER_LEFT);
        fullBoxWithLastMove.setMinWidth(700);
        fullBoxWithLastMove.setMinHeight(50);

        this.setActionsVisible(false);
        this.getChildren().add(fullBoxWithLastMove);
    }

    /**
     * Updates the values of the slider
     */
    public void updateSliderValues(long stackSize){
        slider.setValue(0);
        slider.setMin(0);
        slider.setMax(stackSize);
        slider.setBlockIncrement(0.1f);
        slider.setMinorTickCount(0);

        if (slider.getMax() > 2 * slider.getMin() && slider.getMax() / 2 > 0)
            slider.setMajorTickUnit(slider.getMax() / 2);
        else {
            slider.setMajorTickUnit(1);
            slider.setVisible(false);
        }
    }

    /**
     * Make the action buttons visible and update isActing (a boolean that indicates if this player is acting)
     * @param visible True if buttons should be set to visible
     */
    public void setActionsVisible(boolean visible) {
        isActing = visible;
        betRaiseButton.setVisible(visible);
        checkCallButton.setVisible(visible);
        foldButton.setVisible(visible);
        amountTextField.setVisible(visible);
        slider.setVisible(visible);
        progressBar.setVisible(visible);
    }

    public void setPositionLabel(String pos, Image buttonImage){
        positionLabel.setText(pos);
        this.dealerButtonImage.setImage(buttonImage);
    }

    public void setStackLabel(String stack) {
        //If stack is a number, set stackSize to this number
        try { stackSize = Long.parseLong(stack); } catch (NumberFormatException e) {
            System.out.println("SetStackLabel" + e);
        }
        if (stack.equals("0"))
            stackLabel.setText("All in");
        else
            stackLabel.setText("$" + stack);
    }

    private void setBustLabel(String bustText) {
        stackLabel.setText(bustText);
    }

    public void setLastMove(String lastMove, Image chipImage) {
        lastMoveLabel.setText(lastMove);
        this.chipImage.setImage(chipImage);
    }

    public void setCheckCallButton(String checkCall) {
        checkCallButton.setText(checkCall);
    }

    public void setBetRaiseButton(String betRaise) {
        betRaiseButton.setText(betRaise);
    }

    public void setAmountTextField(String amount) {
        amountTextField.setText(amount);
    }

    public void setTextfieldStyle(String textfieldStyle) {
        amountTextField.setStyle(textfieldStyle);
    }

    public void setBestHand(String bestHand){
        this.bestHand.setText(bestHand);
    }

    /**
     * Set card image and make them visible
     */
    public synchronized void setCardImage(Image leftImage, Image rightImage) {
        DropShadow dropShadow = new DropShadow();
        leftCardImage.setImage(leftImage);
        rightCardImage.setImage(rightImage);
        leftCardImage.setEffect(dropShadow);
        rightCardImage.setEffect(dropShadow);
        leftCardImage.setVisible(true);
        rightCardImage.setVisible(true);
    }

    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }

    public void bustPlayer(String bustedText) {
        setLastMove("", null);
        setPercentLabel("");
        setBustLabel(bustedText);
        setPositionLabel("", null);
        setCardImage(null, null);
        slider.setVisible(false);
        bestHand.setVisible(false);
        isBust = true;
    }

    public void foldPlayer() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.5);
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }

    @Override
    public void highlightTurn(boolean highlight) {
        //Nothing for now
    }

    @Override
    public void setPercentLabel(String s) {
        this.percentLabel.setText(s);
    }

    @Override
    public boolean isBust() {
        return isBust;
    }

    /**
     *  Set the focus of this layout (remove focus amount text field)
     */
    public void setFocus() {
        this.requestFocus();
    }
    public String getCheckCallButtonText() { return checkCallButton.getText(); }
    public String getBetRaiseButtonText() { return betRaiseButton.getText(); }
    public TextField getAmountTextField() { return amountTextField; }
    public long getStackSize() {
        return this.stackSize;
    }

    /**
     * Reset the time to think progress bar
     */
    public void startTimer(GameScreen gameScreen, long timeToThink, Decision.Move moveToExecute) {
        progressBar.setTimer(gameScreen, timeToThink, moveToExecute);
    }

    public void stopTimer() {
        progressBar.stopTimer();
    }

    public boolean isActing() {
        return isActing;
    }

    public void setCallBacks(Consumer<Boolean> callback) {
        Runnable task = (() -> {
            callback.accept(showCards = !showCards);
            leftCardImage.setEffect(showCards ? new SepiaTone() : new DropShadow());
            rightCardImage.setEffect(showCards ? new SepiaTone() : new DropShadow());
        });
        leftCardImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> task.run());
        rightCardImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> task.run());
    }
}
