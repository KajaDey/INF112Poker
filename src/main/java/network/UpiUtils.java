package network;

import gamelogic.Card;
import gui.GameSettings;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Various utility methods for converting between objects and upi-compatible strings representations
 */
public class UpiUtils {

    /**
     * Converts a list of cards into a upi-compatible string
     */
    public static String cardsToString(Card... cards) {
        return Arrays.stream(cards)
                .map(card -> card.suit.name().toLowerCase() + card.rank + " ")
                .reduce("", String::concat);
    }

    /**
     * Converts a map into a upi-compatible string
     */
    public static <K, V> String mapToString(Map<K, V> map) {
        return map.keySet().stream()
                .map(key -> key.toString() + " " + map.get(key).toString() + " ")
                .reduce("", String::concat);
    }

    /**
     * Convert settings of this table to a string matching the Lobby Protocol
     * @return <<setting1, value1> <setting2, value2> ... >
     */
    public static String settingsToString(GameSettings settings) {
        return String.format("maxNumberOfPlayers %d startStack %d smallBlind %d bigBlind %d levelDuration %d",
                settings.getMaxNumberOfPlayers(), settings.getStartStack(), settings.getSmallBlind(), settings.getBigBlind(),
                settings.getLevelDuration()).trim();
    }

    /**
     * Parses a upi input line into its tokens, as specified in the protocol
     * @param input A single line of input
     * @return None if the string contained newlines or unclosed quotes, the tokens otherwise
     */
    public static Optional<String[]> tokenize(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.of(new String[0]);
        }
        List<String> tokens = new ArrayList<>();

        int tokenStartIndex = 0;
        boolean parsingQuote = false;
        int i = 0;
        while (i < input.length()) {
            for (i = tokenStartIndex; i < input.length(); i++) {
                if (input.charAt(i) == '\n') {
                    return Optional.empty();
                }
                if (input.charAt(i) == '\"') {
                    tokenStartIndex = i + 1;
                    parsingQuote = true;
                    break;
                }
                else if (!Character.isWhitespace(input.charAt(i))) {
                    tokenStartIndex = i;
                    parsingQuote = false;
                    break;
                }
            }

            for (i = tokenStartIndex; i < input.length(); i++) {
                if (input.charAt(i) == '\n') {
                    return Optional.empty();
                }
                if (parsingQuote) {
                    if (input.charAt(i) == '\"') {
                        tokens.add(input.substring(tokenStartIndex, i));
                        parsingQuote = false;
                        tokenStartIndex = i + 1;
                        break;
                    }
                } else {
                    if (Character.isWhitespace(input.charAt(i))) {
                        tokens.add(input.substring(tokenStartIndex, i));
                        tokenStartIndex = i + 1;
                        break;
                    }
                }
            }
        }
        if (parsingQuote) {
            return Optional.empty();
        }
        else if (tokenStartIndex < input.length() && !Character.isWhitespace(input.charAt(tokenStartIndex)) && input.charAt(tokenStartIndex) != '\"') {
            tokens.add(input.substring(tokenStartIndex));
        }
        tokens = tokens.stream().map(String::trim)
                .filter(s -> s.length() > 0)
                .filter(s -> s.length() > 1 || Character.isLetterOrDigit(s.charAt(0)))
                .collect(Collectors.toList());
        return Optional.of(tokens.toArray(new String[tokens.size()]));

    }
}
