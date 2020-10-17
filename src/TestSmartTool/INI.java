package TestSmartTool;

import Initiater.InitiateManager;

//初始化
public class INI {
    public static final int blockManagerCount = 10;//BlockManager的个数
    public static final int fileManagerCount = 3;//FileManager的个数

    public static void main(String[] args) {
        //初始化指定个数的BlockManager
//        InitiateManager.initiateBM(blockManagerCount);

        //初始化指定个数个FileManager
        InitiateManager.initiateFM(fileManagerCount, blockManagerCount);
    }
}
