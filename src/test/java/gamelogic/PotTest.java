package gamelogic;

import gui.GameSettings;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by kristianrosland on 09.04.2016.
 */
public class PotTest {

    ArrayList<Player> players;
    GameSettings settings;
    Pot pot;

    @Before
    public void setup() {
        settings = new GameSettings(0, 50, 25, 5, 10, AIType.MCTS_AI);
        players = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            players.add(new Player("Player" + i, settings , i));

        pot = new Pot();
    }

    @Test
    public void testSplitPotBetweenThreePlayersWhoHaveNotPutInSameAmount() {
        pot.addToPot(0, 3000);
        pot.addToPot(1, 9000);
        pot.addToPot(2, 12000);
        pot.addToPot(3, 6000);
        pot.addToPot(4, 12000);

        //Set up hole cards so that players 0, 1 and 2 will split the pot
        players.get(0).setHoleCards(Card.of(7, Card.Suit.SPADES).get(), Card.of(8, Card.Suit.CLUBS).get());
        players.get(1).setHoleCards(Card.of(7, Card.Suit.CLUBS).get(), Card.of(8, Card.Suit.SPADES).get());
        players.get(2).setHoleCards(Card.of(7, Card.Suit.HEARTS).get(), Card.of(8, Card.Suit.DIAMONDS).get());
        players.get(3).setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(3, Card.Suit.DIAMONDS).get());
        players.get(4).setHoleCards(Card.of(3, Card.Suit.SPADES).get(), Card.of(4, Card.Suit.CLUBS).get());

