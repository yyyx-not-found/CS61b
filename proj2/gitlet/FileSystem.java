package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

class FileSystem {
    /* Hash */

    /** Return SHA-1 code of the serializable item. */
    static <T extends Serializable> String getHash(T object) {
        return sha1(serialize(object));
    }

    /** Return pointer of file with given SHA-1 (or abbreviate SHA-1), or return null if not find. */
    static File abbreviateSearch(String hash) {
        if (hash == null || hash.length() > UID_LENGTH) {
            return null;
        }

        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        if (!dir.exists()) {
            return null;
        }

        DirList candidates = new DirList(dir, (_dir, name) -> name.startsWith(hash));
        if (candidates.names.length != 1) {
            return null;
        }

        return join(dir, candidates.names[0]);
    }

    /** Return commit with given SHA-1, or return null if not find. */
    static Commit getCommit(String hash) {
        File file = abbreviateSearch(hash);
        if (file == null) {
            return null;
        }
        return readObject(file, Commit.class);
    }

    /** Return blob with given SHA-1, or return null if not find. */
    static Blob getBlob(String hash) {
        File file = abbreviateSearch(hash);
        if (file == null) {
            return null;
        }
        return readObject(file, Blob.class);
    }

    /** Return head with given SHA-1, or return null if not find. */
    static Head getHead(String headName) {
        File headFile = join(HEADS_DIR, headName);
        if (!join(HEADS_DIR, headName).exists()) {
            return null;
        }

        return readObject(abbreviateSearch(readContentsAsString(headFile)), Head.class);
    }

    /* File I/O */

    /** Save Objects (Blob, Commit, Head...) in OBJECTS_DIR with abbreviate folder. */
    static <T extends Serializable> void saveObject(T object, boolean overwrite) {
        String hash = getHash(object);
        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        dir.mkdir();

        File obj = join(dir, hash);
        if (!obj.exists()) {
            writeObject(join(dir, hash), object);
        }
    }

    /** Delete old object in OBJECTS_DIR for mutable refs (Head, StagingArea, GitTree...). */
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
            join(dir, hash).delete(); // Delete head file
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

    /* Status */

    interface Status {
        boolean judge(String fileName);
    }

    /* Status of File in CWD */

    /** Staged for addition. */
    static final Status isStagedForAddition = (fileName) -> stagingArea.addition.containsKey(fileName);
    /** Staged for removal. */
    static final Status isStagedForRemoval = (fileName) -> stagingArea.removal.contains(fileName);
    /** Staged for addition or removal. */
    static final Status isStaged = (fileName) -> (isStagedForAddition.judge(fileName) || isStagedForRemoval.judge(fileName));
    /** Existed in CWD. */
    static final Status isExisted = (fileName) -> join(CWD, fileName).exists();
    /** Tracked in current commit. */
    static final Status isTracked = (fileName) -> getCommit(HEAD.headCommit).files.containsKey(fileName);
    /** Present in the working directory but neither staged for addition nor tracked. */
    static final Status isUntracked = (fileName) ->
            isExisted.judge(fileName) && !isStagedForAddition.judge(fileName) && !isTracked.judge(fileName);
    /** No change from staging area. */
    static final Status isSameAsStaging = (fileName) ->
            isExisted.judge(fileName) && stagingArea.addition.containsValue(getHash(new Blob(join(CWD, fileName))));
    /** No change from current commit. */
    static final Status isSameAsCurrentCommit = (fileName) ->
            isExisted.judge(fileName) && getCommit(HEAD.headCommit).files.containsValue(getHash(new Blob(join(CWD, fileName))));

}
