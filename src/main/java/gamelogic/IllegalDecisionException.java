package gamelogic;

/**
 * Exception thrown if an illegal decision is made and accepted by Game
 *
 * @author Morten Lohne
 */
public class IllegalDecisionException extends Throwable {
    public IllegalDecisionException() {}
    public IllegalDecisionException(String message) {
        super(message);
    }
}
