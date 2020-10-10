package FileManager;

import BlockManager.Block;
import Id.Id;

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

    JXWFile(){

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
        byte[] resule = new byte[(int) size()];
        return new byte[0];
    }

    @Override
    public void write(byte[] b) {

    }

    @Override
    public long move(long offset, int where) {
        return 0;
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
        for (List BlockList : FileBlockLists){
            for (Object Block : BlockList){
                Block block = (Block)Block;
                if (getMD5Code(block.getBlockContentSize()) == )
            }

            size += block.getBlockContentSize();
        }
        return size;
    }

    @Override
    public void setSize(long newSize) {

    }
}
