package gitlet;

import java.io.File;
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

    /** Create a new branch head on the commit, and save in HEADS_DIR. */
    Head(String name) {
        this.name = name;
    }

    /** Update head commit and also update file in file system. */
    void updateHeadCommit(String headCommit) {
        deleteOldRef(join(HEADS_DIR, name));

        /* Update */
        this.headCommit = headCommit;
        saveObject(this, GITLET_DIR.getPath()); // Save Head in OBJECTS_DIR

        writeContents(join(HEADS_DIR, name), getHash(this)); // Save reference of Head in HEADS_DIR
    }

    /** Return head with given name. */
    static Head get(String headName) {
        File headFile = join(HEADS_DIR, headName);
        if (!headFile.exists()) {
            return null;
        }
        return readObject(abbreviateSearch(readContentsAsString(headFile), GITLET_DIR.getPath()), Head.class);
    }

    /** display each commit backwards along the commit tree, following the first parent commit links. */
    void log() {
        String ID = headCommit;
        while (ID != null) {
            Commit commit = Commit.get(ID);
            System.out.println(commit);
            ID = commit.parents[0];
        }
    }

    /**
     * Do commit on the branch, then update HEAD and branch info.
     * @param parent2 is used for merge.
     */
    static void makeCommit(String message, Date date, String parent2) {
        Commit newCommit = new Commit(message, date, parent2);

        /* Clean staging area */
        stagingArea.addition = new TreeMap<>();
        stagingArea.removal = new TreeSet<>();

        HEAD.updateHeadCommit(getHash(newCommit));
    }

    /** Find split point with given branch. */
    static String getSplitPoint(String givenBranchName) {
        /* Get all ancestor of HEAD */
        Set<String> commits = new TreeSet<>();
        Commit.getParents(Commit.get(HEAD.headCommit), commits);

        /* BFS */
        Commit commit = Commit.get(Head.get(givenBranchName).headCommit);
        Commit splitPoint = null;
        LinkedList<Commit> queue = new LinkedList<>();
        queue.add(commit);
        while (!queue.isEmpty()) {
            Commit cur = queue.removeFirst();
            if (commits.contains(getHash(cur))) {
                if (splitPoint == null || splitPoint.timeStamp.compareTo(cur.timeStamp) < 0) {
                    splitPoint = cur;
                }
            }

            for (String parent : cur.parents) {
                if (parent != null) {
                    Commit parentCommit = Commit.get(parent);
                    if (!queue.contains(parentCommit)) {
                        queue.addLast(parentCommit);
                    }
                }
            }
        }

        return getHash(splitPoint);
    }
}