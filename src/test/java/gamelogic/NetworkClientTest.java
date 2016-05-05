package gamelogic;

import gamelogic.ai.AITest;
import gui.GameScreen;
import gui.GameSettings;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by morten on 27.04.16.
 */
public class NetworkClientTest {

    final long timeToThink = 500L;

    //@Test
    public void playFlopOverNetwork() throws IOException {

        int amountOfPlayers = 4;
        Deck deck = new Deck();
        ServerSocket socketListener = new ServerSocket(39100);
        List<GameClient> players = new ArrayList<>();

        Runnable serverThread = () -> {
            for (int i = 0; i < amountOfPlayers; i++) {
                try {

                    Socket serverSocket = socketListener.accept();

                    GameClient gameClient = new NetworkClient(serverSocket, i);
                    players.add(gameClient);
                    gameClient.setHandForClient(i, deck.draw().get(), deck.draw().get());

                    AITest.setupAi(gameClient, amountOfPlayers, 2);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Connected to all " + amountOfPlayers + " clients.");

        };
        new Thread(serverThread).start();

        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < amountOfPlayers; i++) {
            int i2 = i;
            Runnable r = () -> {
                try {
                    Socket clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
                    BufferedReader socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    ServerGameCommunicator communicator = new ServerGameCommunicator(socketOutput, socketInput, "Morten-" + i2);
                    communicator.startUpi();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            new Thread(r).start();
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < amountOfPlayers; i++) {
            System.out.println(players.get(i).getDecision(timeToThink));

            for (int j = 0; j < amountOfPlayers; j++) {
                players.get(j).playerMadeDecision(i, new Decision(Decision.Move.CALL));
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int j = 0; j < amountOfPlayers; j++) {
            players.get(j).setFlop(deck.draw().get(), deck.draw().get(), deck.draw().get());
        }

        for (int i = 0; i < amountOfPlayers; i++) {
            int playerId = (i + 2) % amountOfPlayers;
            System.out.println("Asking player " + playerId + " for a decision");
            System.out.println(players.get(playerId).getDecision(timeToThink));

            for (int j = 0; j < amountOfPlayers; j++) {
                players.get(j).playerMadeDecision(playerId, new Decision(Decision.Move.CHECK));
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Close all the sockets
        for (GameClient client : players) {
            ((NetworkClient)client).closeSocket();
        }
    }

    @Test
    public void testMapToString() throws Exception {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "Morten");
        map.put(1, "Kristian");
        assertEquals("0 Morten 1 Kristian", NetworkClient.mapToString(map).trim());
    }
}