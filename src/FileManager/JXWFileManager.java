package FileManager;

import BlockManager.JXWBlock;
import Id.Id;

import java.util.ArrayList;
import java.util.List;

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
     * 新建下一个File的编号
     */
    private int FileNumCount = 1;

    /**
     * 此FileManager保有的File
     */
    private List<File> FileList = new ArrayList<>();


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


    @Override
    public File newFile(Id fileId) {
        return null;
    }
}
