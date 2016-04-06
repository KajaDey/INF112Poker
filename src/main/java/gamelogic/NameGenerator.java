package gamelogic;

import com.sun.corba.se.impl.logging.IORSystemException;
import gui.GUIMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by henrik on 05.04.16.
 */
public class NameGenerator {


    private static String filePath = "resources/names.txt";
    static ArrayList<String> names = new ArrayList<String>();


    static ArrayList<String> characterNames = new ArrayList<String>();
    static ArrayList<String> pathsOfSeries = new ArrayList<String>();
    static String pathOfSeries = "";


    static Random random = new Random();

    public static String getRandomName(){

         try {
             BufferedReader reader = new BufferedReader(new FileReader(filePath));
             if (names.isEmpty()) {
                 String currentLine;
                 while ((currentLine = reader.readLine()) != null)
                     names.add(currentLine);
             }
         } catch (IOException e) {
             GUIMain.debugPrintln("Error reading names.txt");
             e.printStackTrace();
         }

        String name = names.get(random.nextInt(names.size()));

        return name.substring(0,1).toUpperCase() + name.substring(1, name.length()).toLowerCase();
    }


    public static String getRandomSeriesName(){

        //add all series paths once
        if(pathsOfSeries.isEmpty()) {
            pathsOfSeries.add("resources/GoT.txt");
            pathsOfSeries.add("resources/friends.txt");
            //and so on
        }

        //get random filepath from a list of paths of series if we havent got one

        if(pathOfSeries == "")
            pathOfSeries = pathsOfSeries.get(random.nextInt(pathsOfSeries.size()));

        //add all names of series to a list which we randomly choose from later
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathOfSeries));
                if(characterNames.isEmpty()) {
                    String currentLine;
                    while ((currentLine = reader.readLine()) != null)
                        characterNames.add(currentLine);
                }

        } catch (IOException e) {
            GUIMain.debugPrintln("Error reading names.txt");
            e.printStackTrace();
        }

        //choose from list and then removerino
        int randomListIndex = random.nextInt(characterNames.size());
        String randomCharacter = characterNames.get(randomListIndex).substring(0,1).toUpperCase() + characterNames.get(randomListIndex).substring(1, characterNames.get(randomListIndex).length()).toLowerCase();
        characterNames.remove(randomListIndex);

        return randomCharacter;

    }




    public static void main(String[] args){

        System.out.println(NameGenerator.getRandomSeriesName());
        System.out.println(NameGenerator.getRandomSeriesName());
        System.out.println(NameGenerator.getRandomSeriesName());
        System.out.println(NameGenerator.getRandomSeriesName());
        System.out.println(NameGenerator.getRandomSeriesName());

    }

}









