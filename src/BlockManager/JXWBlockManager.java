package BlockManager;

import ErrorManager.ErrorCode;
import Id.Id;

import java.io.*;

public class JXWBlockManager implements BlockManager {
    private static String root = "./output/BlockManagers/";

    /**
     * 新建下一个BlockManager的编号
     */
    private static int BlockManagerNumCount = 1;

    /**
     * BlockManager的名字
     */
    private String BlockManagerName;

    public JXWBlockManager() throws ErrorCode{
        BlockManagerName = "BM-" + BlockManagerNumCount++;

        //创建BlockManager的目录
        File file = new File(root + BlockManagerName);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，记录在日志里面
                throw new ErrorCode(ErrorCode.BLOCK_MANAGER_DIR_CONSTRUCT_FAILED);
            }
        }
    }

    public JXWBlockManager(int blockManagerNum) throws ErrorCode{
        BlockManagerName = "BM-" + blockManagerNum;

        //创建BlockManager的目录
        File file = new File(root + BlockManagerName);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，记录在日志里面
                throw new ErrorCode(ErrorCode.BLOCK_MANAGER_DIR_CONSTRUCT_FAILED);
            }
        }
    }

    public JXWBlockManager(String blockManagerName) throws ErrorCode{
        BlockManagerName = blockManagerName;

        //创建BlockManager的目录
        File file = new File(root + BlockManagerName);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，记录在日志里面
                throw new ErrorCode(ErrorCode.BLOCK_MANAGER_DIR_CONSTRUCT_FAILED);
            }
        }
    }

    @Override
    public String getBlockManagerName() {
        return BlockManagerName;
    }

    /**
     * 在此BlockManager中新增一个内容为byte数组b的Block，如果超出Block的大小后面的内容将不被写入Block中
     * @param b 新增Block中写入的数据
     * @return 写了byte数组b的新Block
     */
    @Override
    public Block newBlock(byte[] b) {
        return new JXWBlock(BlockManagerName, b);
    }

    /**
     * 获得这个Id对应的Block
     * @param indexId Block的Id
     * @return 对应的Block
     */
    @Override
    public Block getBlock(Id indexId){
        return new JXWBlock(BlockManagerName, indexId);
    }

    /**
     * 在此BlockManager中新增一个大小为blockSize的Block
     * @param blockSize block的大小
     * @return 大小为blockSize的Block
     */
    @Override
    public Block newEmptyBlock(int blockSize) {
        return newBlock(new byte[blockSize]);
    }
}
