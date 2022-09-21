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
            HEAD = readObject(HEAD_FILE, Head.class);
            stagingArea = readObject(STAGING_FILE, StagingArea.class);
            gitTree = readObject(TREE_FILE, GitTree.class);
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
                    case 2 -> doCheckOutCommand(args[1], false);
                    case 3 -> {
                        if (args[1].equals("--")) {
                            doCheckOutCommand(args[2], true);
                        } else {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                    }
                    case 4 -> {
                        if (args[2].equals("--")) {
                            doCheckOutCommand(args[1], args[3]);
                        } else {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                    }
                    default -> {
                        message("Incorrect operands.");
                        System.exit(0);
                    }
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

        writeObject(HEAD_FILE, HEAD); // Save HEAD
        writeObject(STAGING_FILE, stagingArea); // Save staging area
        writeObject(TREE_FILE, gitTree); // Save git tree
    }

    /** Assert the number of argument be num. */
    private static void validArgs(String[] args, int num) {
        if (args.length != num) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }
}
