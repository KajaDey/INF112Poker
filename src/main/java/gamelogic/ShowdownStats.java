package gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An object that contains all the info about a showdown (such as players, (side)pots, etc..)
 *
 * @author Kristian Rosland
 */
public class ShowdownStats {
    private List<List<Player>> sidePots;
    private List<Long> sidePotSizes;
    private List<Card> communityCards;
    private List<Player> allPlayers;

    public ShowdownStats(List<Player> players, List<Card> communityCards) {
        sidePots = new ArrayList<>();
        sidePotSizes = new ArrayList<>();
        this.communityCards = communityCards;
        this.allPlayers = new ArrayList<>(players);
    }

    public void addSidePot(List<Player> players, long size) {
        sidePots.add(players);
        sidePotSizes.add(size);
    }

    public Map<Integer, Card[]> getHoleCards() {
        Map<Integer, Card[]> holeCards = new HashMap<>();
        allPlayers.stream().forEach(p -> holeCards.put(p.getID(), p.getHoleCards()));
        return holeCards;
    }

    /**
     * @return A text containing who won the pot(s), e.g.: "Kristian won the main pot of 1000 with ..."
     */
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
        return printable.trim();
    }
}
