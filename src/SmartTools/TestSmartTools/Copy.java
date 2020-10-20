package SmartTools.TestSmartTools;

import FileManager.*;
import SmartTools.SmartTools;

//复制文件
public class Copy {
    public static final int blockManagerCount = 10;
    public static final int fileManagerNum = 1;//FileManager
    public static final String fileNameFrom = "jxw_From";//被复制的文件名
    public static final String fileNameTo = "jxw_To";//复制到的文件名

    public static void main(String[] args) {
        FileManager fm = new JXWFileManager(fileManagerNum, blockManagerCount);//生成指定FileManager
        SmartTools.smartCopy(fileNameFrom, fileNameTo, fm);//在fm1中把f1复制到f2
    }
}
