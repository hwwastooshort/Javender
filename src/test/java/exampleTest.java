import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class exampleTest {
    @Test
    void testExampleForDemonstrationPurposes() {
        int expected = 2;
        int actual = 1 + 1;
        assertEquals(expected, actual, "1 + 1 should be equal to 2");
    }
}
