package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Status.*;

/**
 * Inspired by <a href="https://www.knowledgehut.com/tutorials/git-tutorial/git-objects">...</a>
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
        byte[] contents = Blob.get(hash).contents;
        if (contents == null) {
            writeContents(join(CWD, fileName), "");
        } else {
            writeContents(join(CWD, fileName), contents);
        }
    }

    /** Check out all files tracked by given commit, and remove tracked files not presenting in that commit. */
    static void replaceAllInCWD(String commitID) {
        Commit commit = Commit.get(commitID);

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
}
