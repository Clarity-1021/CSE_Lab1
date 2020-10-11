package FileManager;

import Id.Id;

public class JXWFileId implements Id {
    /**
     * File的FileManager
     */
    private FileManager FileManager;
    /**
     * File的编号
     */
    private int FileNum;
    /**
     * File的meta文件的路径
     */
    private String FileMetaPath;

    public JXWFileId(FileManager fileManager) {
        FileManager = fileManager;
        FileNum = fileManager.getFileManagerNum();
        String root = "./output/FileManagers/";//Block文件输出的默认根目录
        FileMetaPath = root + "FM-" + getManagerNum() + "/" + "f-" + FileNum + ".meta";
    }

    public FileManager getFileManager() {
        return FileManager;
    }

    @Override
    public int getManagerNum() {
        return FileManager.getFileManagerNum();
    }

    @Override
    public int getNum() {
        return FileNum;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWFileId && indexId.getManagerNum() == getManagerNum() && indexId.getNum() == FileNum;
    }

    @Override
    public String getMetaPath() {
        return FileMetaPath;
    }
}
