package SmartTools.TestSmartTools;

import BlockManager.*;
import ErrorManager.ErrorCode;
import FileManager.*;

import java.io.File;

//初始化
public class INI {
    public static final int blockManagerCount = 3;//BlockManager的个数
    public static final int fileManagerCount = 2;//FileManager的个数
    public static final int fileManagerNum = 10;//指定生成新的FileManager的Num
    public static final int blockManagerNum = 10;//指定生成新的FileManager的Num

    public static void main(String[] args) {
        //初始化指定个数个FileManager和指定个数的BlockManager
//        initiateFM();

        //生成新的FileManager
//        new JXWFileManager(fileManagerNum, blockManagerCount);

        //生成新的BlockManager
//        new JXWBlockManager(blockManagerNum);
    }

    //清空原来的BM，创建指定数目个BlockManagers
    private static void initiateBM(){
        File bmDir = new File("./output/BlockManagers/");
        String[] children = bmDir.list();
        if (children != null){
            for (String bm : children) {
                File bmPath = new File(bmDir, bm);
                String[] blocks = bmPath.list();

                if (blocks == null) break;
                for (String block : blocks){
                    boolean success = deleteDir(new File(bmDir + bm, block));
                    if (!success){
                        new ErrorCode(ErrorCode.DELETE_FAILED).printStackTrace();
                    }
                }

                boolean success = deleteDir(bmPath);
                if (!success){
                    new ErrorCode(ErrorCode.DELETE_FAILED).printStackTrace();
                }
            }
        }

        BlockManager bm;
        for (int i = 0; i < blockManagerCount; i++) {
            try {
                bm = new JXWBlockManager();
            }
            catch (ErrorCode e) {
                e.printStackTrace();
            }
        }
    }

    //清空原来的FM，创建指定数目个FileManagers
    private static void initiateFM(){
        File fmDir = new File("./output/FileManagers/");
        String[] children = fmDir.list();
        if (children != null){
            for (String fm : children) {
                File fmPath = new File(fmDir, fm);
                String[] files = fmPath.list();

                if (files == null) break;
                for (String file : files){
                    boolean success = deleteDir(new File(fmDir + fm, file));
                    if (!success){
                        new ErrorCode(ErrorCode.DELETE_FAILED).printStackTrace();
                    }
                }

                boolean success = deleteDir(fmPath);
                if (!success){
                    new ErrorCode(ErrorCode.DELETE_FAILED).printStackTrace();
                }
            }
        }

        FileManager fm;
        for (int i = 0; i < fileManagerCount; i++) {
            try {
                fm = new JXWFileManager(blockManagerCount);
            }
            catch (ErrorCode e) {
                e.printStackTrace();
            }
        }

        initiateBM();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return 成功删除返回true，没有成功删除返回false
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //目录此时为空，可以删除
            if (children == null)
                return dir.delete();
            //递归删除目录中的子目录下
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        //目录此时为空，可以删除
        return dir.delete();
    }
}
