package gamelogic;

import java.util.*;

/**
 * Created by kristianrosland on 16.03.2016.
 *
 * Class to represent the pot at all times.
 */
public class Pot {

    private long potSize;
    private Map<Integer, Long> amountPlayerCanClaim;

    public Pot() {
        this.potSize = 0;
        this.amountPlayerCanClaim = new HashMap<>();
    }

    /**
     * TODO write javadoc
     * @return
     */
    public long getPotSize() { return potSize; }

    /**
     * TODO write javadoc
     * @param userID
     * @param amountToAdd
     * @return
     */
    public long addToPot(int userID, long amountToAdd) {
        if (amountPlayerCanClaim.get(userID) == null)
            amountPlayerCanClaim.put(userID, 0L);

        amountPlayerCanClaim.put(userID, amountPlayerCanClaim.get(userID) + amountToAdd);

        assert amountToAdd >= 0 : "A negative number was added to the pot.";

        potSize += amountToAdd;
        return potSize;
    }

    /**
     * TODO write javadoc
     * @param playerID
     * @return
     */
    public long getSharePotPlayerCanWin(int playerID) {
        final long amountPlayerHasPutIn = amountPlayerCanClaim.get(playerID);
        long total = 0;

        for (Integer id : amountPlayerCanClaim.keySet()) {
            long amount = amountPlayerCanClaim.get(id);
            if (amountPlayerHasPutIn >= amount) { //If player has put >= this player
                total += amount;
                amountPlayerCanClaim.put(id, 0L);
            } else {
                total += amountPlayerHasPutIn;
                amountPlayerCanClaim.put(id, amount-amountPlayerHasPutIn);
            }
        }

        potSize -= total;

        return total;
    }


    public void handOutPot(List<Player> players, List<Card> communityCards) {
        //Create a copy of the players list to avoid messing with GameLogic
        ArrayList<Player> playersCopy = new ArrayList<Player>();
        players.stream().forEach(p -> playersCopy.add(p));

        while (potSize > 0) {
            handOutPotShare(playersCopy, communityCards);
        }
    }

    private void handOutPotShare(List<Player> players, List<Card> communityCards) {
        ArrayList<Player> winners = getMainPotWinners(players, communityCards);

        //Comparator to check which player has put the most in the pot
        final Comparator<Player> comp = (p1, p2) -> Long.compare(amountPlayerCanClaim.get(p1.getID()), amountPlayerCanClaim.get(p2.getID()));

        while (winners.size() > 0) {
            Player minPlayer = winners.stream().min(comp).get();
            long minPlayerPutIn = amountPlayerCanClaim.get(minPlayer.getID());
            long splitPot = 0;

            //Get amount from each player
            for (Integer id : amountPlayerCanClaim.keySet()) {
                long putIn = amountPlayerCanClaim.get(id);
                long addToPot = Math.min(minPlayerPutIn, putIn);
                amountPlayerCanClaim.put(id, putIn - addToPot);
                splitPot += addToPot;
            }

            for (Player p : winners) {
                p.incrementStack(splitPot / winners.size());
            }

            potSize -= splitPot;
            winners.remove(minPlayer);
            players.remove(minPlayer);
        }
    }

    private ArrayList<Player> getMainPotWinners(List<Player> players, List<Card> communityCards) {
        assert players.size() > 0 : "Tried to get main pot winner for " + players.size() + " players";
        ArrayList<Hand> hands = new ArrayList<>();

        //Add all player hands
        players.stream().forEach(p -> hands.add(p.getHand(communityCards)));

        //Use streams to find the best hand
        Hand bestHand = hands.stream().max((h1,h2) -> h1.compareTo(h2)).get();

        //Generate an ArrayList of all the players that match the best hand
        ArrayList<Player> mainPotWinners = new ArrayList<>();
        players.stream().filter(p -> p.getHand(communityCards).compareTo(bestHand) == 0).forEach(p -> mainPotWinners.add(p));

        assert mainPotWinners.size() != 0 : "getMainPotWinner returned no players";

        return mainPotWinners;
    }


}
