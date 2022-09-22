package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Repository.*;
import static gitlet.FileSystem.*;
import static gitlet.Utils.*;

class Head implements Serializable {
    /** Branch name. */
    String name;
    /** Reference to head commit of the branch. */
    String headCommit;
    /** Record whether the head is local head or fetched head. */
    boolean isFetched = false;

    /** Create a new branch head on the commit, and save in HEADS_DIR. */
    Head(String name, String commit) {
        this.name = name;
        Pattern pattern = Pattern.compile(".*/.*");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            isFetched = true;
        }

        updateHeadCommit(commit);
    }

    /** Update head commit and also update file in file system. */
    void updateHeadCommit(String headCommit) {
        deleteOldRef(join(HEADS_DIR, name));

        /* Update */
        this.headCommit = headCommit;
        saveObject(this, GITLET_DIR.getPath()); // Save Head in OBJECTS_DIR

        if (isFetched) {
            String remoteHeadPath = REMOTES_DIR.getPath() + File.separator + name;
            writeContents(new File(remoteHeadPath), getHash(this)); // Save reference of Head in REMOTES_DIR
        } else {
            writeContents(join(HEADS_DIR, name), getHash(this)); // Save reference of Head in HEADS_DIR
        }
    }

    /** display each commit backwards along the commit tree, following the first parent commit links. */
    void log() {
        Commit commit = getCommit(headCommit);
        while (commit != null) {
            System.out.println(commit);
            commit = getCommit(commit.parents[0]);
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
        getAllHistoryCommit(getCommit(HEAD.headCommit), commits);

        /* BFS */
        Commit commit = getCommit(getHead(givenBranchName).headCommit);
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
                    Commit parentCommit = getCommit(parent);
                    if (!queue.contains(parentCommit)) {
                        queue.addLast(parentCommit);
                    }
                }
            }
        }

        return getHash(splitPoint);
    }

    /** Modify commits to contain all ancestor commitIDs of given commit, including itself. */
    static void getAllHistoryCommit(Commit commit, Set<String> commitIDs) {
        if (commit == null || commitIDs.contains(getHash(commit))) {
            return;
        }

        commitIDs.add(getHash(commit));
        for (String parent : commit.parents) {
            if (parent != null) {
                getAllHistoryCommit(getCommit(parent), commitIDs);
            }
        }
    }
}
