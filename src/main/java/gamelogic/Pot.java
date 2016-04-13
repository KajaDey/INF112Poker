package gamelogic;

import gui.GUIMain;

import java.util.*;

/**
 * Created by kristianrosland on 16.03.2016.
 *
 * Class to represent the pot and who can claim what
 */
public class Pot {

    private long potSize;
    private Map<Integer, Long> amountPlayerCanClaim;

    public Pot() {
        this.potSize = 0;
        this.amountPlayerCanClaim = new HashMap<>();
    }

    /** @return current pot size */
    public long getPotSize() { return potSize; }

    /**
     *  Increment the pot and update the amount the player can claim if he wins the pot
     * @param userID
     * @param amountToAdd
     */
    public void addToPot(int userID, long amountToAdd) {
        if (amountPlayerCanClaim.get(userID) == null)
            amountPlayerCanClaim.put(userID, 0L);

        amountPlayerCanClaim.put(userID, amountPlayerCanClaim.get(userID) + amountToAdd);

        assert amountToAdd >= 0 : "A negative number was added to the pot.";

        potSize += amountToAdd;
    }

    /**
     * Hand out the pot (and side pots) to the winners
     * @param playersInHand
     * @param communityCards
     */
    public void handOutPot(List<Player> playersInHand, List<Card> communityCards, ShowdownStats showdown) {
        //Create a copy of the players list to avoid messing with GameLogic
        ArrayList<Player> playersCopy = new ArrayList<>(playersInHand);

        while (potSize > 0) {
            handOutPotShare(playersCopy, communityCards, showdown);
        }
    }

    /**
     *  Hand out a part of the pot (or the whole pot if there is only one winner)
     * @param players
     * @param communityCards
     */
    private void handOutPotShare(List<Player> players, List<Card> communityCards, ShowdownStats showdown) {
        ArrayList<Player> winners = getPotWinners(players, communityCards);

        //Make a copy of the winners array for use in showdown stats
        ArrayList<Player> winnersCopy = new ArrayList<>(winners);

        long size = 0;

        //Comparator to check which player has put the most in the pot
        final Comparator<Player> comp = (p1, p2) -> Long.compare(amountPlayerCanClaim.get(p1.getID()), amountPlayerCanClaim.get(p2.getID()));

        while (winners.size() > 0) {
            Player minPlayer = winners.stream().min(comp).get();
            long minPlayerPutIn = amountPlayerCanClaim.get(minPlayer.getID());
            long potShare = 0;

            //Get amount from each player
            for (Integer id : amountPlayerCanClaim.keySet()) {
                long putIn = amountPlayerCanClaim.get(id);
                long addToPot = Math.min(minPlayerPutIn, putIn);
                amountPlayerCanClaim.put(id, putIn - addToPot);
                potShare += addToPot;
            }

            //Hand out pot-shares and inform the players that they won the pot
            long handedOut = 0; //To avoid rounding-errors when pot is split, this tracks what has been handed out
            for (Player p : winners) {
                handedOut += potShare / winners.size();
                p.handWon(p.getHand(communityCards), potShare / winners.size());
            }

            //If there was a rounding error, the split pot is handed out to a random winner
            if (potShare - handedOut > 0) {
                GUIMain.debugPrintln(minPlayer.getName() + " got the spare pot of " + (potShare - handedOut));
                minPlayer.incrementStack(potShare-handedOut);
            }

            potSize -= potShare;
            size += potShare;
            winners.remove(minPlayer);
            players.remove(minPlayer);
        }

        if (size > 0)
            showdown.addSidePot(winnersCopy, size);
    }

    /**
     *   Find out how much a player would get from the pot if he won.
     *   Does not change the pot size
     * @param playerID
     * @return The amount the player would get
     */
    public long getSharePlayerCanWin(int playerID) {
        long share = 0;
        long canClaim = amountPlayerCanClaim.get(playerID) == null ? 0 : amountPlayerCanClaim.get(playerID);

        for (Integer i : amountPlayerCanClaim.keySet()) {
            long putIn = amountPlayerCanClaim.get(i);
            long amount = Math.min(canClaim, putIn);
            share += amount;
            amountPlayerCanClaim.put(i, putIn - amount);
        }

        potSize -= share;
        return share;
    }

    /**
     *  Get the winner(s) among the given players
     * @param players
     * @param communityCards
     * @return  A list of the player(s) with the best hand
     */
    private ArrayList<Player> getPotWinners(List<Player> players, List<Card> communityCards) {
        assert players.size() > 0 : "Tried to get pot winner for " + players.size() + " players";
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
