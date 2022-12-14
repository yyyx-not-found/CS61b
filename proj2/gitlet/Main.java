package gitlet;

import java.util.Date;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.FileSystem.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        if (!args[0].equals("init")) {
            /* Check Environment */
            if (!GITLET_DIR.exists()) {
                message("Not in an initialized Gitlet directory.");
                System.exit(0);
            }

            /* Load info */
            HEAD = readObject(abbreviateSearch(readContentsAsString(HEAD_FILE), GITLET_DIR.getPath()), Head.class);
            stagingArea = readObject(abbreviateSearch(readContentsAsString(STAGING_FILE), GITLET_DIR.getPath()), StagingArea.class);
            gitTree = readObject(abbreviateSearch(readContentsAsString(TREE_FILE), GITLET_DIR.getPath()), GitTree.class);
        }

        switch (firstArg) {
            case "init" -> {
                validArgs(args, 1);
                doInitCommand();
            }
            case "add" -> {
                validArgs(args, 2);
                doAddCommand(args[1]);
            }
            case "commit" -> {
                validArgs(args, 2);
                doCommitCommand(args[1], new Date());
            }
            case "rm" -> {
                validArgs(args, 2);
                doRemoveCommand(args[1]);
            }
            case "log" -> {
                validArgs(args, 1);
                doLogCommand();
            }
            case "global-log" -> {
                validArgs(args, 1);
                doGlobalLogCommand();
            }
            case "find" -> {
                validArgs(args, 2);
                doFindCommand(args[1]);
            }
            case "status" -> {
                validArgs(args, 1);
                doStatusCommand();
            }
            case "checkout" -> {
                switch (args.length) {
                    case 2:
                        checkoutBranch(args[1]);
                        break;
                    case 3:
                        if (args[1].equals("--")) {
                            checkoutFile(args[2]);
                            break;
                        }
                    case 4:
                        if (args[2].equals("--")) {
                            checkoutCommit(args[1], args[3]);
                            break;
                        }
                    message("Incorrect operands.");
                    System.exit(0);
                }
            }
            case "branch" -> {
                validArgs(args, 2);
                doBranchCommand(args[1]);
            }
            case "rm-branch" -> {
                validArgs(args, 2);
                doRemoveBranchCommand(args[1]);
            }
            case "reset" -> {
                validArgs(args, 2);
                doResetCommand(args[1]);
            }
            case "merge" -> {
                validArgs(args, 2);
                doMergeCommand(args[1]);
            }
            default -> {
                message("No command with that name exists.");
                System.exit(0);
            }
        }

        writeContents(HEAD_FILE, getHash(HEAD)); // Save HEAD
        stagingArea.save(); // Save staging area
        gitTree.save(); // Save tree
    }

    /** Assert the number of argument be num. */
    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }
}
