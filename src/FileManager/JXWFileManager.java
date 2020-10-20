package FileManager;

import BlockManager.BlockManager;
import BlockManager.JXWBlockManager;
import ErrorManager.ErrorCode;
import Id.Id;

import java.util.Random;

public class JXWFileManager implements FileManager {

    String root = "./output/FileManagers/";

    /**
     * 新建下一个FileManager的编号
     */
    private static int FileManagerNumCount = 1;

    /**
     * FileManager的编号
     */
    private String FileManagerName;

    /**
     * FileManager关联的BlockManager的个数
     */
    private int BlockManagerCount;

    public JXWFileManager(int blockManagerCount) throws ErrorCode{
        FileManagerName = "FM-" + FileManagerNumCount++;
        BlockManagerCount = blockManagerCount;

        //创建BlockManager的目录
        java.io.File file = new java.io.File(root + FileManagerName);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，抛出异常
                throw new ErrorCode(ErrorCode.FILE_MANAGER_DIR_CONSTRUCT_FAILED);
            }
        }
    }

    public JXWFileManager(int fileManagerNum, int blockManagerCount) throws ErrorCode{
        FileManagerName = "FM-" + fileManagerNum;
        BlockManagerCount = blockManagerCount;

        //创建BlockManager的目录
        java.io.File file = new java.io.File(root + FileManagerName);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，抛出异常
                throw new ErrorCode(ErrorCode.FILE_MANAGER_DIR_CONSTRUCT_FAILED);
            }
        }
    }

    @Override
    public String getFileManagerName() {
        return FileManagerName;
    }

    /**
     * 获得这个Id对应的File
     * @param fileId File的Id
     * @return 对应的File
     */
    @Override
    public File getFile(Id fileId) throws ErrorCode {
        File file = null;

        file = new JXWFile(this, fileId, JXWFile.GET_MODE);

        return file;
    }

    public BlockManager getRandomBlockManager(){
        return new JXWBlockManager((new Random()).nextInt(BlockManagerCount) + 1);
    }

    /**
     * 在此FileManager下面新增新的File
     * @param fileId File的Id
     * @return FileId给定的新的File
     */
    @Override
    public File newFile(Id fileId) throws ErrorCode {
        File file = null;

        file = new JXWFile(this, fileId, JXWFile.NEW_MODE);

        return file;
    }
}
