package gamelogic;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by henrik on 05.04.16.
 *
 * Class to represent game statistics, e.g. how many times a player has folded during the game etc. Can be written to
 * file after a game is finished.
 */
public class Statistics {

    private int playerID;
    private Map<Integer, Integer> rankingTable;
    private Map<Integer, String> names;
    private int handsWon, handsPlayed;
    private int foldsPreFlop;
    private int aggressiveMoves, passiveMoves;
    private String bestHand;

    public Statistics(Player player, Map<Integer, String> names, Map<Integer, Integer> rankingTable){
        this.rankingTable = rankingTable;
        this.names = names;
        this.handsWon = player.handsWon();
        this.handsPlayed = player.handsPlayed();
        this.foldsPreFlop = player.preFlopFolds();
        this.aggressiveMoves = player.aggressiveMoves();
        this.passiveMoves = player.passiveMoves();
        this.bestHand = player.getBestHand();
        this.playerID = player.getID();
    }

    /**
     * Prints the current statistics to a file and stores it in the stats-directory
     * If the directory is not present, this method will create it
     *
     */
    public void printStatisticsToFile(){
        long gameID = System.currentTimeMillis() / 100000;

        try {
            //Create the file
            File statsFile = new File("stats/Game" + gameID + ".txt");

            //Create the directory if it doesn't exist
            new File("stats").mkdir();
            PrintWriter fw = new PrintWriter(statsFile, "UTF-8");

            //Write stats to file
            fw.print("Game finished " + dateTime() + "\n\n" + this.toString());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            System.out.println("Error when writing statistics to file");
            ioe.printStackTrace();
        }
    }

    @Override
    public String toString() {
        int p = rankingTable.get(playerID);

        String allStats = "";
        allStats += "Personal stats \n";
        allStats += " - You finished " + p + (p == 1 ? "st" : p==2 ? "nd" : p==3 ? "rd" : "th") + '\n';
        allStats += " - You won " + handsWon + " of the " + handsPlayed + (handsPlayed==1 ? " hand" : " hands") + " you played" + '\n';
        allStats += " - You folded " + foldsPreFlop + (foldsPreFlop==1 ? " time" : " times") + " pre-flop" + '\n';
        allStats += " - You made " + aggressiveMoves + " aggressive " + (aggressiveMoves==1 ? "move" : "moves") + ", and "
                + passiveMoves + " passive " + (passiveMoves==1 ? "move" : "moves") + '\n';
        allStats += " - Your best hand was " + bestHand + "\n\n";
        allStats += getRankingTable();
        return allStats;
    }

    /**
     * @return The client that finished in position 1 (-1 if no winner is present)
     */
    public int getWinnerID() {
        for (Integer clientID : rankingTable.keySet()) {
            if (rankingTable.get(clientID) == 1)
                return clientID;
        }
        return -1;
    }

    /**
     * @return The current date and time, format: yyyy/MM/dd HH:mm:ss
     */
    private String dateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * @return A list with the positions of each player (separated with new lines)
     */
    private String getRankingTable() {
        String returnString = "";
        String [] table = new String[rankingTable.size()];
        for (Integer id : rankingTable.keySet())
            table[rankingTable.get(id)-1] = names.get(id);

        for (int i = 0; i < table.length; i++)
            returnString += (i+1) + (i==0 ? "st" : i==1 ? "nd" : i==2 ? "rd" : "th") + ": " + table[i] + "\n";

        return returnString;
    }

}

