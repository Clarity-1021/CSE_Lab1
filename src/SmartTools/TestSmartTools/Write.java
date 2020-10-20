package SmartTools.TestSmartTools;

import FileManager.*;
import SmartTools.SmartTools;

//写文件
public class Write {
    public static final int blockManagerCount = 10;
    public static final int fileManagerNum = 1;//FileManager
    public static final String fileName = "jxw_From";//文件名
    public static final int index = 0;//偏移量

    public static void main(String[] args) {
        FileManager fm = new JXWFileManager(fileManagerNum, blockManagerCount);//生成指定FileManager
        SmartTools.smartWrite(fileName, index, fm);//写文件
    }
}
