package gitlet;

import static gitlet.Repository.*;
import static gitlet.Utils.join;
import static gitlet.FileSystem.*;

interface Status {
    /** Determine whether file in CWD satisfies the status. */
    boolean judge(String fileName);

    /** Staged for addition. */
    Status isStagedForAddition = (fileName) -> stagingArea.addition.containsKey(fileName);
    /** Staged for removal. */
    Status isStagedForRemoval = (fileName) -> stagingArea.removal.contains(fileName);
    /** Staged for addition or removal. */
    Status isStaged = (fileName) ->
            (isStagedForAddition.judge(fileName) || isStagedForRemoval.judge(fileName));
    /** Existed in CWD. */
    Status isExisted = (fileName) -> join(CWD, fileName).exists();
    /** Tracked in current commit. */
    Status isTracked = (fileName) -> Commit.get(HEAD.headCommit).files.containsKey(fileName);
    /** Present in the working directory but neither staged for addition nor tracked. */
    Status isUntracked = (fileName) -> isExisted.judge(fileName) &&
            !isStagedForAddition.judge(fileName) && !isTracked.judge(fileName);
    /** No change from staging area. */
    Status isSameAsStaging = (fileName) -> isExisted.judge(fileName) &&
            stagingArea.addition.containsValue(getHash(new Blob(join(CWD, fileName))));
    /** No change from current commit. */
    Status isSameAsCurrentCommit = (fileName) -> isExisted.judge(fileName) &&
            Commit.get(HEAD.headCommit).files.containsValue(getHash(new Blob(join(CWD, fileName))));
}
