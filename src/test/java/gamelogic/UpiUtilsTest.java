package gamelogic;

import network.UpiUtils;
import org.junit.Test;

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

}