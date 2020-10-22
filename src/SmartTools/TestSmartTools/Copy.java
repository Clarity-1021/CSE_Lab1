package SmartTools.TestSmartTools;

import FileManager.*;
import SmartTools.SmartTools;

//复制文件
public class Copy {
    public static final int blockManagerCount = 3;
    public static final int fileManagerNumFrom = 1;//被复制的文件的FileManager
    public static final String fileNameFrom = "file1";//被复制的文件名
    public static final int fileManagerNumTo = 2;//复制到的文件的FileManager
    public static final String fileNameTo = "file2";//复制到的文件名

    public static void main(String[] args) {
        FileManager fmFrom = new JXWFileManager(fileManagerNumFrom, blockManagerCount);//生成指定FileManager
        FileManager fmTo = new JXWFileManager(fileManagerNumTo, blockManagerCount);//生成指定FileManager
        SmartTools.smartCopy(fileNameFrom, fileNameTo, fmFrom, fmTo);//在fm1中把f1复制到f2
    }
}
