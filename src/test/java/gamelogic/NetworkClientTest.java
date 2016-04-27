package gamelogic;

import gamelogic.ai.AITest;
import gui.GameScreen;
import gui.GameSettings;
import org.junit.Test;

import java.io.IOException;
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

    @Test
    public void stuffWorks() throws IOException {

        int amountOfPlayers = 4;
        Deck deck = new Deck();
        ServerSocket socketListener = new ServerSocket(39100);
        List<GameClient> players = new ArrayList<>();

        Runnable serverThread = () -> {

            try {

                for (int i = 0; i < 1; i++) {
                    System.out.println("Listening on port 39100");
                    Socket serverSocket = socketListener.accept();
                    System.out.println("Got connected");

                    GameClient gameClient = new NetworkClient(serverSocket, i);
                    players.add(gameClient);
                    gameClient.setHandForClient(i, deck.draw().get(), deck.draw().get());
                    AITest.setupAi(gameClient, amountOfPlayers, 2);
                }
                for (GameClient client : players) {
                    System.out.println(client.getDecision(1000L));
                    for (int i = 0; i < amountOfPlayers; i++) {
                        //players.get(i).playerMadeDecision();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(serverThread).start();

        System.out.println("Opening socket");
        Socket clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
        System.out.println("Opened socket");
        //GameScreen gameScreen = new GameScreen(0);
        //gameScreen.createSceneForGameScreen(new GameSettings(1000L, 50L, 25L, 2, 10, AIType.MCTS_AI));
        ServerGameCommunicator communicator = new ServerGameCommunicator(clientSocket, "Morten", null);

        
        //communicator.startUpi();


    }

    @Test
    public void testMapToString() throws Exception {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "Morten");
        map.put(1, "Kristian");
        assertEquals("0 Morten 1 Kristian", NetworkClient.mapToString(map).trim());
    }
}