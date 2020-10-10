package BlockManager;

import Id.Id;

public class JXWBlockId implements Id {
    /**
     * Block的BlockManager的编号
     */
    private int BlockManagerNum;
    /**
     * Block的编号
     */
    private int BlockNum;
    /**
     * Block的meta文件的路径
     */
    private String BlockMetaPath;
    /**
     * Block的data文件的路径
     */
    private String BlockDataPath;

    public JXWBlockId(int blockManagerNum, int blockNum) {
        BlockManagerNum = blockManagerNum;
        BlockNum = blockNum;
        String root = "./output/BlockManagers/";//Block文件输出的默认根目录
        BlockMetaPath = root + "BM-" + blockManagerNum + "/" + "b-" + blockNum + ".meta";
        BlockDataPath = root + "BM-" + blockManagerNum + "/" + "b-" + blockNum + ".data";
    }

    @Override
    public int getManagerNum() {
        return BlockManagerNum;
    }

    @Override
    public int getNum() {
        return BlockNum;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWBlockId && indexId.getManagerNum() == BlockManagerNum && indexId.getNum() == BlockNum;
    }

    @Override
    public String getMetaPath() {
        return BlockMetaPath;
    }

    @Override
    public String getDataPath() {
        return BlockDataPath;
    }
}
