package gamelogic;

import gui.GameLobby;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Kristian Rosland on 27.04.2016.
 */
public class ServerLobbyCommunicator {

    Socket clientSocket;

    public ServerLobbyCommunicator(String name) {
        Server.main(null);

        //Handshake
        try {
            clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        GameLobby lobby = new GameLobby();
        lobby.createMultiPlayerLobbyScreen();
    }
}
