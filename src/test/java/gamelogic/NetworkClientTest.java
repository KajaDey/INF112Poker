package gamelogic;

import gamelogic.ai.AITest;
import gamelogic.ai.MCTSAI;
import gui.GameSettings;
import gui.LobbyScreen;
import javafx.application.Platform;
import network.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import org.powermock.reflect.Whitebox;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by morten on 27.04.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerGameCommunicator.class, Platform.class })

public class NetworkClientTest {

    final long timeToThink = 500L;
    int amountOfPlayers;
    Deck deck;
    ServerSocket socketListener;
    List<GameClient> players;
    ServerLobbyCommunicator lobbyCommunicator;
    ServerGameCommunicator gameCommunicator;
    public static Stack<String> inputStrings;

    @Test
    public void playFlopOverNetwork() throws Exception {
        setupPlayFlopOverNetworkTest();

        for (int i = 0; i < amountOfPlayers; i++) {
            setDecisionForClient(Decision.Move.CALL, i);
        }

        setFlop();

        for (int i = 0; i < amountOfPlayers; i++) {
            setDecisionForClient(Decision.Move.CHECK, (i + 2) % amountOfPlayers);
        }

        // Close all the sockets
        for (GameClient client : players) {
            ((NetworkClient)client).closeSocket();
        }

    }

    @Test
    public void testServerLobbyCommunicator() throws Exception {
        setupServerLobbyTest();

        lobbyCommunicator.start();

        Thread commThread = Whitebox.getInternalState(lobbyCommunicator, "listeningThread");
        commThread.join();
    }

    @Test
    public void testMapToString() throws Exception {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "Morten");
        map.put(1, "Kristian");
        assertEquals("0 Morten 1 Kristian", UpiUtils.mapToString(map).trim());
    }

    private void delay(long delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setFlop() {
        for (int j = 0; j < amountOfPlayers; j++) {
            players.get(j).setFlop(deck.draw().get(), deck.draw().get(), deck.draw().get());
        }
    }

    /**
     * Connects given number of ai players to ServerSocket
     */
    private void connectClients() {
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
    }

    private void setDecisionForClient(Decision.Move decision, int playerID) {
        System.out.println("Asking player " + playerID + " for a decision");
        System.out.println(players.get(playerID).getDecision(timeToThink));

        for (int j = 0; j < amountOfPlayers; j++) {
            players.get(j).playerMadeDecision(playerID, new Decision(decision));
        }
        delay(200L);
    }

    private void createGameCommunicatorForClients() {
        for (int i = 0; i < amountOfPlayers; i++) {
            int i2 = i;
            Runnable r = () -> {
                try {
                    Socket clientSocket = new Socket(InetAddress.getLocalHost(), 39100);
                    BufferedReader socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    createServerGameCommunicatorSpy(socketOutput, socketInput, "Morten-" + i2);
                    gameCommunicator.startUpi();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };
            new Thread(r).start();

            delay(100L);
        }
    }

    private void setupServerLobbyTest() throws Exception {
        Server server = new Server();

        inputStrings = new Stack<>();
        inputStrings.addAll(Arrays.asList("startGame", "playerJoinedTable 0 0",
                "tableSettings 0 smallBlind 25 bigBlind 100 maxNumberOfPlayers 2",
                "tableCreated 0", "playerJoinedLobby 0 Ragnhild", "lobbySent", "lobbyok"));

        createServerLobbyCommunicatorSpy();
    }

    private void setupPlayFlopOverNetworkTest() throws Exception {
        Server server = new Server();
        amountOfPlayers = 4;
        deck = new Deck();
        socketListener = server.serverSocket;
        players = new ArrayList<>();

        connectClients();

        delay(200L);

        createGameCommunicatorForClients();
    }

    private void createServerLobbyCommunicatorSpy() throws Exception{
        try {
            lobbyCommunicator = spy(new ServerLobbyCommunicator("Ragnhild", mock(LobbyScreen.class), InetAddress.getLocalHost()));

            doNothing().when(lobbyCommunicator, "goToGameScreen");

            doAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    if (inputStrings.isEmpty()) System.exit(0);

                    return NetworkClientTest.inputStrings.pop();
                }
            }).when(lobbyCommunicator, "readFromServer");

            mockStatic(Platform.class);
            doNothing().when(Platform.class, "runLater", any(Runnable.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createServerGameCommunicatorSpy(BufferedWriter socketOutput, BufferedReader socketInput, String playerName) {
        gameCommunicator = spy(new ServerGameCommunicator(socketOutput, socketInput, playerName));

        try {
            doAnswer((Answer<Optional<GameClient>>) arg -> {
                int userID = ((int)arg.getArguments()[1]);
                return Optional.of(new MCTSAI(userID, 1.0));
            }).when(gameCommunicator, "createGUIClient", any(GameSettings.class), anyInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}