package FileManager;

import Id.Id;

public class JXWFileId implements Id {
    /**
     * File的FileManager的编号
     */
    private int FileManagerNum;
    /**
     * File的编号
     */
    private int FileNum;
    /**
     * File的meta文件的路径
     */
    private String FileMetaPath;

    public JXWFileId(int blockManagerNum, int blockNum) {
        FileManagerNum = blockManagerNum;
        FileNum = blockNum;
        String root = "./output/FileManagers/";//Block文件输出的默认根目录
        FileMetaPath = root + "FM-" + blockManagerNum + "/" + "f-" + blockNum + ".meta";
    }

    @Override
    public int getManagerNum() {
        return FileManagerNum;
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
        return indexId instanceof JXWFileId && indexId.getManagerNum() == FileManagerNum && indexId.getNum() == FileNum;
    }

    @Override
    public String getMetaPath() {
        return FileMetaPath;
    }

    @Override
    public String getDataPath() {
        return null;
    }
}
