package main.java.gamelogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public abstract class User {
    private String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
