package gitlet;

import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;
import static gitlet.Utils.*;

class Head implements Serializable {
    /** Branch name. */
    String name;
    /** Reference to head commit of the branch. */
    String headCommit;
    /** Contain file names tracked in the branch. */
    Set<String> trackedFileNames = new TreeSet<>();
    /** Contain file names staged for removal. */
    Set<String> removedFileNames = new TreeSet<>();
    /** Maintain split point between this branch and others (null if two branches don't have common ancestor).
     * Map branch name to ID of split point.
     */
    Map<String, String> splitPoints = new TreeMap<>();

    /** Create a new branch head on the commit, and save in HEADS_DIR. */
    Head(String name, Commit commit) {
        this.name = name;
        if (commit != null) {
            String hash = getHash(commit);
            headCommit = hash;

            splitPoints.put(HEAD.name, hash); // Commit not null means there is a split point
            HEAD.splitPoints.put(name, hash); // Also need to update HEAD
        }
        writeObject(join(HEADS_DIR, name), this); // Save branch
    }

    /** Do commit on the branch, then update HEAD and branch info. */
    static void makeCommit(String message, Date date) {
        Commit newCommit = new Commit(message, date, HEAD.headCommit, null);
        
        gitTree.commits.add(getHash(newCommit)); // Add to git tree

        /* Update HEAD */
        HEAD.removedFileNames = new TreeSet<>(); // Clean files staged for removal
        HEAD.headCommit = getHash(newCommit); // Update head commit
        HEAD.trackedFileNames = new TreeSet<>(newCommit.files.keySet()); // Update tracked files
    }

    /** display each commit backwards along the commit tree, following the first parent commit links. */
    void log() {
        Commit commit = getCommit(headCommit);
        while (commit != null) {
            System.out.println(commit);
            commit = getCommit(commit.parents[0]);
        }
    }

}
