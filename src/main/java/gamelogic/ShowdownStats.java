package gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristianrosland on 09.04.2016.
 */
public class ShowdownStats {
    ArrayList<ArrayList<Player>> sidePots;
    ArrayList<Long> sidePotSizes;
    List<Card> communityCards;
    ArrayList<Player> allPlayers;

    public ShowdownStats(List<Player> players, List<Card> communityCards) {
        sidePots = new ArrayList<>();
        sidePotSizes = new ArrayList<>();
        this.communityCards = communityCards;
        this.allPlayers = new ArrayList<>();

        players.stream().forEach(p -> allPlayers.add(p));
    }

    public void addSidePot(ArrayList<Player> players, long size) {
        sidePots.add(players);
        sidePotSizes.add(size);
    }

    public int numberOfPlayers() {
        return allPlayers.size();
    }

    public Map<Integer, Card[]> getHoleCards() {
        Map<Integer, Card[]> holeCards = new HashMap<>();
        allPlayers.stream().forEach(p -> holeCards.put(p.getID(), p.getHoleCards()));
        return holeCards;
    }

    public ArrayList<Player> getAllPlayers() {
        return allPlayers;
    }

    public String getWinnerText() {
        String printable = "";

        for (int i = 0; i < sidePots.size(); i++) {
            Hand winningHand = null;

            ArrayList<String> names = new ArrayList<>();
            for (Player p : sidePots.get(i)) {
                names.add(p.getName());
                winningHand = p.getHand(communityCards);
            }

            for (int ind = 0; ind < names.size(); ind++)
                printable += names.get(ind) + (ind == names.size()-1 ? " " : ind == names.size() - 2 ? " and " : ", ");

            long potSize = sidePotSizes.get(i);
            printable += (sidePots.get(i).size() == 1 ? "won" : "split") + (i==0 ? " the main" : " a side")+" pot of " + potSize;

            HandCalculator hc = new HandCalculator(winningHand);
            printable += " with " + hc.getBestHandString().toLowerCase() + "\n";
        }
        return printable;
    }
}
