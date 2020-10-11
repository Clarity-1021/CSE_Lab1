package FileManager;

import Id.Id;

public interface FileManager {
    int getFileManagerNum();
    File getFile(Id fileId);
    File newFile(Id fileId);
}
