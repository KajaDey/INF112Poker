package gui;
import gamelogic.Decision;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;



/**
 * This class plays a sound for a given move for both players and AIs.
 * The sounds that are played are all self made, which means that all of the sounds
 * are recorded using poker chips, a stack of cards, and a microphone.
 *
 * @author Jostein Kringlen.
 */

public class SoundPlayer{

    private String sound;
    private AudioClip audioClip;
    private static boolean muted;

    /**
     * Method for differentiating between the different sounds for the different moves
     * @param move The move to play sound for
     */
    public void getSoundForDecision(Decision.Move move){
        switch (move){
            case CHECK: playCheckSound();
                break;
            case CALL: playChipSound();
                break;
            case BET: playChipSound();
                break;
            case ALL_IN: playAllInSound();
                break;
            case FOLD: playFoldSound();
                break;
            case RAISE: playChipSound();
                break;
        }
    }

    /**
     * Gets one of the 5 check sounds, and calls the method for playing it.
     */
    private void playCheckSound(){
        int randomNumber = getRandomNumber(1, 5);
        switch (randomNumber){
            case 1:
                sound = "file:resources/sounds/check1.wav";
                break;
            case 2:
                sound = "file:resources/sounds/check2.wav";
                break;
            case 3:
                sound = "file:resources/sounds/check3.wav";
                break;
            case 4:
                sound = "file:resources/sounds/check4.wav";
                break;
            case 5:
                sound = "file:resources/sounds/check5.wav";
                break;
        }
        playSound(sound);
    }

    /**
     * Gets one of the 5 chip sounds, and calls the method for playing it
     */
    private void playChipSound(){
        int randomNumber = getRandomNumber(1, 5);
        switch (randomNumber){
            case 1:
                sound = "file:resources/sounds/chips1.wav";
                break;
            case 2:
                sound = "file:resources/sounds/chips2.wav";
                break;
            case 3:
                sound = "file:resources/sounds/chips3.wav";
                break;
            case 4:
                sound = "file:resources/sounds/chips4.wav";
                break;
            case 5:
                sound = "file:resources/sounds/chips5.wav";
                break;
        }
        playSound(sound);
    }

    /**
     * Gets the shuffle sound, and calls the method for playing it
     */
    public void playShuffleSound(){
        sound = "file:resources/sounds/shuffle.wav";
        playSound(sound);
    }

    /**
     * Gets the card sound, and calls the method for playing it
     */
    public void playDealCardSound(){
        sound = "file:resources/sounds/cardsound.wav";
        playSound(sound);
    }

    /**
     * Gets one of the 3 all in sounds, and calls the method for playing it
     */
    private void playAllInSound(){
        int randomNumber = getRandomNumber(1, 3);
        switch (randomNumber){
            case 1:
                sound = "file:resources/sounds/allin1.wav";
                break;
            case 2:
                sound = "file:resources/sounds/allin2.wav";
                break;
            case 3:
                sound = "file:resources/sounds/allin3.wav";
                break;
        }
        playSound(sound);
    }

    /**
     * Gets one of the 3 fold sounds, and calls the method for playing it
     */
    private void playFoldSound(){
        int randomNumber = getRandomNumber(1,3);
        switch (randomNumber){
            case 1:
                sound = "file:resources/sounds/fold1.wav";
                break;
            case 2:
                sound = "file:resources/sounds/fold2.wav";
                break;
            case 3:
                sound = "file:resources/sounds/fold3.wav";
                break;
        }
        playSound(sound);
    }

    /**
     * Method for getting a random number between min and max
     * @param min The minimum number
     * @param max The maximum number
     * @return The random number between min and max
     */
    private int getRandomNumber(int min, int max){
        Random random = new Random();
        return random.nextInt(max) + min;
    }

    /**
     * The method for playing the sound of the move
     * @param soundURL The name/URL of the sound to be played.
     */
    private void playSound(String soundURL) {
        if (!muted) {
            try {
                audioClip = Applet.newAudioClip(new URL(soundURL));
                audioClip.play();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
    }

    public void muteSound(){
        if (!muted) muted = true;
        else muted = false;
    }
    public boolean getMutedValue(){
        return muted;
    }
}
