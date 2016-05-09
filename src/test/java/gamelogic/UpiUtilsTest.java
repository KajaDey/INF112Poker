package gamelogic;

import gui.GameSettings;
import network.Server;
import network.UpiUtils;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by morten on 07.05.16.
 */
public class UpiUtilsTest {

    @Test
    public void tokenizeQuote() throws Exception {
        assertArrayEquals(new String[]{"playerName", "Morten Lohne"}, UpiUtils.tokenize("playerName   \"Morten Lohne\"").get());
    }

    @Test
    public void tokenizeNumbers() {
        assertArrayEquals(new String[]{"yourId", "0"}, UpiUtils.tokenize("yourId 0").get());
    }

    @Test
    public void tokenizeSeveralQuotes() {
        assertArrayEquals(new String[]{"player 0 got 1000", "player 1 got 500", "player 2 got 2000"},
                UpiUtils.tokenize(" \"player 0 got 1000\" \"player 1 got 500\" \"player 2 got 2000\"").get());
    }

    @Test
    public void tokenizeSpaces() {
        assertArrayEquals(new String[]{"playerNames", "3", "m"}, UpiUtils.tokenize("playerNames 3  m ").get());
    }

    @Test
    public void testSettingsToString() {
        GameSettings settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
        assertEquals("maxNumberOfPlayers 6 startStack 5000 smallBlind 25 bigBlind 50 levelDuration 10",UpiUtils.settingsToString(settings));
        settings.setMaxNumberOfPlayers(3);
        settings.setStartStack(3000);
        settings.setBigBlind(100);
        assertEquals("maxNumberOfPlayers 3 startStack 3000 smallBlind 25 bigBlind 100 levelDuration 10",UpiUtils.settingsToString(settings));
    }

    @Test
    public void testParsingStatisticsObjectBackAndForth() {
        NameGenerator.readNewSeries();
        HashMap<Integer, Integer> rankingTable = new HashMap<>();
        HashMap<Integer, String> names = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            rankingTable.put(i, i + 1);
            names.put(i, NameGenerator.getRandomName());
        }

        Statistics stats = new Statistics(0, rankingTable, names, 10, 20, 5, 30, 15, "Royal Straight Flush");
        String upiString = stats.toUPIString();

        Statistics newStats = null;
        try {
            newStats = UpiUtils.upiStringToStatistics(upiString);
        } catch (Server.PokerProtocolException e) {
            e.printStackTrace();
        }

        assertTrue(newStats != null);
        assertTrue(stats.equals(newStats));
    }

}