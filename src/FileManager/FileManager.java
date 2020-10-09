package FileManager;

import Id.Id;

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId);
}
