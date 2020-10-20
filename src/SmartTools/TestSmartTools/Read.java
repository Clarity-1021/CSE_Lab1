package SmartTools.TestSmartTools;

import FileManager.*;
import SmartTools.SmartTools;

//读文件
public class Read {
    public static final int blockManagerCount = 10;
    public static final int fileManagerNum = 2;//FileManager
    public static final String fileName = "jxw_2";//文件名

    public static void main(String[] args) {
        FileManager fm = new JXWFileManager(fileManagerNum, blockManagerCount);//生成指定FileManager
        SmartTools.smartCat(fileName, fm);//读文件
    }
}