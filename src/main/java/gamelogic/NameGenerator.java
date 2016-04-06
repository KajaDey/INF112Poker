package gamelogic;

import com.sun.corba.se.impl.logging.IORSystemException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by henrik on 05.04.16.
 *
 * TODO class information
 */
public class NameGenerator {


    private static String filePath = "/home/henrik/inf112/inf112v16-g4/names.txt";

    static ArrayList<String> names = new ArrayList<String>();

    /**
     * TODO write javadoc
     * @return A random name
     * @throws IOException
     */
    public static String getRandomName() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        Random random = new Random();
        if (names.isEmpty()) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null)
                names.add(currentLine);
        }

        return names.get(random.nextInt(names.size()));


    }



}









