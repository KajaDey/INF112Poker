package gui;

import gamelogic.Decision;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

/**
 * Created by Kristian Rosland on 24.04.2016.
 */
public class RemainingTimeBar extends ProgressBar {

    private Thread countDown;

    public RemainingTimeBar() {
        this.minHeight(75       );
        this.setProgress(1.0);
    }

    public void setTimer(long timeInMilliseconds, Decision.Move moveToExecute) {
        // If there is a count down going on already, we need to interrupt it
        if (countDown != null && !countDown.isInterrupted())
            countDown.interrupt();

        //Set the progress property to our Timer-task and start the progress bar
        Task timer = timer(timeInMilliseconds, moveToExecute);
        this.progressProperty().unbind();
        this.progressProperty().bind(timer.progressProperty());
        countDown = new Thread(timer);
        countDown.start();

        //Add a change listener to change colors at certain values (red, yellow, orange, red)
        addChangeListener();
    }

    /**
     *  A runnable task that changes the value of the RemainingTimeBar.
     * @param time  Time it takes from 100% to 0%
     * @param moveToExecute  Move this method executes if it reaches 0%
     * @return
     */
    public Task timer(long time, Decision.Move moveToExecute) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                updateProgress(10, 10);
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(time/100);
                    updateProgress(99-i, 100);
                }
                if (moveToExecute == Decision.Move.FOLD)
                    ButtonListeners.foldButtonListener();
                else if (moveToExecute == Decision.Move.CHECK)
                    ButtonListeners.checkButtonListener("Check");

                return true;
            }
        };
    }

    /**
     * Method for adding a change listener to the TimeBar. Changes to color based on the time left
     */
    private void addChangeListener() {
        RemainingTimeBar bar = this;
        this.progressProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue == null ? 0 : newValue.doubleValue();
            String style = "-fx-accent: rgba(";
            if (progress > 0.5)
                style += 255-(255 * (progress - 0.5) * 2) + ",255,0";
            else
                style += "255," + (255 * progress * 2) + ",0";

            bar.setStyle(style+",1.0);");
        });
    }


}
