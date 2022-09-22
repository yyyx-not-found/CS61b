package gitlet;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;
import static gitlet.Utils.*;

class GitTree implements Serializable {
    /** Contain all leaf nodes. */
    Set<String> leafs = new TreeSet<>();

    void save() {
        deleteOldRef(TREE_FILE);
        saveObject(this, false);
        writeContents(TREE_FILE, getHash(this));
    }
}
