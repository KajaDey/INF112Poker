package gamelogic.network;

import gui.GameSettings;
import network.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by morten on 27.04.16.
 */
public class ServerTest {

    private ArrayList<Socket> clientSockets;
    private ArrayList<BufferedReader> readers;
    private ArrayList<BufferedWriter> writers;
    private InetSocketAddress socketAddress;
    private Stack<String> names;

    @Test
    public void testHandshakeWithServer() throws Exception {
        connectClientsToServer(2, 49100);
        assertClientsNotifiedWhenNewPlayerJoined(2);
    }

    @Test
    public void testQuit() throws Exception {
        connectClientsToServer(2, 39200);
        ignoreAnswersWhenPlayersJoined(2);

        writeToSocket(0, "quit");

        assertEquals("playerLeftLobby 0", readFromSocket(1)); // The other player gets notified
    }

    @Test
    public void testTakeSeat() throws Exception {
        connectClientsToServer(2, 39300);
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
        connectClientsToServer(1, 39400);
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
        connectClientsToServer(1, 39500);
        ignoreAnswersWhenPlayersJoined(1);

        writeToSocket(0, "createTable");
        assertWhenTableCreatedCorrectAnswersReceived();

        writeToSocket(0, "createTable"); // Tries to create second table
        assertErrorMessageWhenAlreadySeatedAtATable();
    }

    @Test
    public void testChangeSettings() throws Exception {
        connectClientsToServer(1, 39600);
        ignoreAnswersWhenPlayersJoined(1);

        writeToSocket(0, "createTable");
        ignoreAnswers(0, 3);

        // Tries to change settings on nonexisting table
        GameSettings sett = new GameSettings();
        sett.setLevelDuration(100);

        writeToSocket(0, "changeSettings 0 \""+ UpiUtils.settingsToString(sett) +"\"");

        String []answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("tableSettings", answer[0]);
        assertNotEquals(UpiUtils.settingsToString(new GameSettings()), answer[2]);
        assertEquals(UpiUtils.settingsToString(sett), answer[2]);
    }

    @Test
    public void testDeleteTable() throws Exception {
        connectClientsToServer(2, 39700);
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
    public void testCannotJoinAlreadyFullTable() throws Exception {
        connectClientsToServer(2, 39800);
        ignoreAnswersWhenPlayersJoined(2);

        GameSettings sett = new GameSettings();
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
        connectClientsToServer(2, 39900);
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

        GameSettings gameSettings = new GameSettings();

        assertEquals("tableCreated", answer[0]);
        assertEquals("0", answer[1]);
        assertEquals("tableSettings 0 \""+ UpiUtils.settingsToString(gameSettings) +"\"", readFromSocket(0));
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

    private void connectClientsToServer(int numClients, int port) throws Exception {
        socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), port);
        clientSockets = new ArrayList<>();
        readers = new ArrayList<>();
        writers = new ArrayList<>();
        names = new Stack();
        names.addAll(Arrays.asList("Kaja", "Simon", "Vegar", "Ragnhild", "Kristian", "Morten", "Mariah"));

        new Server(port);
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

            assertEquals("lobbyok", readFromSocket(i));

            assertEquals("yourId "+ i, readFromSocket(i));

            String[] tokens = UpiUtils.tokenize(readFromSocket(i)).get();
            assertEquals("playerNames", tokens[0]);

            assertEquals("lobbySent", readFromSocket(i));

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

}