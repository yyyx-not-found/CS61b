package gitlet;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

class GitTree implements Serializable {
    /** Contain all SHA-1 of commits. */
    Set<String> commits = new TreeSet<>();

}
