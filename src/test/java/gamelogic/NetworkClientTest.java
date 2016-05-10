package gamelogic;

import gamelogic.ai.AITest;
import gamelogic.ai.MCTSAI;
import gui.GUIMain;
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by morten on 27.04.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerGameCommunicator.class, Platform.class})

public class NetworkClientTest {

    final long timeToThink = 500L;
    int amountOfPlayers;
    Deck deck;
    ServerSocket socketListener;
    List<GameClient> players;
    ServerLobbyCommunicator lobbyCommunicator;
    ServerGameCommunicator gameCommunicator;
    public static Stack<String> inputStrings;
    ArrayList<Socket> clientSockets;
    ArrayList<BufferedReader> readers;
    ArrayList<BufferedWriter> writers;
    InetSocketAddress socketAddress;
    Stack<String> names;

    @Test
    public void testHandshakeWithServer() throws Exception {
        setupServerTestFourClients();

        new Server();

        connectClients(4);
    }

    @Test
    public void testPlayerMadeTable() throws Exception {
        setupServerTestFourClients();

        new Server();

        connectClients(4);

        assertClientsNotifiedWhenNewPlayerJoined(4);

        writeToSocket(0, "createTable");

        String answer = readFromSocket(0);
        assert answer.equals("tableCreated 0") : "Got message "+ answer +", expected tableCreated 0";

        // createtable --> tableCreated, settings, playerjoined
        // other players takeseat
    }

    private void assertClientsNotifiedWhenNewPlayerJoined(int numClients) throws IOException {
        for (int i = 0; i < numClients; i++) {
            for (int j = i; j < numClients; j++) {
                String answer = readFromSocket(i);
                assert answer.startsWith("playerJoinedLobby " + j) : "Got message "+ answer + ", expected playerJoinedLobby "+ j;
            }
        }
    }

    private void setupServerTestFourClients() throws IOException {
        socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 39100);
        clientSockets = new ArrayList<>();
        readers = new ArrayList<>();
        writers = new ArrayList<>();
        names = new Stack();
        names.addAll(Arrays.asList("Ragnhild", "Kristian", "Morten", "Mariah"));
    }

    /**
     * Connects given number of client sockets to server socket, and makes handshake
     * @param numClients
     */
    private void connectClients(int numClients) throws Exception {
        for (int i = 0; i < numClients; i++) {
            Socket clientSocket = new Socket();
            clientSocket.connect(socketAddress, 1000);

            clientSockets.add(clientSocket);
            readers.add(new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8")));
            writers.add(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8")));

            String name = names.pop();
            writeToSocket(i, "lobby "+ name);

            String answer = readFromSocket(i);
            assert answer.equals("lobbyok") : "Got handshake "+ answer +", expected lobbyok";

            answer = readFromSocket(i);
            assert answer.equals("yourId "+ i) : "Got handshake "+ answer +", expected yourId "+ i;

            answer = readFromSocket(i);
            assert answer.startsWith("playerNames") : "Got handshake "+ answer +", expected playerNames";

            answer = readFromSocket(i);
            assert answer.equals("lobbySent") : "Got handshake "+ answer + ", expected lobbySent";

            Thread.sleep(100L); // To avoid race conditions
        }
    }

    private String readFromSocket(int clientID) throws IOException {
        String input = readers.get(clientID).readLine();
        return input.trim();
    }

    /**
     * Write to socket (adds new line)
     * @param output Message to write
     */
    private void writeToSocket(int clientID, String output) {
        try {
            writers.get(clientID).write(output + "\n");
            writers.get(clientID).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServerLobbyCommunicator() throws Exception {
        setupServerLobbyTest();

        lobbyCommunicator.start();

        Thread commThread = lobbyCommunicator.getListeningThread();
        commThread.join();
    }

    private void setupServerLobbyTest() throws Exception {
        Server server = new Server();

        inputStrings = new Stack<>();
        inputStrings.addAll(Arrays.asList("startGame", "playerJoinedTable 0 0",
                "tableSettings 0 smallBlind 25 bigBlind 100 maxNumberOfPlayers 2",
                "tableCreated 0", "playerJoinedLobby 0 Ragnhild", "lobbySent", "lobbyok"));

        createServerLobbyCommunicatorSpy();
    }

    private void createServerLobbyCommunicatorSpy() throws Exception {
        try {
            lobbyCommunicator = spy(new ServerLobbyCommunicator("Ragnhild", mock(LobbyScreen.class), InetAddress.getLocalHost(), GUIMain.guiMain.logger));

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
        gameCommunicator = spy(new ServerGameCommunicator(socketOutput, socketInput, playerName, GUIMain.guiMain.logger));

        try {
            doAnswer((Answer<Optional<GameClient>>) arg -> {
                int userID = ((int) arg.getArguments()[1]);
                return Optional.of(new MCTSAI(userID, GUIMain.guiMain.logger));
            }).when(gameCommunicator, "createGUIClient", any(GameSettings.class), anyInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}