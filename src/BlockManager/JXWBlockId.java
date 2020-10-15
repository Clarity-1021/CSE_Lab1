package BlockManager;

import Id.Id;

public class JXWBlockId implements Id {
    /**
     * Block的BlockManager
     */
    private BlockManager BlockManager;
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

    public JXWBlockId(BlockManager blockManager) {
        BlockManager = blockManager;
        BlockNum = blockManager.getBlockNumCount();
        String root = "./output/BlockManagers/";//Block文件输出的默认根目录
        BlockMetaPath = root + "BM-" + getManagerNum() + "/" + "b-" + BlockNum + ".meta";
        BlockDataPath = root + "BM-" + getManagerNum() + "/" + "b-" + BlockNum + ".data";
    }

    public BlockManager getManager() {
        return BlockManager;
    }

    @Override
    public int getManagerNum() {
        return BlockManager.getBlockManagerNum();
    }

    public int getNum() {
        return BlockNum;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWBlockId && indexId.getManagerNum() == getManagerNum() && ((JXWBlockId) indexId).getNum() == BlockNum;
    }

    @Override
    public String getMetaPath() {
        return BlockMetaPath;
    }

    public String getDataPath() {
        return BlockDataPath;
    }
}
