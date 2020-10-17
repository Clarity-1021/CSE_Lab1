package SmartTools;

import BlockManager.*;
import FileManager.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SmartTools {
    private static final int FileBlockSize = 1024;
    private static final String ENDMARK = "$$JXW$$";

    //获取File的File内容；能够从⽂件指定位置，读去指定⻓度的内容并且打印在控制台。
    public static void smartCat(String fileName, FileManager fm){
        int where = 1;//从哪里移动当前游标
        int offset = 0;//偏移
        int length = 100;//需要读取的长度
        File file = new JXWFile(fm, new JXWFileId(fileName));

        file.move(offset, where);//移动当前游标
        System.out.println(new String(file.bufferedRead(length)));//打印到console
        file.close();
    }

    //将写⼊指针移动到指定位置后，开始读取⽤户数据，并且写⼊到⽂件中
    public static void smartWrite(String fileName, int index, FileManager fm){
        File file = new JXWFile(fm, new JXWFileId(fileName));

        int where = 1;//从哪里移动当前游标
        file.move(index, where);//移动当前游标到文件开始偏移index个偏移量的地方
        byte[] contentToWrite = readFromConsole();//从控制台读需要写入的数据
        file.bufferedWrite(contentToWrite);//写入数据
        file.close();//清空Buffer并写回
    }

    //读取block的data并⽤16禁⽌的形式打印到控制台
    public static void smartHex(Block block){
        byte[] content = block.read();
        System.out.println(parseBytesToHex(content));
    }

    //复制File到另⼀个File
    //1. 读取已有的file的fileData，写⼊到新File中
    //2. 直接复制File的FileMeta，这个⽅法的正确性以来于Block是不可重写的，建议使⽤这个⽅法实现
    public static void smartCopy(String from, String to, FileManager fm){
        File file = new JXWFile(fm, new JXWFileId(from), new JXWBlockId(to));
    }

    private static String parseBytesToHex(byte[] content) {
        StringBuilder sb = new StringBuilder();
        for (byte i : content) {
            String hex = Integer.toHexString(i & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }

        return sb.toString();
    }

    private static byte[] readFromConsole(){
        byte[] result = new byte[0];
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter Content To Write(length < " + FileBlockSize + " bytes) and End With A Line {" + ENDMARK + "}: ");
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null && !line.equals(ENDMARK)){
                sb.append(line).append("\n");
            }
            String content = sb.toString();
            result = content.substring(0, content.lastIndexOf("\n")).getBytes();
//            System.out.println("Your input is: \n" + content.substring(0, content.lastIndexOf("\n")));
            if (result.length > FileBlockSize){
                byte[] newContent = new byte[FileBlockSize];
                System.arraycopy(result, 0, newContent, 0, FileBlockSize);
                result = newContent;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }
}
