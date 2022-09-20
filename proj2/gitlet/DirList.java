package gitlet;

import java.io.File;
import java.io.FilenameFilter;

import static gitlet.Utils.*;

public class DirList {
    /** Path of the directory. */
    File path;
    /** Contain file names in the directory. */
    String[] names;

    /** Create a new DirList with that iterates through plain files. */
    DirList(File path) {
        this.path = path;
        names = path.list(((dir, name) -> join(dir, name).isFile()));
        if (names == null) {
            names = new String[0];
        }
    }

    /** Create a new DirList with that iterates with given filter. */
    DirList(File path, FilenameFilter filter) {
        this.path = path;
        names = path.list((dir, name) -> join(dir, name).isFile() && filter.accept(dir, name));
        if (names == null) {
            names = new String[0];
        }
    }

    /** A runner that can iterate through and do some actions. */
    interface Runner {
        /** The action that runner will do when iterating. */
        void run(String name);
    }

    /** Iterate through names and do the action. */
    void iterate(Runner action) {
        for (String name : names) {
            action.run(name);
        }
    }

    /** Iterate through names and print out name. */
    void iterate() {
        for (String name : names) {
            System.out.println(name);
        }
    }
}
