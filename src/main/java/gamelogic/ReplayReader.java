package gamelogic;

import gui.GUIMain;
import gui.GameSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

/**
 * This class i made to read a replay file from a previous game.
 * The text is converted to objects which the program can read.
 *
 * @author André Dyrstad
 */
public class ReplayReader {

    private static InfoType currentType = null;

    private static ArrayDeque<Card> communityCards = new ArrayDeque<>();
    private static ArrayDeque<Card> holdCards = new ArrayDeque<>();
    private static ArrayDeque<String> settings = new ArrayDeque<>();
    private static ArrayDeque<Decision> decisions = new ArrayDeque<>();

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
                                communityCards.add(makeCard(line));
                                break;
                            case HOLD:
                                holdCards.add(makeCard(line));
                                break;
                            case SETTINGS:
                                settings.add(line);
                                break;
                            case DECISION:

                                decisions.add(makeDecision(line));
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
            GUIMain.debugPrint("Uploaded replay without problems");
        }
        catch (IOException | NoSuchElementException | IllegalArgumentException e){
            System.out.println("Error while uploading replay \n" + e);
        }
    }

    /**
     * Returns the next decision made in the game.
     *
     * @return next Decision
     */
    public static Decision getNextDecision(){
        return decisions.pop();
    }

    /**
     * Returns the next holecard
     *
     * @return holecard
     */

    public static Card getNextHold(){
        return holdCards.pop();
    }

    /**
     * Returns the next community card
     *
     * @return community card
     */
    public static Card getNextCommunity(){
        return communityCards.pop();
    }

    public static GameSettings getSettings(){
        return new GameSettings(Long.parseLong(settings.pop()),Long.parseLong(settings.pop()),
                Long.parseLong(settings.pop()),Integer.parseInt(settings.pop()),Integer.parseInt(settings.pop()),
                AIType.fromString(settings.pop()),Integer.parseInt(settings.pop()));
    }

    /**
     *
     * Converts a string from the replay file to a decision
     *
     * @param move from file
     * @return new decision
     */
    private static Decision makeDecision(String move){
        String[] split = move.split(" ");
        if(split[2].equals("Allin"))
            split[2] = "ALL_IN";

        split[2] = split[2].toUpperCase();

        if(split.length == 4)
            return new Decision(Decision.Move.valueOf(split[2]));
        else
            return new Decision(Decision.Move.valueOf(split[2]),Long.parseLong(split[3]));
    }

    /**
     *
     * Converts a string from the replay file to a card
     *
     * @param card from file
     * @return new card
     */
    private static Card makeCard(String card){

        String[] split = card.split("");

        if(split.length == 3)
            split[1] = "10";

        switch (split[1]){
            case "J":
                split[1]="11";
                break;
            case "Q":
                split[1]="12";
                break;
            case "K":
                split[1]="13";
                break;
            case "A":
                split[1]="14";
                break;
        }
        switch (split[0]) {
            case "\u2660": split[0] = "SPADES"; break;
            case "\u2665": split[0] = "HEARTS"; break;
            case "\u2666": split[0] = "DIAMONDS"; break;
            case "\u2663": split[0] = "CLUBS"; break;
        }
        return Card.of(Integer.parseInt(split[1]), Card.Suit.valueOf(split[0])).get();
    }

}