package gamelogic;

import gui.GUIMain;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by henrik on 05.04.16.
 *
 * Class to generate player names.
 */
public class NameGenerator {


    private static String filePath;
    public static ArrayList<String> names = new ArrayList<>();
    public static ArrayList<String> characterNames = new ArrayList<>();
    public static ArrayList<String> pathsOfSeries = new ArrayList<>();
    public static String pathOfSeries = "";
    public  static Random random = new Random();

    static {
        filePath = "resources/nameList/names.txt";
    }

    /**
     *  Get a random name from a randomly selected theme series
     *  Guarantees no duplicates, removes all white spaces
     *  @return
     */
    public static String getRandomName(){
        if (pathOfSeries.isEmpty() || characterNames.isEmpty())
            readNewSeries();

        //choose from list and then remove
        int randomListIndex = random.nextInt(characterNames.size());
        String randomCharacter = characterNames.get(randomListIndex).substring(0,1).toUpperCase() + characterNames.get(randomListIndex).substring(1, characterNames.get(randomListIndex).length()).toLowerCase();
        characterNames.remove(randomListIndex);

        return randomCharacter.trim();
    }

    /**
     * Resets the current series to get names from
     * Should be used every time a new game is initialized
     */
    public static void readNewSeries(){
        characterNames = new ArrayList<>();
        pathsOfSeries = new ArrayList<>();

        String url = "resources/nameList/";
        ArrayList<String> names = new ArrayList<>(Arrays.asList(new File(url).list()));

        for (String name : names) {
            name = url + name;
            pathsOfSeries.add(name);
        }

        //get random filepath from a list of paths of series if we haven't got one
        pathOfSeries = pathsOfSeries.get(random.nextInt(pathsOfSeries.size()));

        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathOfSeries));
            String currentLine;
            while ((currentLine = reader.readLine()) != null)
                characterNames.add(currentLine);
        } catch (IOException e) {
            GUIMain.debugPrintln("Error reading file: " + pathOfSeries);
            e.printStackTrace();
        }
    }
}









