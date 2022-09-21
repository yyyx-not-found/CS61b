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

    /** Create a new branch head on the commit, and save in HEADS_DIR. */
    Head(String name, Commit commit) {
        this.name = name;
        if (commit != null) {
            String hash = getHash(commit);
            headCommit = hash;
        }
        writeObject(join(HEADS_DIR, name), this); // Save branch
    }

    /** Do commit on the branch, then update HEAD and branch info. */
    static void makeCommit(String message, Date date) {
        Commit newCommit = new Commit(message, date, null);
        
        gitTree.commits.add(getHash(newCommit)); // Add to git tree
        HEAD.headCommit = getHash(newCommit); // Update head commit
        writeObject(join(HEADS_DIR, HEAD.name), HEAD); // Save branch
    }

    /** Find split point with given branch. */
    static String getSplitPoint(String givenBranchName) {
        /* Get all ancestor of HEAD */
        Set<String> commits = new TreeSet<>();
        getAllAncestor(getCommit(HEAD.headCommit), commits);

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

    private static void getAllAncestor(Commit commit, Set<String> commits) {
        if (commit == null) {
            return;
        }

        commits.add(getHash(commit));
        for (String parent : commit.parents) {
            if (parent != null) {
                getAllAncestor(getCommit(parent), commits);
            }
        }
    }

    /** Merge given branch into the branch. */
    static void merge(String givenBranchName, boolean isConflict) {
        String message = isConflict? "Encountered a merge conflict.": "Merged " + givenBranchName + " into " + HEAD.name + ".";
        Commit newCommit = new Commit(message, new Date(), getHead(givenBranchName).headCommit);

        gitTree.commits.add(getHash(newCommit)); // Add to git tree
        HEAD.headCommit = getHash(newCommit); // Update head commit
        writeObject(join(HEADS_DIR, HEAD.name), HEAD); // Save branch
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
