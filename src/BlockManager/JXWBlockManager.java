package BlockManager;

import Id.Id;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JXWBlockManager implements BlockManager {
    /**
     * 新建下一个BlockManager的编号
     */
    private static int BlockManagerNumCount = 1;

    /**
     * BlockManager的编号
     */
    private int BlockManagerNum;

    /**
     * BlockManager的编号
     */
    private int BlockNumCount = 1;

    /**
     * 此BlockManager保有的Block
     */
    private List<JXWBlock> BlockList = new ArrayList<>();

    JXWBlockManager(){
        BlockManagerNum = BlockManagerNumCount++;
        String root = "../../output/BlockManagers/";

        //创建BlockManager的目录
        File file = new File(root + "FM-" + BlockManagerNum);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功
                //Todo
                //输出到日志
            }
        }
    }

    @Override
    public int getBlockManagerNum() {
        return BlockManagerNum;
    }

    @Override
    public int getBlockNumCount() {
        return BlockNumCount++;
    }

    /**
     * 在此BlockManager中新增一个内容为byte数组b的Block，如果超出Block的大小后面的内容将不被写入Block中
     * @param b 新增Block中写入的数据
     * @return 写了byte数组b的新Block
     */
    @Override
    public Block newBlock(byte[] b) {
        JXWBlock newEmptyBlock = new JXWBlock(this, b);
        BlockList.add(newEmptyBlock);
        return newEmptyBlock;
    }

    /**
     * 获得这个BlockId对应的Block
     * @param indexId Block的Id
     * @return 对应的Block
     */
    @Override
    public Block getBlock(Id indexId){
        int Num = indexId.getNum();
        int ManagerNum = indexId.getManagerNum();

        for (JXWBlock block : BlockList){
            int blockNum = block.getIndexId().getNum();
            int blockManagerNum = block.getIndexId().getManagerNum();
            if (blockManagerNum == ManagerNum && blockNum == Num){
                return block;
            }
        }

        return null;
    }

    /**
     * 在此BlockManager中新增一个大小为blockSize的Block
     * @param blockSize block的大小
     * @return 大小为blockSize的Block
     */
    @Override
    public Block newEmptyBlock(int blockSize) {
        JXWBlock newEmptyBlock = new JXWBlock(this, blockSize);
        BlockList.add(newEmptyBlock);
        return newEmptyBlock;
    }
}
