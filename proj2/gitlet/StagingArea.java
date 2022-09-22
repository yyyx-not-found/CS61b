package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;
import static gitlet.Utils.*;

class StagingArea implements Serializable {
    /** Map file name to SHA-1 */
    Map<String, String> addition = new TreeMap<>();
    /** Map file name to SHA-1 */
    Set<String> removal = new TreeSet<>();

    void save() {
        deleteOldRef(STAGING_FILE);
        saveObject(this, GITLET_DIR.getPath());
        writeContents(STAGING_FILE, getHash(this));
    }
}
