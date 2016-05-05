package gamelogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by ady on 05/05/16.
 */
public class ReplayReader {

    private static InfoType currentType = null;

    private static ArrayDeque<String> communityCards = new ArrayDeque<>();
    private static ArrayDeque<String> holdCards = new ArrayDeque<>();
    private static ArrayList<String> settings = new ArrayList<>();
    private static ArrayDeque<String> decisions = new ArrayDeque<>();

    public enum InfoType{
        COMMUNITY,HOLD,DECISION,SETTINGS
    }

    /**
     * Reads the chosen file and saves it. This is later used to replay a game.
     *
     * @param file you want to watch
     */
    public static void readFile(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while(br.ready()){

            String line = br.readLine();

                switch (line) {
                    case "COMMUNITY CARDS":
                        currentType = InfoType.COMMUNITY;
                        break;
                    case "CARDS":
                        currentType = InfoType.HOLD;
                        break;
                    case "SETTINGS":
                        currentType = InfoType.SETTINGS;
                        break;
                    case "DECISIONS":
                        currentType = InfoType.DECISION;
                        break;
                    default:

                        switch (currentType) {
                            case COMMUNITY:
                                communityCards.add(line);
                                break;
                            case HOLD:
                                holdCards.add(line);
                                break;
                            case SETTINGS:
                                settings.add(line);
                                break;
                            case DECISION:
                                decisions.add(line);
                                break;
                            default:
                                break;
                        }

                        break;

                }

            }
        }
        catch (IOException e){
            System.out.println(e);
        }

        communityCards.forEach(System.out::println);
        System.out.println("\n");
        holdCards.forEach(System.out::println);
        System.out.println("\n");
        settings.forEach(System.out::println);
        System.out.println("\n");
        decisions.forEach(System.out::println);

    }
}
