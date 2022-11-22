package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;
import static gitlet.Utils.*;

final class Commit implements Serializable {
    /** The message of this Commit. */
    String message;
    /** The time when the commit is made. */
    Date timeStamp;
    /** Reference to parent commits. */
    String[] parents;
    /** Map file name to SHA-1. */
    Map<String, String> files = new TreeMap<>();

    /** Create a new commit and save it in OBJECTS_DIR. */
    Commit(String message, Date timeStamp, String parent2) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.parents = new String[]{HEAD.headCommit, parent2};

        /* Inherit files in parent commit (except file staged for removal) */
        if (HEAD.headCommit != null) {
            files.putAll(Commit.get(HEAD.headCommit).files);
            for (String fileName : stagingArea.removal) {
                files.remove(fileName);
            }
        }

        /* Update new file in staging area */
        for (String fileName : stagingArea.addition.keySet()) {
            files.put(fileName, stagingArea.addition.get(fileName));
        }

        /* Add to git tree */
        if (parents[0] != null) {
            gitTree.leafs.remove(parents[0]);
        } else if (parents[1] != null) {
            gitTree.leafs.remove(parents[1]);
        }
        gitTree.leafs.add(getHash(this));

        saveObject(this, GITLET_DIR.getPath()); // Save commit
    }

    @Override
    public String toString() {
        Locale loc = new Locale("en", "US");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z", loc);

        String commit = "commit " + getHash(this) + "\n";
        String date = "Date: " + simpleDateFormat.format(timeStamp) + "\n";

        return "===\n" + commit + date + message + "\n";
    }

    public int compareTo(Commit commit) {
        if (commit == null) {
            return 1;
        }

        return timeStamp.compareTo(commit.timeStamp);
    }

    /** Search commit with given ID in file system (support abbreviate search). */
    public static Commit get(String ID) {
        File file = abbreviateSearch(ID, GITLET_DIR.getPath());
        if (file == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(file, Commit.class);
    }

    /** Modify commits to contain all ancestor commitIDs of given commit, including itself. */
    static void getParents(Commit commit, Set<String> commitIDs) {
        if (commit == null || commitIDs.contains(getHash(commit))) {
            return;
        }

        commitIDs.add(getHash(commit));
        for (String parent : commit.parents) {
            if (parent != null) {
                getParents(Commit.get(parent), commitIDs);
            }
        }
    }
}
