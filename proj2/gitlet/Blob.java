package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;
import static gitlet.FileSystem.*;
import static gitlet.Repository.*;

final class Blob implements Serializable {
    /** Name of the file. */
    String name;
    /** Contents of the file. */
    byte[] contents;

    /** Create a new blob and save it in OBJECTS_DIR. */
    Blob(File file) {
        name = file.getName();
        contents = readContents(file);
        saveObject(this, GITLET_DIR.getPath());
    }

    /** Search blob with given ID in file system (support abbreviate search). */
    public static Blob get(String ID) {
        File file = abbreviateSearch(ID, GITLET_DIR.getPath());
        if (file == null) {
            message("File does not exist.");
            System.exit(0);
        }

        return readObject(file, Blob.class);
    }
}
