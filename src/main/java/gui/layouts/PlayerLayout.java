package gui.layouts;

import gui.ButtonListeners;
import gui.ImageViewer;
import gui.ObjectStandards;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by ady on 04/04/16.
 */

public class PlayerLayout {

    private Label stackLabel, positionLabel, lastMoveLabel, nameLabel, bestHand;
    private ImageView leftCardImage, rightCardImage;
    private Slider slider = new Slider(0,0,0);
    private TextField amountTextField;

    private long currentSmallBlind, currentBigBlind;

    private Button betRaiseButton, checkCallButton, foldButton;

    public PlayerLayout(){

    }

    /**
     * A method for making a playerLayout
     *
     * @return A VBox with the player layout
     */
    public VBox updateLayout(int playerID, String name, long stackSizeIn) {
        //Make ALL the boxes
        HBox fullBox = new HBox();
        VBox fullBoxWithLastMove = new VBox();
        VBox stats = new VBox();
        VBox inputAndButtons = new VBox();
        HBox twoButtonsUnderInput = new HBox();
        twoButtonsUnderInput.setMaxWidth(50);
        VBox twoButtonsRight = new VBox();
        VBox sliderBox = new VBox();

        //////Make all the elements i want to add to the playerLayout//////////
        stackLabel = ObjectStandards.makeStandardLabelWhite("Stack size:", stackSizeIn + "");
        positionLabel = ObjectStandards.makeStandardLabelWhite("Position: ", "");
        lastMoveLabel = ObjectStandards.makeStandardLabelWhite("", "");
        nameLabel = ObjectStandards.makeStandardLabelWhite("Name: ", name);
        bestHand = ObjectStandards.makeStandardLabelWhite("Your hand:","");


        Image backOfCards = new Image(ImageViewer.returnURLPathForCardSprites("_Back"));

        leftCardImage = ImageViewer.getEmptyImageView("player");
        rightCardImage = ImageViewer.getEmptyImageView("player");
        leftCardImage.setImage(backOfCards);
        rightCardImage.setImage(backOfCards);
        leftCardImage.setVisible(false);
        rightCardImage.setVisible(false);

        amountTextField = ObjectStandards.makeTextFieldForGameScreen("Amount");

        slider.setMin(currentBigBlind);
        slider.setMax(stackSizeIn);
        slider.setValue(currentBigBlind);

        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(slider.getMax()/2);
        slider.setBlockIncrement(0.1f);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(false);


        //Buttons in the VBox
        checkCallButton = ObjectStandards.makeStandardButton("Check");
        foldButton = ObjectStandards.makeStandardButton("Fold");
        betRaiseButton = ObjectStandards.makeStandardButton("Bet");
        betRaiseButton.setMinHeight(66);


        //Actions
        betRaiseButton.setOnAction(e -> {
            if(!amountTextField.getText().equals("")) {
                if (amountTextField.getText().equals("All in"))
                    ButtonListeners.betButtonListener(String.valueOf(stackSizeIn), betRaiseButton.getText());
                else
                    ButtonListeners.betButtonListener(amountTextField.getText(), betRaiseButton.getText());
                updateSliderValues(stackSizeIn);
            }
        });

        amountTextField.setOnAction(e -> {
            if (!amountTextField.getText().equals("")) {
                if (amountTextField.getText().equals("All in"))
                    ButtonListeners.betButtonListener(String.valueOf(stackSizeIn), betRaiseButton.getText());
                else
                    ButtonListeners.betButtonListener(amountTextField.getText(), betRaiseButton.getText());
                updateSliderValues(stackSizeIn);
            }
        });

        checkCallButton.setOnAction(e -> {
            ButtonListeners.checkButtonListener(checkCallButton.getText());
            updateSliderValues(stackSizeIn);
        });

        foldButton.setOnAction(e -> ButtonListeners.foldButtonListener());

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int sliderNumber = (int) slider.getValue();
            if (sliderNumber >= slider.getMax())
                amountTextField.setText("All in");
            else
                amountTextField.setText(String.valueOf(sliderNumber));
        });


        //Add objects to the boxes
        stats.getChildren().addAll(nameLabel, stackLabel, positionLabel);
        stats.setAlignment(Pos.CENTER);

        twoButtonsUnderInput.getChildren().addAll(checkCallButton, foldButton);

        inputAndButtons.getChildren().addAll(amountTextField, twoButtonsUnderInput);
        inputAndButtons.setAlignment(Pos.CENTER);

        twoButtonsRight.getChildren().addAll(betRaiseButton);
        twoButtonsRight.setAlignment(Pos.CENTER);

        sliderBox.getChildren().addAll(bestHand,slider);
        sliderBox.setAlignment(Pos.CENTER);

        fullBox.getChildren().addAll(stats, leftCardImage, rightCardImage, inputAndButtons, twoButtonsRight, sliderBox);
        fullBox.setAlignment(Pos.CENTER);

        fullBoxWithLastMove.getChildren().addAll(lastMoveLabel, fullBox);
        fullBoxWithLastMove.setAlignment(Pos.CENTER);

        this.setActionsVisible(false);
        return fullBoxWithLastMove;
    }

    /**
     * Updates the values of the slider
     */
    public void updateSliderValues(long stackSize){
        Runnable task;

        task = () -> {
            long maxValue = stackSize;

            if (positionLabel.getText().equals("Position: Small blind"))
                maxValue -= currentSmallBlind;
            else if (positionLabel.getText().equals("Position: Big blind"))
                maxValue -= currentBigBlind;

            slider.setValue(currentBigBlind);
            slider.setMin(currentBigBlind);
            slider.setMax(maxValue);
            slider.setBlockIncrement(0.1f);
            slider.setMinorTickCount(0);

            /*if (maxValue < 100)
                slider.setVisible(false);
            else
                slider.setVisible(true);*/

            if (slider.getMax() > 2 * slider.getMin() && slider.getMax() / 2 > 0)
                slider.setMajorTickUnit(slider.getMax() / 2);
            else {
                slider.setMajorTickUnit(1);
                slider.setVisible(false);
            }

        };
        Platform.runLater(task);
    }

    /**
     * Show the buttons on the board
     *
     * @param visible
     */

    public void setActionsVisible(boolean visible) {
        Runnable task = () -> {
            betRaiseButton.setVisible(visible);
            checkCallButton.setVisible(visible);
            foldButton.setVisible(visible);
            amountTextField.setVisible(visible);
            slider.setVisible(visible);
        };
        Platform.runLater(task);
    }

    /**
     * Set button visibility
     *
     * @param visible
     */

    public void setVisible(boolean visible){
        betRaiseButton.setVisible(visible);
        checkCallButton.setVisible(visible);
        foldButton.setVisible(visible);
        amountTextField.setVisible(visible);
    }

    public void setPositionLabel(String pos){
        Platform.runLater(() -> positionLabel.setText(pos));
    }

    public void setStackLabel(String stack) {
        Platform.runLater(() -> stackLabel.setText(stack));
    }

    public void setLastMoveLabel(String lastMove) {
        lastMoveLabel.setText(lastMove);
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
        Platform.runLater(() -> this.bestHand.setText(bestHand));
    }


    /**
     *
     * Set card image and make them visible
     *
     * @param leftImage
     * @param rightImage
     */
    public void setCardImage(Image leftImage, Image rightImage) {
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

    public void setSliderVisibility() {
        slider.setVisible(true);
    }

    public void bustPlayer(String bustedText) {
        setLastMoveLabel("");
        setStackLabel(bustedText);
        setPositionLabel("");

        setCardImage(null, null);
    }

    public void removeHolecards() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-0.5);
        leftCardImage.setEffect(adjust);
        rightCardImage.setEffect(adjust);
    }
}
