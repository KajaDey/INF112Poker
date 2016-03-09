package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.gamelogic.Card;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    /*public static void main(String[] args) {
        launch(args);
    }*/

    public static void run(String[] args){
        launch(args);
    }

    public void start(Stage window) throws Exception{
        GUIClient kake = new GUIClient(0);
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable", kake), "Main Screen");

        Map<Integer,Long> map = new HashMap<>();
        Map<Integer,Integer> map2 = new HashMap<>();
        Map<Integer,String> map3 = new HashMap<>();

        Card card1 = Card.of(5, Card.Suit.DIAMONDS).get();
        Card card2 = Card.of(10, Card.Suit.CLUBS).get();

        map.put(0, 0L);
        map.put(1, 0L);

        map2.put(0,0);
        map2.put(1,2);

        map3.put(0,"Kake");
        map3.put(1,"Katt");

        kake.setBigBlind(50);
        kake.setSmallBlind(25);
        //kake.setPot(0);
        kake.setStackSizes(map);
        kake.setHoleCards(card1, card2);
        kake.setPositions(map2);
        kake.setName(map3);
        kake.setAmountOfPlayers(2);
        kake.setLevelDuration(10);
        //kake.setStartChips(10000);

    }
}
