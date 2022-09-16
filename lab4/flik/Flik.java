package flik;

/** An Integer tester created by Flik Enterprises.
 * @author Josh Hug
 * */
public class Flik {
    /** @param a Value 1
     *  @param b Value 2
     *  @return Whether a and b are the same */
    public static boolean isSameNumber(Integer a, Integer b) {
        /* The JVM is caching Integer values.
         * Hence the comparison with == only works for numbers between -128 and 127. */
        return a.equals(b);
    }
}