        //Add community cards to make it a split
        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(9, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(10, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(2, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(4, Card.Suit.SPADES).get());

        //Remove the 'folded' players
        players.remove(4);

        //Hand out the pot
        ShowdownStats s = new ShowdownStats(players, communityCards);
        pot.handOutPot(players, communityCards, s);

        assertTrue(pot.getPotSize() == 0);
        assertEquals(players.get(0).getStackSize(), 5000);
        assertEquals(players.get(1).getStackSize(), 15500);
        assertEquals(players.get(2).getStackSize(), 21500);
        assertEquals(players.get(3).getStackSize(), 0);
    }

    @Test
    public void testSingleWinnerGetsTheEntirePot() {
        pot.addToPot(0, 12000);
        pot.addToPot(1, 8000);
        pot.addToPot(2, 12000);
        pot.addToPot(3, 6000);
        pot.addToPot(4, 12000);

        //Set up hole cards so that player 0 wins the pot
        players.get(0).setHoleCards(Card.of(7, Card.Suit.SPADES).get(), Card.of(8, Card.Suit.CLUBS).get());
        players.get(1).setHoleCards(Card.of(13, Card.Suit.CLUBS).get(), Card.of(13, Card.Suit.SPADES).get());
        players.get(2).setHoleCards(Card.of(12, Card.Suit.HEARTS).get(), Card.of(12, Card.Suit.DIAMONDS).get());
        players.get(3).setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(3, Card.Suit.DIAMONDS).get());
        players.get(4).setHoleCards(Card.of(3, Card.Suit.SPADES).get(), Card.of(4, Card.Suit.CLUBS).get());

        //Add community cards
        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(9, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(10, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(2, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(4, Card.Suit.SPADES).get());

        //Hand out the pot
        ShowdownStats s = new ShowdownStats(players, communityCards);
        pot.handOutPot(players, communityCards, s);

        assertTrue(pot.getPotSize() == 0);
        assertTrue(players.get(0).getStackSize() == 50000);
        for (int i = 1; i < players.size(); i++)
            assertEquals(players.get(i).getStackSize(), 0);
    }

    @Test
    public void testThatMainAndSidePotIsHandedOutCorrectly() {
        pot.addToPot(0, 5000);
        pot.addToPot(1, 8000);
        pot.addToPot(2, 4000);
        pot.addToPot(3, 11000);
        pot.addToPot(4, 0);

        //Setup hands so that player 0 wins main pot, player 1 and 2 split side pot
        players.get(0).setHoleCards(Card.of(12, Card.Suit.SPADES).get(), Card.of(13, Card.Suit.CLUBS).get());
        players.get(1).setHoleCards(Card.of(7, Card.Suit.CLUBS).get(), Card.of(8, Card.Suit.SPADES).get());
        players.get(2).setHoleCards(Card.of(7, Card.Suit.HEARTS).get(), Card.of(8, Card.Suit.DIAMONDS).get());
        players.get(3).setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(3, Card.Suit.DIAMONDS).get());
        players.get(4).setHoleCards(Card.of(3, Card.Suit.SPADES).get(), Card.of(4, Card.Suit.CLUBS).get());

        //Add community cards
        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(9, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(10, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(2, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(4, Card.Suit.SPADES).get());

        //Player 4 folded preflop
        players.remove(4);

        //Hand out the pot
        ShowdownStats s = new ShowdownStats(players, communityCards);
        pot.handOutPot(players, communityCards, s);

        assertTrue(pot.getPotSize() == 0);
        assertEquals(players.get(0).getStackSize(), 19000);
        assertEquals(players.get(1).getStackSize(), 6000);
        assertEquals(players.get(2).getStackSize(), 0);
        assertEquals(players.get(3).getStackSize(), 3000);
    }

    @Test
    public void testNormalCaseWhereNoPlayerIsAllIn() {
        pot.addToPot(0, 5000);
        pot.addToPot(1, 5000);
        pot.addToPot(2, 0);
        pot.addToPot(3, 5000);
        pot.addToPot(4, 5000);

        //Set up so that player 1 wins the main pot
        players.get(0).setHoleCards(Card.of(12, Card.Suit.SPADES).get(), Card.of(13, Card.Suit.CLUBS).get());
        players.get(1).setHoleCards(Card.of(7, Card.Suit.CLUBS).get(), Card.of(8, Card.Suit.SPADES).get());
        players.get(2).setHoleCards(Card.of(7, Card.Suit.HEARTS).get(), Card.of(8, Card.Suit.DIAMONDS).get());
        players.get(3).setHoleCards(Card.of(2, Card.Suit.HEARTS).get(), Card.of(3, Card.Suit.DIAMONDS).get());
        players.get(4).setHoleCards(Card.of(3, Card.Suit.SPADES).get(), Card.of(4, Card.Suit.CLUBS).get());

        //Add community cards
        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(9, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(10, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(2, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(4, Card.Suit.SPADES).get());

        //Player 2 folded preflop
        players.remove(2);

        //Hand out the pot
        ShowdownStats s = new ShowdownStats(players, communityCards);
        pot.handOutPot(players, communityCards, s);

        assertTrue(pot.getPotSize() == 0);
        assertEquals(players.get(0).getStackSize(), 20000);
        assertEquals(players.get(1).getStackSize(), 0);
        assertEquals(players.get(2).getStackSize(), 0);
        assertEquals(players.get(3).getStackSize(), 0);
    }

    @Test
    public void testThatPotIsEmptyIfAllPlayersGetTheirShare() {
        pot.addToPot(0, 5000);
        pot.addToPot(1, 6500);
        pot.addToPot(2, 400);
        pot.addToPot(3, 5000);
        pot.addToPot(4, 10000);

        //Assuming that player 1 won the hand, player 2 came second and so on
        for (Player p : players) {
            p.incrementStack(pot.getSharePlayerCanWin(p.getID()));
            assertEquals(0, pot.getSharePlayerCanWin(p.getID()));
        }

        assertEquals(0, pot.getPotSize());

        assertEquals(players.get(0).getStackSize(), 20400);
        assertEquals(players.get(1).getStackSize(), 3000);
        assertEquals(players.get(2).getStackSize(), 0);
        assertEquals(players.get(3).getStackSize(), 0);
        assertEquals(players.get(4).getStackSize(), 3500);
    }

    @Test
    public void testThatSumOfPotIsAlwaysZeroAfterAllPlayersGetTheirShare() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            pot.addToPot(0, rand.nextInt(10000));
            pot.addToPot(1, rand.nextInt(10000));
            pot.addToPot(2, rand.nextInt(10000));
            pot.addToPot(3, rand.nextInt(10000));
            pot.addToPot(4, rand.nextInt(10000));

            //Assuming that player 1 won the hand, player 2 came second and so on
            for (int j = 0; j < 100; j++) {
                for (Player p : players) {
                    p.incrementStack(pot.getSharePlayerCanWin(p.getID()));
                    assertEquals(0, pot.getSharePlayerCanWin(p.getID()));
                }
            }

            assertEquals(0, pot.getPotSize());

        }
    }

    @Test
    public void testThatASidePotOfZeroIsNotAdded() {
        //Player 2 will win the main pot of 19825
        //Player 4 will take a side pot of 150
        //Player 0 will take a side pot of 2525
        //The stats should not print that player 3 wins a side pot of 0

        pot.addToPot(0, 7525);
        pot.addToPot(1, 25);
        pot.addToPot(2, 4950);
        pot.addToPot(3, 5000);
        pot.addToPot(4, 5000);

        players.get(0).setHoleCards(Card.of(4, Card.Suit.DIAMONDS).get(), Card.of(10, Card.Suit.HEARTS).get());
        players.get(1).setHoleCards(Card.of(2, Card.Suit.CLUBS).get(), Card.of(8, Card.Suit.SPADES).get());
        players.get(2).setHoleCards(Card.of(10, Card.Suit.CLUBS).get(), Card.of(7, Card.Suit.CLUBS).get());
        players.get(3).setHoleCards(Card.of(13, Card.Suit.SPADES).get(), Card.of(7, Card.Suit.DIAMONDS).get());
        players.get(4).setHoleCards(Card.of(13, Card.Suit.CLUBS).get(), Card.of(14, Card.Suit.DIAMONDS).get());

        //Player 1 folded
        players.remove(1);

        ArrayList<Card> communityCards = new ArrayList<>();
        communityCards.add(Card.of(9, Card.Suit.CLUBS).get());
        communityCards.add(Card.of(13, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(8, Card.Suit.DIAMONDS).get());
        communityCards.add(Card.of(11, Card.Suit.HEARTS).get());
        communityCards.add(Card.of(3, Card.Suit.CLUBS).get());

        ShowdownStats stats = new ShowdownStats(players, communityCards);
        pot.handOutPot(players, communityCards, stats);

        assertFalse(stats.getWinnerText().contains(players.get(2).getName()));
        assertTrue(stats.getWinnerText().contains(players.get(0).getName()));
        assertTrue(stats.getWinnerText().contains(players.get(1).getName()));
        assertTrue(stats.getWinnerText().contains(players.get(3).getName()));
    }

}