package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class StagingArea implements Serializable {
    /** Map file name to SHA-1 */
    Map<String, String> addition = new TreeMap<>();
    /** Map file name to SHA-1 */
    Set<String> removal = new TreeSet<>();
}
