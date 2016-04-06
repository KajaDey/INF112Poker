package gamelogic;

/**
 * Created by kristianrosland on 07.03.2016.
 *
 * TODO: info about the class
 */
public abstract class User {
    private String name;

    public User(String name) {
        this.name = name;
    }

    /**
     * Getter
     * @return user name
     */
    public String getName() {
        return name;
    }
}
