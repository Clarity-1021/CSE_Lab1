package SmartTools;

import BlockManager.Block;
import FileManager.FileManager;

public class SmartTools {
    //将写⼊指针移动到指定位置后，开始读取⽤户数据，并且写⼊到⽂件中
    public static void smartWrite(String fileName, int index, FileManager fm){

    }

    //获取File的File内容；能够从⽂件指定位置，读去指定⻓度的内容并且打印在控制台。
    public static void smartCat(String fileName, FileManager fm){

    }

    //读取block的data并⽤16禁⽌的形式打印到控制台
    public static void smartHex(Block block){

    }

    //复制File到另⼀个File
    //1. 读取已有的file的fileData，写⼊到新File中
    //2. 直接复制File的FileMeta，这个⽅法的正确性以来于Block是不可重写的，建议使⽤这个⽅法实
    //现
    public static void smartCopy(String from, String to, FileManager fm){

    }
}
