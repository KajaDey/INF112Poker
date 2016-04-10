package gamelogic;

import gui.GUIMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by henrik on 05.04.16.
 *
 * Class to generate player names.
 */
public class NameGenerator {


    private static String filePath = "resources/nameList/names.txt";
    static ArrayList<String> names = new ArrayList<String>();
    static ArrayList<String> characterNames = new ArrayList<String>();
    static ArrayList<String> pathsOfSeries = new ArrayList<String>();
    static String pathOfSeries = "";
    static Random random = new Random();


    public static String getRandomName(){
        if (pathOfSeries.isEmpty() || characterNames.isEmpty())
            readNewSeries();

        //choose from list and then removerino
        int randomListIndex = random.nextInt(characterNames.size());
        String randomCharacter = characterNames.get(randomListIndex).substring(0,1).toUpperCase() + characterNames.get(randomListIndex).substring(1, characterNames.get(randomListIndex).length()).toLowerCase();
        characterNames.remove(randomListIndex);

        return randomCharacter;

    }

    public static void readNewSeries(){
        characterNames = new ArrayList<>();
        pathsOfSeries = new ArrayList<>();

        //add all series paths once
        if(pathsOfSeries.isEmpty()) {
            pathsOfSeries.add("resources/nameList/bigBangTheory.txt");
            pathsOfSeries.add("resources/nameList/bms.txt");
            pathsOfSeries.add("resources/nameList/breakingBad.txt");
            pathsOfSeries.add("resources/nameList/communists.txt");
            pathsOfSeries.add("resources/nameList/community.txt");
            pathsOfSeries.add("resources/nameList/dictators.txt");
            pathsOfSeries.add("resources/nameList/disney.txt");
            pathsOfSeries.add("resources/nameList/friends.txt");
            pathsOfSeries.add("resources/nameList/futurama.txt");
            pathsOfSeries.add("resources/nameList/gossipGirl");
            pathsOfSeries.add("resources/nameList/got.txt");
            pathsOfSeries.add("resources/nameList/harryPotter.txt");
            pathsOfSeries.add("resources/nameList/hotelCeasar");
            pathsOfSeries.add("resources/nameList/kapteinSabeltann.txt");
            pathsOfSeries.add("resources/nameList/kardashians");
            pathsOfSeries.add("resources/nameList/oneTreeHill");
            pathsOfSeries.add("resources/nameList/orangeIsTheNewBlack");
            pathsOfSeries.add("resources/nameList/paradiseHotel.txt");
            pathsOfSeries.add("resources/nameList/presidents.txt");
            pathsOfSeries.add("resources/nameList/rickAndMorty.txt");
            pathsOfSeries.add("resources/nameList/siliconValley.txt");
            pathsOfSeries.add("resources/nameList/spiceGirls.txt");
            pathsOfSeries.add("resources/nameList/starWars.txt");
            pathsOfSeries.add("resources/nameList/suits.txt");
            pathsOfSeries.add("resources/nameList/theLordOfTheRings.txt");
            pathsOfSeries.add("resources/nameList/theOffice.txt");
            pathsOfSeries.add("resources/nameList/theSimpsons.txt");
            pathsOfSeries.add("resources/nameList/videoGames.txt");
            pathsOfSeries.add("resources/nameList/usPresidents.txt");
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









