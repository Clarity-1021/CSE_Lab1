package FileManager;

import Id.Id;

public class JXWFileId implements Id {
    /**
     * File的FileManager
     */
    private FileManager FileManager;
    /**
     * File的Name
     */
    private String FileName;
    /**
     * File的meta文件的路径
     */
    private String FileMetaPath;

    public JXWFileId(FileManager fileManager, String fileName) {
        FileManager = fileManager;
        FileName = fileName;
        String root = "./output/FileManagers/";//Block文件输出的默认根目录
        FileMetaPath = root + "FM-" + getManagerNum() + "/" + "f-" + FileName + ".meta";
    }

    public FileManager getFileManager() {
        return FileManager;
    }

    @Override
    public int getManagerNum() {
        return FileManager.getFileManagerNum();
    }

    public String getName() {
        return FileName;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWFileId && indexId.getManagerNum() == getManagerNum() && ((JXWFileId) indexId).getName().equals(FileName);
    }

    @Override
    public String getMetaPath() {
        return FileMetaPath;
    }
}
