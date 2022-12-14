package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.FileSystem.*;
import static gitlet.Status.*;

public class Repository {
    /* Paths */

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File REMOTES_DIR = join(REFS_DIR, "remotes");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File STAGING_FILE = join(GITLET_DIR, "staging");
    public static final File TREE_FILE = join(GITLET_DIR, "git_tree");
    public static final File REMOTE_FILE = join(GITLET_DIR, "remote");

    /* Constants */
    /** The length of folders containing commits for abbreviate search. */
    static final int ABBREVIATE_LENGTH = 2;

    /* Environments */

    public static Head HEAD;
    public static StagingArea stagingArea = new StagingArea();
    public static GitTree gitTree = new GitTree();

    /* Functions */

    static void doInitCommand() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        /* Create Directories */
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        REMOTES_DIR.mkdir();

        /* Initial Commit */
        HEAD = new Head("master");
        Head.makeCommit("initial commit", new Date(0), null);
    }

    static void doAddCommand(String fileName) {
        /* Check file */
        if (!isExisted.judge(fileName)) {
            message("File does not exist.");
            System.exit(0);
        }

        /* create new blob, track it and add to staging area */
        Blob newBlob = new Blob(join(CWD, fileName));
        stagingArea.addition.put(newBlob.name, getHash(newBlob));

        /* Restore if it is staged for removal */
        if (isStagedForRemoval.judge(fileName)) {
            stagingArea.removal.remove(fileName);
        }

        /* If same as file in current commit, then remove it from staging area. */
        if (isSameAsCurrentCommit.judge(fileName)) {
            stagingArea.addition.remove(newBlob.name);
        }
    }

    static void doCommitCommand(String message, Date timeStamp) {
        /* Check message */
        if (message.length() == 0) {
            message("Please enter a commit message.");
            System.exit(0);
        }

        /* Make Commit */
        if (stagingArea.addition.isEmpty() && stagingArea.removal.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        } else {
            Head.makeCommit(message, timeStamp, null);
        }
    }

    static void doRemoveCommand(String fileName) {
        boolean tag = false; // check if the function does something.

        /* Un-stage */
        if (isStagedForAddition.judge(fileName)) {
            stagingArea.addition.remove(fileName);
            tag = true;
        }

        if (isTracked.judge(fileName)) {
            stagingArea.removal.add(fileName); // Staging for removal
            restrictedDelete(join(CWD, fileName)); // Remove file in CWD
            tag = true;
        }

        if (!tag) {
            message("No reason to remove the file.");
            System.exit(0);
        }
    }

    static void doLogCommand() {
        HEAD.log();
    }

    static void doGlobalLogCommand() {
        for (String commitID : getAllCommits()) {
            System.out.println(Commit.get(commitID));
        }
    }

    static void doFindCommand(String commitMessage) {
        boolean isFound = false;
        for (String commitID : getAllCommits()) {
            if (Commit.get(commitID).message.equals(commitMessage)) {
                System.out.println(commitID);
                isFound = true;
            }
        }

        if (!isFound) {
            message("Found no commit with that message.");
        }
    }

    /** Return a Set containing all commitIDs. */
    static Set<String> getAllCommits() {
        Set<String> commitIDs = new TreeSet<>();
        for (String commitID : gitTree.leafs) {
            Commit.getParents(Commit.get(commitID), commitIDs);
        }
        return commitIDs;
    }

    static void doStatusCommand() {
        /* Branches */
        System.out.println("=== Branches ===");
        System.out.println("*" + HEAD.name);
        DirList headNames = new DirList(HEADS_DIR, (dir, name) -> !name.equals(HEAD.name));
        headNames.iterate();
        System.out.println();

        /* Staged Files */
        System.out.println("=== Staged Files ===");
        for (String stagedFileName : stagingArea.addition.keySet()) {
            System.out.println(stagedFileName);
        }
        System.out.println();

        /* Removed Files */
        System.out.println("=== Removed Files ===");
        for (String removedFileName: stagingArea.removal) {
            System.out.println(removedFileName);
        }
        System.out.println();

        /* Modifications Not Staged For Commit */
        System.out.println("=== Modifications Not Staged For Commit ===");

        for (String stagedFileName: stagingArea.addition.keySet()) {
            if (!isExisted.judge(stagedFileName)) {
                System.out.println(stagedFileName + " (deleted)");
            } else if (!isSameAsStaging.judge(stagedFileName)) {
                System.out.println(stagedFileName + " (modified)");
            }
        }

        for (String commitFileName : Commit.get(HEAD.headCommit).files.keySet()) {
            if (!isStagedForRemoval.judge(commitFileName) && !isExisted.judge(commitFileName)) {
                System.out.println(commitFileName + " (deleted)");
            } else if (!isSameAsCurrentCommit.judge(commitFileName) && !isStaged.judge(commitFileName)) {
                System.out.println(commitFileName + " (modified)");
            }
        }

        System.out.println();

        /* Untracked Files */
        System.out.println("=== Untracked Files ===");
        DirList untrackedFileNames = new DirList(CWD, (dir, name) -> isUntracked.judge(name));
        untrackedFileNames.iterate();
        System.out.println();
    }

    static void checkoutFile(String fileName) {
        replaceFileInCWD(fileName, Commit.get(HEAD.headCommit).files.get(fileName));
        stagingArea.addition.remove(fileName); // Un-stage
    }

    static void checkoutCommit(String commitID, String fileName) {
        Commit commit = Commit.get(commitID);
        replaceFileInCWD(fileName, commit.files.get(fileName));
        stagingArea.addition.remove(fileName); // Un-stage
    }

    static void checkoutBranch(String branchName) {
        Head branch = Head.get(branchName);

        if (branch == null) {
            message("No such branch exists.");
            System.exit(0);
        } else if (branchName.equals(HEAD.name)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }

        replaceAllInCWD(branch.headCommit);
        HEAD = branch; // Switch HEAD
    }

    static void doBranchCommand(String branchName) {
        if (join(HEADS_DIR, branchName).exists()) {
            message("A branch with that name already exists.");
            System.exit(0);
        }

        Head newHead = new Head(branchName);
        newHead.updateHeadCommit(HEAD.headCommit);
    }

    static void doRemoveBranchCommand(String branchName) {
        Head head = Head.get(branchName);

        if (head == null) {
            message("A branch with that name does not exist.");
            System.exit(0);
        } else if (HEAD.name.equals(branchName)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }

        join(HEADS_DIR, branchName).delete(); // Remove branch reference
    }

    static void doResetCommand(String comminID) {
        Commit commit = Commit.get(comminID);
        if (commit == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }

        replaceAllInCWD(comminID);
        HEAD.updateHeadCommit(comminID);
    }

    static void doMergeCommand(String givenBranchName) {
        if (!stagingArea.addition.isEmpty() || !stagingArea.removal.isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        } else if (HEAD.name.equals(givenBranchName)) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Head givenBranch = Head.get(givenBranchName);
        if (givenBranch == null) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }

        String splitPoint = Head.getSplitPoint(givenBranchName);
        if (splitPoint.equals(givenBranch.headCommit)) {
            message("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPoint.equals(HEAD.headCommit)) {
            checkoutBranch(givenBranchName);
            message("Current branch fast-forwarded.");
            return;
        }

        Map<String, String> splitFiles = Commit.get(splitPoint).files;
        Map<String, String> currentFiles = Commit.get(HEAD.headCommit).files;
        Map<String, String> givenFiles = Commit.get(givenBranch.headCommit).files;

        Map<String, String> files = new TreeMap<>();
        files.putAll(splitFiles);
        files.putAll(currentFiles);
        files.putAll(givenFiles);

        /* Merge */
        boolean isConflict = false;
        for (String fileName : files.keySet()) {
            String currentBlob = currentFiles.get(fileName);
            String givenBlob = givenFiles.get(fileName);
            String splitBlob = splitFiles.get(fileName);

            /* Whether file is tracked in split point */
            if (splitBlob != null) {
                /* Whether file is modified in current commit */
                if (splitBlob.equals(currentBlob)) {
                    if (givenBlob == null) {
                        addToBeModified(files, fileName, "remove");
                    } else if (!splitBlob.equals(givenBlob)) {
                        addToBeModified(files, fileName, "replace");
                    }
                } else if (!splitBlob.equals(givenBlob) && !(currentBlob == null && givenBlob == null)) {
                    addToBeModified(files, fileName, "conflict");
                }
            } else {
                if (currentBlob == null && givenBlob != null) {
                    addToBeModified(files, fileName, "replace");
                } else if (currentBlob != null && givenBlob != null && !currentBlob.equals(givenBlob)) {
                    addToBeModified(files, fileName, "conflict");
                }
            }
        }

        /* Do operations */
        for (String fileName : files.keySet()) {
            switch (files.get(fileName)) {
                case "remove" -> doRemoveCommand(fileName);
                case "conflict" -> {
                    mergeConflict(fileName, currentFiles.get(fileName), givenFiles.get(fileName));
                    isConflict = true;
                }
                case "replace" -> {
                    String givenBlob = givenFiles.get(fileName);
                    replaceFileInCWD(fileName, givenBlob);
                    stagingArea.addition.put(fileName, givenBlob); // Stage
                }
            }
        }

        String message = isConflict? "Encountered a merge conflict.": "Merged " + givenBranchName + " into " + HEAD.name + ".";
        Head.makeCommit(message, new Date(), Head.get(givenBranchName).headCommit);
    }

    private static void addToBeModified(Map<String, String> files, String fileName, String operation) {
        if (isUntracked.judge(fileName)) {
            message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        files.put(fileName, operation);
    }

    private static void mergeConflict(String fileName, String currentBlob, String givenBlob) {
        message("Encountered a merge conflict.");

        StringBuilder stringBuilder = new StringBuilder("<<<<<<< HEAD\n");

        String currentContent = currentBlob == null? "": new String(Blob.get(currentBlob).contents, StandardCharsets.UTF_8);
        stringBuilder.append(currentContent);

        stringBuilder.append("=======\n");

        String givenContent = givenBlob == null? "": new String(Blob.get(givenBlob).contents, StandardCharsets.UTF_8);
        stringBuilder.append(givenContent);

        stringBuilder.append(">>>>>>>\n"); // Must add \n at the end!!!

        writeContents(join(CWD, fileName), stringBuilder.toString());
        doAddCommand(fileName); // Stage
    }

}
