package gamelogic;

import java.io.FileWriter;

/**
 * Created by henrik on 05.04.16.
 *
 * Class to represent game statistics, e.g. how many times a player has folded during the game etc. Can be written to
 * file after a game is finished.
 */
public class Statistics {

    private int positionFinished;
    private int handsWon;
    private int handsPlayed;
    private int foldsPreflop;
    private int aggressiveMoves;
    private int passiveMoves;
    private String bestHand;
    private long highestStack;

    public Statistics(int positionFinished, int handsWon, int handsPlayed, int foldsPreflop, int aggressiveMoves, int passiveMoves, String bestHand,long highestStack ){
        this.positionFinished = positionFinished;
        this.handsWon = handsWon;
        this.handsPlayed = handsPlayed;
        this.foldsPreflop = foldsPreflop;
        this.aggressiveMoves = aggressiveMoves;
        this.passiveMoves = passiveMoves;
        this.bestHand = bestHand;
        this.highestStack = highestStack;
    }

    /**
     * TODO write javadoc
     * @param filepath
     * @throws Exception
     */
    public void printStatisticsToFile(String filepath) throws Exception{

        FileWriter fw = new FileWriter(filepath);

        fw.write("Position finished: " + positionFinished + '\n');
        fw.write("Hands won: " + handsWon + '\n');
        fw.write("Hands played: " + handsPlayed + '\n');
        fw.write("Folds preflop: " + foldsPreflop + '\n');
        fw.write("aggressiveMoves: " + aggressiveMoves + '\n');
        fw.write("passiveMoves: " + passiveMoves + '\n');
        fw.write("best hand: " + bestHand + '\n');
        fw.write("highest stack: " + highestStack + '\n');

        fw.close();
    }

}

