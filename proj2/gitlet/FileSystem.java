package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Status.*;

/**
 * Inspired by https://www.knowledgehut.com/tutorials/git-tutorial/git-objects
 * which explains the object file system of git.
 */
class FileSystem {
    /* Hash */

    /** Return SHA-1 code of the serializable item. */
    static <T extends Serializable> String getHash(T object) {
        return sha1(serialize(object));
    }

    /** Return pointer of file with given SHA-1 (or abbreviate SHA-1), or return null if not find. */
    static File abbreviateSearch(String hash, String gitletDir) {
        if (hash == null || hash.length() > UID_LENGTH) {
            return null;
        }

        File objectDir = join(String.join(File.separator, gitletDir, "objects"));
        File dir = join(objectDir, hash.substring(0, ABBREVIATE_LENGTH));
        if (!dir.exists()) {
            return null;
        }

        DirList candidates = new DirList(dir, (_dir, name) -> name.startsWith(hash.substring(ABBREVIATE_LENGTH)));
        if (candidates.names.length != 1) {
            return null;
        }

        return join(dir, candidates.names[0]);
    }

    /* Get */

    /** Return commit with given SHA-1, or return null if not find. */
    static Commit getCommit(String hash) {
        File file = abbreviateSearch(hash, GITLET_DIR.getPath());
        if (file == null) {
            return null;
        }
        return readObject(file, Commit.class);
    }

    /** Return blob with given SHA-1, or return null if not find. */
    static Blob getBlob(String hash) {
        File file = abbreviateSearch(hash, GITLET_DIR.getPath());
        if (file == null) {
            return null;
        }
        return readObject(file, Blob.class);
    }

    /** Return head with given name, not including remote head.
     * Local head: headName
     * Fetched head: remoteName/remoteHeadName
     */
    static Head getHead(String headName) {
        File headFile = join(HEADS_DIR, headName);
        if (headFile.exists()) {
            return readObject(abbreviateSearch(readContentsAsString(headFile), GITLET_DIR.getPath()), Head.class);
        }

        if (headName.contains("/")) {
            String[] names = headName.split("/");
            String remoteName = names[0];
            String remoteHeadName = names[1];
            File fetchedHeadFile = new File(String.join(File.separator, REFS_DIR.getPath(), remoteName, remoteHeadName));

            if (fetchedHeadFile.exists()) {
                return readObject(abbreviateSearch(readContentsAsString(fetchedHeadFile), GITLET_DIR.getPath()), Head.class);
            }
        }

        return null;
    }

    /** Return Head in HEADS_DIR of remote .gitlet folder, or return null if not find. */
    static Head getRemoteHead(String remoteName, String remoteHeadName) {
        String remoteGitletDir = getRemoteGitletPath(remoteName);
        File remoteHeadsDir = new File(String.join(File.separator, remoteGitletDir, "refs", "heads"));
        File remoteHeadFile = join(remoteHeadsDir, remoteHeadName);
        if (!remoteHeadFile.exists()) {
            return null;
        }

        return readObject(abbreviateSearch(readContentsAsString(remoteHeadFile), remoteGitletDir), Head.class);
    }

    /** Return path of remote .gitlet folder. */
    static String getRemoteGitletPath(String remoteName) {
        String rgex = String.format("\\[Remote\\]\nRemote name: %s\nRemote path: (.*)\n", remoteName);
        Pattern pattern = Pattern.compile(rgex);
        Matcher remotePath = pattern.matcher(readContentsAsString(REMOTE_FILE));
        if (!remotePath.find()) {
            message("Remote directory not found.");
            System.exit(0);
        }
        return remotePath.group(1);
    }

    /* File I/O */

    /** Save Objects (Blob, Commit, Head...) in OBJECTS_DIR (local or remote) with abbreviate folder. */
    static <T extends Serializable> void saveObject(T object, String gitletDir) {
        String hash = getHash(object);
        File objectDir = new File(String.join(File.separator, gitletDir, "objects"));
        File dir = join(objectDir, hash.substring(0, ABBREVIATE_LENGTH));
        dir.mkdir();

        File obj = join(dir, hash);
        if (!obj.exists()) {
            writeObject(join(dir, hash.substring(ABBREVIATE_LENGTH)), object);
        }
    }

    /** Delete old object in OBJECTS_DIR (only local) for mutable refs (Head, StagingArea, GitTree...). */
    static <T extends Serializable> void deleteOldRef(File refFile) {
        if (!refFile.exists()) {
            return;
        }

        String hash = readContentsAsString(refFile);
        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        DirList dirList = new DirList(dir);
        if (dirList.names.length == 1) {
            dir.delete(); // Delete folder directly
        } else {
            join(dir, hash.substring(ABBREVIATE_LENGTH)).delete(); // Delete head file
        }
    }

    /** Replace file with fileName in CWD by given SHA-1 of blob. */
    static void replaceFileInCWD(String fileName, String hash) {
        if (hash == null) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        byte[] contents = getBlob(hash).contents;
        if (contents == null) {
            writeContents(join(CWD, fileName), "");
        } else {
            writeContents(join(CWD, fileName), contents);
        }
    }

    /** Check out all files tracked by given commit, and remove tracked files not presenting in that commit. */
    static void replaceAllInCWD(String commitID) {
        Commit commit = getCommit(commitID);

        /* Check */
        new DirList(CWD).iterate((fileName) -> {
            String hash = getHash(new Blob(join(CWD, fileName)));
            if (isUntracked.judge(fileName) && (!hash.equals(commit.files.get(fileName)))) {
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        });

        new DirList(CWD).iterate((fileName) -> restrictedDelete(fileName)); // Delete all file in CWD
        for (String fileName : commit.files.keySet()) {
            replaceFileInCWD(fileName, commit.files.get(fileName));
        }

        /* Clean staging area */
        stagingArea.addition = new TreeMap<>();
        stagingArea.removal = new TreeSet<>();
    }

    /** Synchronize remote head and fetched head, and return head commitID after Synchronization. */
    static String synchronize(String remoteName, String remoteHeadName) {
        Head remoteHead = getRemoteHead(remoteName, remoteHeadName);
        if (remoteHead == null) {
            message("That remote does not have that branch.");
            System.exit(0);
        }

        Head fetchedHead = getHead(String.join("/", remoteName, remoteHeadName));
        Commit remoteHeadCommit = getCommit(remoteHead.headCommit);

        String fromGitlet, toGitlet;
        String commitID, headCommitID;
        String remoteGitletPath = getRemoteGitletPath(remoteName);

        if (fetchedHead == null || getCommit(fetchedHead.headCommit).compareTo(remoteHeadCommit) < 0) {
            /* Update fetched head */
            fromGitlet = remoteGitletPath;
            toGitlet = GITLET_DIR.getPath();
            commitID = remoteHead.headCommit;
        } else {
            fromGitlet = GITLET_DIR.getPath();
            toGitlet = remoteGitletPath;
            commitID = fetchedHead.headCommit;
        }

        headCommitID = commitID;

        while (commitID != null) {
            Commit commit = readObject(abbreviateSearch(commitID, fromGitlet), Commit.class);

            if (!abbreviateSearch(commitID, toGitlet).exists()) {
                saveObject(commit, toGitlet);
            }

            for (String blobID : commit.files.values()) {
                File blobFile = abbreviateSearch(blobID, fromGitlet);
                if (!abbreviateSearch(blobID, toGitlet).exists()) {
                    saveObject(readObject(blobFile, Blob.class), toGitlet);
                }
            }

            commitID = commit.parents[0];
        }

        return headCommitID;
    }
}
