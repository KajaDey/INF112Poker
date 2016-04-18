package gui;

import gamelogic.Decision;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * TODO: Add class description
 *
 * @author Jostein Kringlen.
 */

public class SoundPlayer{

    private String sound;
    private AudioClip audioClip;

    public void playSound(Decision.Move move){
        switch (move){
            case CHECK: playCheckSound();
                break;
            case CALL: playChipSound();
                break;
            case BET: playChipSound();
                break;
            /*case BIG_BLIND: playChipSound();
                break;
            case SMALL_BLIND:playChipSound();
                break;*/
            case ALL_IN: playChipSound();
                break;
        }
    }

    private void playCheckSound(){
        sound = "file:resources/sounds/check_sound2.wav";
        try {
            audioClip = Applet.newAudioClip(new URL(sound));
            audioClip.play();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void playChipSound(){
        sound = "file:resources/sounds/poker_chips1.wav";
        try {
            audioClip = Applet.newAudioClip(new URL(sound));
            audioClip.play();
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public void playShuffleSound(){
        sound = "file:resources/sounds/cardShuffle.wav";
        try {
            audioClip = Applet.newAudioClip(new URL(sound));
            audioClip.play();
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public void playDealCardSound(){
        sound = "file:resources/sounds/dealing_card.wav";
        try {
            audioClip = Applet.newAudioClip(new URL(sound));
            audioClip.play();
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    private void playAllInSound(){
        sound = "file:resources/sounds/allIn.wav";
        try {
            audioClip = Applet.newAudioClip(new URL(sound));
            audioClip.play();
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
}
