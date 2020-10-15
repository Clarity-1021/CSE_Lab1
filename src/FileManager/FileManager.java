package FileManager;

import Id.Id;

public interface FileManager {
    String getFileManagerName();
    File getFile(Id fileId);
    File newFile(Id fileId);
}
