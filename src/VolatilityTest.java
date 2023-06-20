import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolatilityTest {

    @Test
    void numDaysForward() {
        Volatility volatility = new Volatility();

        Integer expected = 0;
        Integer actual = 0;//volatility.helperDate(xxx, xxx);

        assertEquals(expected, actual);
    }
}