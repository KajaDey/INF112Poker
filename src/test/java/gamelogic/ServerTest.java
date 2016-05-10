package gamelogic;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by morten on 27.04.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerGameCommunicator.class, Platform.class})

public class ServerTest {

    ServerLobbyCommunicator lobbyCommunicator;
    public static Stack<String> inputStrings;
    ArrayList<Socket> clientSockets;
    ArrayList<BufferedReader> readers;
    ArrayList<BufferedWriter> writers;
    InetSocketAddress socketAddress;
    Stack<String> names;

    @Test
    public void testHandshakeWithServer() throws Exception {
        connectClientsToServer(4);

        assertClientsNotifiedWhenNewPlayerJoined(4);
    }

    @Test
    public void testQuit() throws Exception {
        connectClientsToServer(1);
        ignoreAnswersWhenPlayersJoined(1);

        writeToSocket(0, "quit");

        assertEquals("playerLeftLobby 0", readFromSocket(1)); // The other player gets notified
    }

    @Test
    public void testTakeSeat() throws Exception {
        connectClientsToServer(2);
        ignoreAnswersWhenPlayersJoined(2);

        // Tries to take seat when table doesnt exist
        writeToSocket(1, "takeSeat 0");
        String [] answer = UpiUtils.tokenize(readFromSocket(1)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("Table 0 does not exist", answer[1]);

        // Creates table, other player takes seat
        writeToSocket(0, "createTable");
        ignoreAnswers(0, 3);
        ignoreAnswers(1, 3);

        writeToSocket(0, "takeSeat 0");
        answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("You are already seated at a table", answer[1]);

        writeToSocket(1, "takeSeat 0");
        assertEquals("playerJoinedTable 1 0", readFromSocket(1));
    }

    @Test
    public void testLeaveSeat() throws Exception {
        connectClientsToServer(1);
        ignoreAnswersWhenPlayersJoined(1);

        // Attempting to leave seat when not seated
        writeToSocket(0, "leaveSeat 0");

        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("Table 0 does not exist", answer[1]);

        // Creates table and then leaves
        writeToSocket(0, "createTable");
        assertWhenTableCreatedCorrectAnswersReceived();

        writeToSocket(0, "leaveSeat 0");
        assertEquals("playerLeftTable 0 0", readFromSocket(0));
    }

    @Test
    public void testCreateTable() throws Exception {
        connectClientsToServer(1);
        ignoreAnswersWhenPlayersJoined(1);

        writeToSocket(0, "createTable");
        assertWhenTableCreatedCorrectAnswersReceived();

        writeToSocket(0, "createTable"); // Tries to create second table
        assertErrorMessageWhenAlreadySeatedAtATable();
    }

    @Test
    public void testChangeSettings() throws Exception {
        connectClientsToServer(1);
        ignoreAnswersWhenPlayersJoined(1);

        writeToSocket(0, "createTable");
        ignoreAnswers(0, 3);

        // Tries to change settings on nonexisting table
        GameSettings sett = new GameSettings(GameSettings.DEFAULT_SETTINGS);
        sett.setLevelDuration(100);

        writeToSocket(0, "changeSettings 0 \""+ UpiUtils.settingsToString(sett) +"\"");

        String []answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("tableSettings", answer[0]);
        assertNotEquals(UpiUtils.settingsToString(GameSettings.DEFAULT_SETTINGS), answer[2]);
        assertEquals(UpiUtils.settingsToString(sett), answer[2]);
    }

    @Test
    public void testDeleteTable() throws Exception {
        connectClientsToServer(2);
        ignoreAnswersWhenPlayersJoined(2);

        writeToSocket(0, "createTable");
        ignoreAnswers(0, 3);
        ignoreAnswers(1, 3);

        writeToSocket(0, "deleteTable 1");
        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("There is no table with id 1", answer[1]);

        writeToSocket(1, "deleteTable 0");
        answer = UpiUtils.tokenize(readFromSocket(1)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("You are not the host of this table", answer[1]);

        writeToSocket(0, "deleteTable 0");

        assertEquals("playerLeftTable 0 0", readFromSocket(0));
        assertEquals("tableDeleted 0", readFromSocket(0));

    }

    @Test
    public void testCannotJoinFullTable() throws Exception {
        connectClientsToServer(2);
        ignoreAnswersWhenPlayersJoined(2);

        GameSettings sett = GameSettings.DEFAULT_SETTINGS;
        sett.setMaxNumberOfPlayers(1);

        writeToSocket(0, "createTable \""+ UpiUtils.settingsToString(sett) +"\"");
        ignoreAnswers(0, 3);
        ignoreAnswers(1, 3);

        writeToSocket(1, "takeSeat 0");
        String [] answer = UpiUtils.tokenize(readFromSocket(1)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("This table is full", answer[1]);
    }

    @Test
    public void testUnknownCommandSent() throws Exception {
        connectClientsToServer(2);
        ignoreAnswersWhenPlayersJoined(2);

        writeToSocket(0, "Something illegal");

        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("Unknown command", answer[1]);
    }

    private void assertErrorMessageWhenAlreadySeatedAtATable() throws IOException {
        String []answer = UpiUtils.tokenize(readFromSocket(0)).get();

        assertEquals("errorMessage", answer[0]);
        assertEquals("You are already seated at a table", answer[1]);

        writeToSocket(0, "takeSeat 0");

        answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("errorMessage", answer[0]);
        assertEquals("You are already seated at a table", answer[1]);
    }

    private void assertWhenTableCreatedCorrectAnswersReceived() throws IOException {
        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();

        assertEquals("tableCreated", answer[0]);
        assertEquals("0", answer[1]);

        assertEquals("tableSettings 0 maxNumberOfPlayers 6 startStack 5000 smallBlind 25 bigBlind 50 levelDuration 10 playerClock 30", readFromSocket(0));

        assertEquals("playerJoinedTable 0 0", readFromSocket(0));
    }

    private void assertClientsNotifiedWhenNewPlayerJoined(int numClients) throws IOException {
        for (int i = 0; i < numClients; i++) {
            for (int j = i; j < numClients; j++) {
                assertTrue(readFromSocket(i).startsWith("playerJoinedLobby "+ j));
            }
        }
    }

    /**
     * Ignores a given number of answers to given client. Answers are irrelevant for this test, therefore ignored.
     */
    private void ignoreAnswers(int clientID, int numToIgnore) throws Exception {
        for (int i = 0; i < numToIgnore; i++)
            readFromSocket(clientID);
    }

    private void ignoreAnswersWhenPlayersJoined(int numClients) throws Exception {
        for (int i = 0; i < numClients; i++) {
            for (int j = i; j < numClients; j++) {
                readFromSocket(i);
            }
        }
    }

    private void connectClientsToServer(int numClients) throws Exception {
        socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 39100);
        clientSockets = new ArrayList<>();
        readers = new ArrayList<>();
        writers = new ArrayList<>();
        names = new Stack();
        names.addAll(Arrays.asList("Kaja", "Simon", "Vegar", "Ragnhild", "Kristian", "Morten", "Mariah"));

        new Server();
        connectClients(numClients);
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
            assert answer.equals("yourId "+ i) : "Got message "+ answer +", expected yourId "+ i;

            answer = readFromSocket(i);
            assert answer.startsWith("playerNames") : "Got message "+ answer +", expected playerNames";

            answer = readFromSocket(i);
            assert answer.equals("lobbySent") : "Got message "+ answer + ", expected lobbySent";

            Thread.sleep(100L); // To avoid race conditions
        }
    }

    /**
     * Reads input from socket and returns it
     */
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
        new Server();

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

                    return ServerTest.inputStrings.pop();
                }
            }).when(lobbyCommunicator, "readFromServer");

            mockStatic(Platform.class);
            doNothing().when(Platform.class, "runLater", any(Runnable.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}