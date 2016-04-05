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

    public static String getRandomName(){
        Random random = new Random();
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



}









