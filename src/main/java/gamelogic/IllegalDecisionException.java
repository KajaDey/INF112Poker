package gamelogic;

/**
 * Created by morten on 19.04.16.
 */
public class IllegalDecisionException extends Throwable {
    public IllegalDecisionException() {}
    public IllegalDecisionException(String message) {
        super(message);
    }
}
