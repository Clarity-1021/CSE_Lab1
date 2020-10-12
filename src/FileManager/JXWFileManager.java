package FileManager;

import BlockManager.BlockManager;
import ErrorManager.ErrorLog;
import Id.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JXWFileManager implements FileManager {
    /**
     * 新建下一个FileManager的编号
     */
    private static int FileManagerNumCount = 1;

    /**
     * FileManager的编号
     */
    private int FileManagerNum;

    /**
     * FileManager关联的所有BlockManager
     */
    private List<BlockManager> BlockManagerList;

    /**
     * 此FileManager保有的File
     */
    private List<File> FileList = new ArrayList<>();

    JXWFileManager(List<BlockManager> blockManagerList){
        FileManagerNum = FileManagerNumCount++;
        BlockManagerList = blockManagerList;
        String root = "./output/FileManagers/";

        //创建BlockManager的目录
        java.io.File file = new java.io.File(root + "FM-" + FileManagerNum);
        if (!file.exists()){//目录不存在
            if (!file.mkdir()){//创建目录不成功，记录在日志里面
                ErrorLog.logErrorMessage("FileManager-" + FileManagerNum + "目录创建失败");
            }
        }
    }

    @Override
    public int getFileManagerNum() {
        return FileManagerNum;
    }

    /**
     * 获得这个Id对应的File
     * @param fileId File的Id
     * @return 对应的File
     */
    @Override
    public File getFile(Id fileId) {
        for (File file : FileList){
            if (file.getFileId().equals(fileId)){
                return file;
            }
        }

        return null;
    }

    public BlockManager getRandomBlockManager(){
        return BlockManagerList.get((new Random()).nextInt(BlockManagerList.size()));
    }

    /**
     * 在此FileManager下面新增新的File
     * @param fileId File的Id
     * @return FileId给定的新的File
     */
    @Override
    public File newFile(Id fileId) {
        File newFile = new JXWFile(fileId);
        FileList.add(newFile);
        return newFile;
    }
}
