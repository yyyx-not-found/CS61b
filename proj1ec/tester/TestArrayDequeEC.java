package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.ArrayList;

public class TestArrayDequeEC {
    /** Random test for only int type. */
    @Test
    public void arrayDequeRandomTest() {
        StudentArrayDeque<Integer> actual = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> expected = new ArrayDequeSolution<>();
        ArrayList<String> trace = new ArrayList<>();

        int testSize = 500;
        int valueSize = 1000;

        for (int i = 0; i < testSize; i++) {
            int operation;
            if (actual.size() == 0) {
                operation = 0;
            } else {
                operation = StdRandom.uniform(2);
            }

            switch (operation) {
                case 0:
                    /* Add */
                    randomAdd(actual, expected, valueSize, trace);
                    break;

                case 1:
                    /* Remove */
                    randomRemove(actual, expected, trace);
                    break;
            }
        }
    }

    private void randomAdd(StudentArrayDeque<Integer> actual,
                           ArrayDequeSolution<Integer> expected,
                           int valueSize,
                           ArrayList<String> trace){
        int randomVal = StdRandom.uniform(valueSize);

        int mode = StdRandom.uniform(2);
        switch (mode) {
            case 0:
                /* Add first. */
                trace.add("addFirst(" + randomVal + ")");
                actual.addFirst(randomVal);
                expected.addFirst(randomVal);
                assertEquals(String.join("\n", trace), expected.size(), actual.size());
                break;

            case 1:
                /* Add last. */
                trace.add("addLast(" + randomVal + ")");
                actual.addLast(randomVal);
                expected.addLast(randomVal);
                assertEquals(String.join("\n", trace), expected.size(), actual.size());
                break;
        }
    }

    private void randomRemove(StudentArrayDeque<Integer> actual,
                              ArrayDequeSolution<Integer> expected,
                              ArrayList<String> trace) {
        int mode = StdRandom.uniform(2);
        Integer expectedVal, actualVal;
        switch (mode) {
            case 0:
                /* Remove first. */
                trace.add("removeFirst()");
                expectedVal = expected.removeFirst();
                actualVal = actual.removeFirst();

                assertEquals(String.join("\n", trace), expectedVal, actualVal);
                break;

            case 1:
                /* Remove last. */
                trace.add("removeLast()");
                expectedVal = expected.removeLast();
                actualVal = actual.removeLast();

                assertEquals(String.join("\n", trace), expectedVal, actualVal);
                break;
        }
    }

    private void randomGet(StudentArrayDeque<Integer> actual, ArrayDequeSolution<Integer> expected) {
        int randomIndex = StdRandom.uniform(actual.size());
        assertEquals(expected.get(randomIndex), actual.get(randomIndex));
    }
}
