package gui;

import gamelogic.Logger;
import network.Server;
import network.UpiUtils;

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
    private int hostID;

    public LobbyTable(int id, Logger logger) {
        this.id = id;
        this.logger = logger;
        settings = new GameSettings(GameSettings.DEFAULT_SETTINGS);
    }

    public void parseSetting(String name, String value) {
        try {
            UpiUtils.parseSetting(this.settings, name, value);
        } catch (Server.PokerProtocolException e) {
            logger.println("Could not parse setting, " + name + " " + value, Logger.MessageType.NETWORK, Logger.MessageType.GAMEPLAY);
        }
    }

    public void addPlayer(int id) {
        if (playerIds.isEmpty())
            hostID = id;
        playerIds.add(id);
    }

    public void removePlayer(Integer playerID) {
        playerIds.remove(playerID);

        assert !playerIds.contains(playerID) : "Removed p.id " + playerID + " from table " + this.id + ", but he wasn't removed";
    }

    public boolean isSeated(int playerID) {
        return playerIds.contains(playerID);
    }

    public int getHost() {return hostID;}
}