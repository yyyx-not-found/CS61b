package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

class FileSystem {
    /* Hash */

    /** Return SHA-1 code of the serializable item. */
    static <T extends Serializable> String getHash(T item) {
        return sha1(serialize(item));
    }

    /** Return commit with given SHA-1, or return null if not find. */
    static Commit getCommit(String hash) {
        if (hash == null) {
            return null;
        }

        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        if (!dir.exists() || hash.length() > UID_LENGTH) {
            return null;
        }

        DirList candidateCommits = new DirList(dir, (_dir, name) -> name.startsWith(hash));
        if (candidateCommits.names.length != 1) {
            return null;
        }
        return readObject(join(dir, hash), Commit.class);
    }

    /** Return blob with given SHA-1, or return null if not find. */
    static Blob getBlob(String hash) {
        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        if (!dir.exists() || !join(dir, hash).exists()) {
            return null;
        }
        return readObject(join(dir, hash), Blob.class);
    }

    /** Return head with given SHA-1, or return null if not find. */
    static Head getHead(String headName) {
        if (!join(HEADS_DIR, headName).exists()) {
            return null;
        }
        return readObject(join(HEADS_DIR, headName), Head.class);
    }

    /* File I/O */

    /** Save Blob and Commit in OBJECTS_DIR (not overwrite). */
    static <T extends Serializable> void saveObject(T object) {
        String hash = getHash(object);
        File dir = join(OBJECTS_DIR, hash.substring(0, ABBREVIATE_LENGTH));
        dir.mkdir();

        File obj = join(dir, hash);
        if (!obj.exists()) {
            writeObject(join(dir, hash), object);
        }
    }

    /** Replace file with fileName in CWD by given SHA-1 of blob. */
    static void replaceFileInCWD(String fileName, String hash) {
        if (hash == null) {
            throw error("File does not exist in that commit.");
        }
        writeContents(join(CWD, fileName), getBlob(hash).contents);
    }

    /* Status of File in CWD */

    interface Status {
        boolean judge(String fileName);
    }

    /** Staged for addition. */
    static final Status isStagedForAddition = (fileName) -> stagingArea.map.containsKey(fileName);
    /** Staged for removal. */
    static final Status isStagedForRemoval = (fileName) -> HEAD.removedFileNames.contains(fileName);
    /** Existed in CWD. */
    static final Status isExisted = (fileName) -> join(CWD, fileName).exists();
    /** Tracked in HEAD. */
    static final Status isTracked = (fileName) -> HEAD.trackedFileNames.contains(fileName);
    /** Present in the working directory but neither staged for addition nor tracked. */
    static final Status isUntracked = (fileName) ->
            isExisted.judge(fileName) && !isStagedForAddition.judge(fileName) && !isTracked.judge(fileName);
    /** No change from staging area. */
    static final Status isSameAsStaging = (fileName) ->
            isExisted.judge(fileName) && stagingArea.map.containsValue(getHash(new Blob(join(CWD, fileName))));
    /** No change from current commit. */
    static final Status isSameAsCurrentCommit = (fileName) ->
            isExisted.judge(fileName) && getCommit(HEAD.headCommit).files.containsValue(getHash(new Blob(join(CWD, fileName))));
}
