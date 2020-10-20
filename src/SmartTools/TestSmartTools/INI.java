package SmartTools.TestSmartTools;

import BlockManager.*;
import FileManager.*;
import ErrorManager.ErrorLog;

import java.io.File;

//初始化
public class INI {
    public static final int blockManagerCount = 10;//BlockManager的个数
    public static final int fileManagerCount = 3;//FileManager的个数

    public static void main(String[] args) {
        //初始化指定个数的BlockManager
//        initiateBM(blockManagerCount);

        //初始化指定个数个FileManager
        initiateFM(fileManagerCount, blockManagerCount);
    }

    //清空原来的BM，创建指定数目个BlockManagers
    private static void initiateBM(int bmCount){
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
                        ErrorLog.logErrorMessage(bm + " " + block + " is not deleted successfully.");
                    }
                }

                boolean success = deleteDir(bmPath);
                if (!success){
                    ErrorLog.logErrorMessage(bm + " is not deleted successfully.");
                }
            }
        }

        BlockManager bm;
        for (int i = 0; i < bmCount; i++) {
            bm = new JXWBlockManager();
        }
    }

    //清空原来的FM，创建指定数目个FileManagers
    private static void initiateFM(int fmCount, int bmCount){
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
                        ErrorLog.logErrorMessage(fm + " " + file + " is not deleted successfully.");
                    }
                }

                boolean success = deleteDir(fmPath);
                if (!success){
                    ErrorLog.logErrorMessage(fm + " is not deleted successfully.");
                }
            }
        }

        FileManager fm;
        for (int i = 0; i < fmCount; i++) {
            fm = new JXWFileManager(bmCount);
        }

        initiateBM(bmCount);
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
