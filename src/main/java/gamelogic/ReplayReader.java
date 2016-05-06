package gamelogic;

import gui.GameSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;

/**
 * Created by ady on 05/05/16.
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
        }
        catch (IOException e){
            System.out.println(e);
        }

    }

    public static Decision getNextDecision(){
        return decisions.pop();
    }

    public static Card getNextHold(){
        return holdCards.pop();
    }

    public static Card getNextCommunity(){
        return communityCards.pop();
    }

    public static GameSettings getSettings(){
        return new GameSettings(Long.parseLong(settings.pop()),Long.parseLong(settings.pop()),
                Long.parseLong(settings.pop()),Integer.parseInt(settings.pop()),Integer.parseInt(settings.pop()),
                AIType.fromString(settings.pop()),Integer.parseInt(settings.pop()));
    }

    private static Decision makeDecision(String move){
        String[] split = move.split(" ");
        if(split[2].equals("Allin"))
            split[2] = "ALL_IN";

        split[2] = split[2].toUpperCase();
        System.out.println(split[2]);

        if(split.length == 4)
            return new Decision(Decision.Move.valueOf(split[2]));
        else
            return new Decision(Decision.Move.valueOf(split[2]),Long.parseLong(split[3]));
    }

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
