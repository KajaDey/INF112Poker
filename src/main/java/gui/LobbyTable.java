package gui;

import gamelogic.Logger;

import java.util.ArrayList;

/**
 * This class represents a table in the lobby (used by LobbyScreen)
 *
 * @author Kristian Rosland
 */
public class LobbyTable {
    final int id;
    final GameSettings settings;
    final ArrayList<Integer> playerIds = new ArrayList<>();
    private final Logger logger;

    public LobbyTable(int id, Logger logger) {
        this.id = id;
        this.logger = logger;
        settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
    }

    public void parseSetting(String name, String value) {
        logger.println("Setting " + name + " to " + value + " for table " + id, Logger.MessageType.GAMEPLAY);
        switch (name) {
            case "smallBlind":
                settings.setSmallBlind(Long.parseLong(value));
                break;
            case "bigBlind":
                settings.setBigBlind(Long.parseLong(value));
                break;
            case "maxNumberOfPlayers":
                settings.setMaxNumberOfPlayers(Integer.parseInt(value));
                break;
            case "startStack":
                settings.setStartStack(Long.parseLong(value));
                break;
            case "levelDuration":
                settings.setLevelDuration(Integer.parseInt(value));
                break;
            default:
                System.out.println("Received unknown table setting " + name + ", ignoring...");
        }
    }

    public void addPlayer(int id) {
        playerIds.add(id);
    }

    public void removePlayer(Integer playerID) {
        playerIds.remove(playerID);

        assert !playerIds.contains(playerID) : "Removed p.id " + playerID + "from table " + this.id + ", but he wasn't removed";
    }
}