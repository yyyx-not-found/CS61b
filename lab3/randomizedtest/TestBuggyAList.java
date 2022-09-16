package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import timingtest.AList;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AList<Integer> expected = new AList<>();
        BuggyAList<Integer> actual = new BuggyAList<>();

        /* Add three values */
        for (int i = 4; i <= 6; i++) {
            expected.addLast(i);
            actual.addLast(i);
        }

        /* Remove value and test equality */
        for (int i = 0; i < 3; i++) {
            assertEquals(expected.removeLast(), actual.removeLast());
            assertEquals(expected.size(), actual.size());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> expected = new AListNoResizing<>();
        BuggyAList<Integer> actual = new BuggyAList<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber;
            if (expected.size() > 0) {
                operationNumber = StdRandom.uniform(0, 4);
            } else {
                operationNumber = StdRandom.uniform(0, 2);
            }

            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                expected.addLast(randVal);
                actual.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int expected_size = expected.size();
                assertEquals(expected_size, actual.size());
            } else if (operationNumber == 2) {
                // getLast
                int expected_last = expected.getLast();
                assertEquals(expected_last, (int)actual.getLast());
            } else if (operationNumber == 3) {
                // removeLast
                int expected_removed = expected.removeLast();
                assertEquals(expected_removed, (int)actual.removeLast());
            }
        }
    }
}
