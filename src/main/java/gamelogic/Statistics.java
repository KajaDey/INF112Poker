package gamelogic;

import java.io.*;

/**
 * Created by henrik on 05.04.16.
 *
 * Class to represent game statistics, e.g. how many times a player has folded during the game etc. Can be written to
 * file after a game is finished.
 */
public class Statistics {

    private int winnerID;
    private int positionFinished;
    private int handsWon;
    private int handsPlayed;
    private int foldsPreFlop;
    private int aggressiveMoves;
    private int passiveMoves;
    private String bestHand;

    public Statistics(int winnerID, int positionFinished, int handsWon, int handsPlayed, int foldsPreFlop, int aggressiveMoves, int passiveMoves, String bestHand){
        this.positionFinished = positionFinished;
        this.handsWon = handsWon;
        this.handsPlayed = handsPlayed;
        this.foldsPreFlop = foldsPreFlop;
        this.aggressiveMoves = aggressiveMoves;
        this.passiveMoves = passiveMoves;
        this.bestHand = bestHand;
        this.winnerID = winnerID;
    }

    /**
     * Prints the current statistics to a file and stores it in the stats-directory
     * If the directory is not present, this method will create it
     *
     */
    public void printStatisticsToFile(){
        try {
            File statsFile = new File("stats/Game" + System.currentTimeMillis() / 100000 + ".txt");
            new File("stats").mkdir();
            PrintWriter fw = new PrintWriter(statsFile, "UTF-8");
            fw.print(this.toString());
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            System.out.println("Error when writing statistics to file");
            ioe.printStackTrace();
        }
    }

    @Override
    public String toString() {
        int p = positionFinished;
        String allStats = "";
        allStats += "You finished " + p + (p == 1 ? "st" : p==2 ? "nd" : p==3 ? "rd" : "th") + '\n';
        allStats += "You won " + handsWon + " of the " + handsPlayed + " hands you played\n";
        allStats += "You folded " + foldsPreFlop + " times pre-flop\n";
        allStats += "You made " + aggressiveMoves + " aggressive moves, and " + passiveMoves + " passive moves\n";
        allStats += "Your best hand was " + bestHand;
        return allStats;
    }

    public int getWinnerID() { return winnerID; }

}

