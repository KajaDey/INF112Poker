package gamelogic;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kristianrosland on 05.04.2016.
 */
public class NameGeneratorTest {

    @Test
    public void testGetRandomName() throws Exception {

        for(int i = 0;i<100;i++) {
            String name = NameGenerator.getRandomName();
            assertTrue(name != null && !name.equals(""));
        }
    }
}