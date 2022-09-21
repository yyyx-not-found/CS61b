package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;

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
        Commit parent = getCommit(HEAD.headCommit);
        if (parent != null) {
            files.putAll(parent.files);
            for (String fileName : stagingArea.removal) {
                files.remove(fileName);
            }
        }

        /* Update new file in staging area */
        for (String fileName : stagingArea.addition.keySet()) {
            files.put(fileName, stagingArea.addition.get(fileName));
        }
        stagingArea.addition = new TreeMap<>(); // Clean staging area

        saveObject(this); // Save commit
    }

    public String toString() {
        Locale loc = new Locale("en", "US");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z", loc);

        String commit = "commit " + getHash(this) + "\n";
        String date = "Date: " + simpleDateFormat.format(timeStamp) + "\n";

        return "===\n" + commit + date + message + "\n";
    }
}
