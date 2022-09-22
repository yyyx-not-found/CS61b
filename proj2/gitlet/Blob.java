package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;
import static gitlet.FileSystem.*;

final class Blob implements Serializable {
    /** Name of the file. */
    String name;
    /** Contents of the file. */
    byte[] contents;

    /** Create a new blob and save it in OBJECTS_DIR. */
    Blob(File file) {
        name = file.getName();
        contents = readContents(file);
        saveObject(this, false);
    }
}
