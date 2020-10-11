package FileManager;

import BlockManager.Block;
import BlockManager.BlockManager;
import Id.Id;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static SmartTools.EnCode.getMD5Code;

public class JXWFile implements File {
    /**
     * File的Id
     */
    private Id FileId;

    /**
     * File的FileManager
     */
    private FileManager FileManager;

    /**
     * File对应的Block的个数
     */
    private int FileBlockSize;

    /**
     * File的数据所存储的Block，按顺序
     */
    private List<List<Block>> FileBlockLists = new ArrayList<>();

    /**
     * File游标的当前位置
     */
    private long FileCurr = 0;

    /**
     * File的开始位置
     */
    private long FileStart = 0;

    /**
     * File的结束位置
     */
    private long FileEnd = 0;


    /**
     * 创建FileId为fileId的File
     * @param fileId File的Id
     */
    JXWFile(Id fileId){
        FileId = fileId;
        FileManager = ((JXWFileId)fileId).getFileManager();

        //创建空的meta文件
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFileMetaPath()));
            bw.write("");//不管此文件是否存在都清空
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取File的meta文件的地址
     * @return FileMeta的地址
     */
    public String getFileMetaPath(){
        return FileId.getMetaPath();
    }

    @Override
    public Id getFileId() {
        return FileId;
    }

    @Override
    public FileManager getFileManager() {
        return FileManager;
    }

    @Override
    public byte[] read(int length) {
        return new byte[0];
    }

    /**
     * 在文件游标的当前位置插入byte数组b
     * @param b 需要被写入文件的数据
     */
    @Override
    public void write(byte[] b) {

    }

    /**
     * 把File内保有的三个指针之一where移动offset个偏移量，返回移动后的位置
     * @param offset 偏移量
     * @param where 需要移动的指针
     * @return 如果where输入的正确返回新位置，where不为三个指针之一返回-1
     */
    @Override
    public long move(long offset, int where) {//Todo 没有考虑溢出long的情况，可以写一个日志记录一下溢出
        long result = -1;
        switch (where){
            case MOVE_CURR:
                result = FileCurr + offset;
                result = getValidPosition(result);
                FileCurr = result;
                break;
            case MOVE_HEAD:
                result = FileStart + offset;
                result = getValidPosition(result);
                FileStart = result;
                break;
            case MOVE_TAIL:
                result = FileEnd + offset;
                result = getValidPosition(result);
                FileEnd = result;
                break;
        }
        return result;
    }

    /**
     * 确保该位置必须在旧的文件开始和结束位置之间
     * @param pos 现在的位置
     * @return 合法的位置
     */
    private long getValidPosition(long pos) {
        if (pos > FileEnd)
            pos = FileEnd;
        else if (pos < FileStart)
            pos = FileStart;
        return pos;
    }


//Buffer需实现以下

    @Override
    public void close() {

    }

    /**
     * 获得File的大小
     * @return File中存数据的Block中data的长度之和
     */
    @Override
    public long size() {
        long size = 0;
        for (List blockList : FileBlockLists){
            for (Object block : blockList){
                int blockDataSize = ((Block)block).getBlockContentSize();
                if (blockDataSize != -1){//这个副本没有被损坏
                    size += blockDataSize;
                    break;
                }
            }
        }
        return size;
    }

    @Override
    public void setSize(long newSize) {

    }
}


/**
 * 读最多length长度的文件内容
 * @param length 长度限制
 * @return 最多length长度的文件内容
 */