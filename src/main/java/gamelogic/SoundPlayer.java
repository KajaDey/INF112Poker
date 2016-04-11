package gamelogic;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sun.applet.Main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by kristianrosland on 11.04.2016.
 */
public class SoundPlayer {
    public enum Sound { CHECK_SOUND, CHIPS_SOUND };

    public void playSound(Sound sound) {
        switch(sound) {
            case CHECK_SOUND:

                break;
            case CHIPS_SOUND:
                //play("poker_chips1.wav");
                break;
        }

    }

    private void play(String clipName) {
        try {
            final AudioClip clip = new AudioClip("resources/sounds/"+clipName);

            clip.play(1.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playS(String filename) {
        String path = "resources/sounds/" + filename;

        try {
            Media sound = new Media(new File(path).toURI().toString());
            MediaPlayer mp = new MediaPlayer(sound);
            mp.play();
        } catch (Exception e) {
            System.out.println("Catch");
        }
    }


}
