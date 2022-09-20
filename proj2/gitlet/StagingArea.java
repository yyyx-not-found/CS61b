package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

class StagingArea implements Serializable {
    /** Map file name to SHA-1 */
    Map<String, String> map = new TreeMap<>();

}
