package FileManager;

import BlockManager.Block;
import ErrorManager.ErrorLog;
import Id.Id;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * File中Block的统一BlockSize
     */
    private int FileBlockSize = 0;

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
        upDateFileMeta();
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

    /**
     * 从文件的开始位置开始读到结束位置，读最多length长度的文件内容
     * @param length 长度限制
     * @return 最多length长度的文件内容
     */
    @Override
    public byte[] read(int length) {
        length = (int) Math.min(getFileSize(), length);//length是int,取小总是int
        byte[] result = new byte[length];

        int currentPos = 0;
        int currentBlockPos = (int)(FileStart % FileBlockSize);
        int nextLengthToRead = Math.min(length - currentPos, FileBlockSize) - currentBlockPos;

        for (List<Block> blockList : FileBlockLists) {
            if (nextLengthToRead == 0) {//不需要继续读
                break;
            }

            //还需要继续读nextLengthToRead个字节，查找下一个块
            for (Object block : blockList) {//顺序查找副本
                if (((Block) block).getBlockContentSize() != -1){//这个副本的Data没有损坏
                    System.arraycopy(((Block) block).read(), currentBlockPos, result, currentPos, nextLengthToRead);
                    currentBlockPos = (currentBlockPos + nextLengthToRead) % FileBlockSize;
                    currentPos += nextLengthToRead;
                    nextLengthToRead = Math.min(length - currentPos, FileBlockSize) - currentBlockPos;
                    break;
                }
                else {//此副本被损坏,从副本List中移除，并更新FileMeta文件
                    ErrorLog.logErrorMessage("FM-" + FileId.getManagerNum() + " F-" + FileId.getNum() + " BM-" + ((Block) block).getIndexId().getManagerNum() + " B-" + ((Block) block).getIndexId().getNum() + " is damaged.");
                    blockList.remove(block);
                    upDateFileMeta();//更新FileMeta
                }
            }
        }

        return result;
    }

    /**
     * 在文件游标的当前位置插入byte数组b
     * @param b 需要被写入文件的数据
     */
    @Override
    public void write(byte[] b) {
        List<List<Block>> newBlockLists = new ArrayList<>();

        int length = b.length;




        int currentPos = 0;
        int blockCopySize = (int)(FileCurr / FileBlockSize);//当前游标所在块儿之前的需要复制的完整的Block的个数
        for (int i = 0; i < blockCopySize; i++){
            List<Block> blockList = FileBlockLists.get(i);
            newBlockLists.add(blockList);

            byte[] contentToWrite = new byte[FileBlockSize];
            //需要创建新副本
            for (Object block : blockList) {//顺序查找副本
                if (((Block) block).getBlockContentSize() != -1){//这个副本的Data没有损坏
                    System.arraycopy(((Block)block).read(), 0, contentToWrite, 0, FileBlockSize);
                    currentPos += FileBlockSize;
                    break;
                }
                else {//此副本被损坏,从副本List中移除
                    ErrorLog.logErrorMessage("FM-" + FileId.getManagerNum() + " F-" + FileId.getNum() + " BM-" + ((Block) block).getIndexId().getManagerNum() + " B-" + ((Block) block).getIndexId().getNum() + " is damaged.");
                    blockList.remove(block);
                }
            }

            blockList.add(newBlock(contentToWrite));
        }

        int currentBlockIndex = blockCopySize;
        int currentBlockPos = 0;
        int nextLengthToWrite = Math.min((int)(FileCurr % FileBlockSize + length), FileBlockSize);
        int bPos = 0;
        int remainLengthBeforeFileCurr = (int)(FileCurr % FileBlockSize);
        int nextLengthToCopyInB = remainLengthBeforeFileCurr + length > FileBlockSize ? FileBlockSize - remainLengthBeforeFileCurr : length;

        List<Block> oldBlockList = FileBlockLists.get(blockCopySize);
        List<Block> blockList = new ArrayList<>();
        newBlockLists.add(blockList);

        byte[] contentToWrite = new byte[nextLengthToCopyInB];
        for (Object block : oldBlockList) {//顺序查找副本
            if (((Block) block).getBlockContentSize() != -1){//这个副本的Data没有损坏
                System.arraycopy(((Block)block).read(), 0, contentToWrite, 0, remainLengthBeforeFileCurr);
                currentPos += remainLengthBeforeFileCurr;
                break;
            }
            else {//此副本被损坏,从副本List中移除
                ErrorLog.logErrorMessage("FM-" + FileId.getManagerNum() + " F-" + FileId.getNum() + " BM-" + ((Block) block).getIndexId().getManagerNum() + " B-" + ((Block) block).getIndexId().getNum() + " is damaged.");
                blockList.remove(block);
            }
        }

        System.arraycopy(b, bPos, contentToWrite, remainLengthBeforeFileCurr, nextLengthToCopyInB);
        bPos += nextLengthToCopyInB;
        blockList.add(newBlock(contentToWrite));
        int newBlockSize = (length - nextLengthToCopyInB) / FileBlockSize;//b中可新建完整Block的个数

        for (int i = 0; i < newBlockSize; i++){
            List<Block> newBlockList = new ArrayList<>();
            newBlockLists.add(newBlockList);

            byte[] newContentToWrite = new byte[FileBlockSize];
            System.arraycopy(b, bPos, newContentToWrite, 0, FileBlockSize);
            bPos += FileBlockSize;
            newBlockList.add(newBlock(newContentToWrite));
        }

        int remainLengthBeforeBLength = length - bPos;
        blockList = new ArrayList<>();

    }

    public Block newBlock(byte[] content){
        return ((JXWFileManager)FileManager).getRandomBlockManager().newBlock(content);
    }

    public long getFileSize(){
        return FileEnd - FileStart;
    }

    /**
     * 更新FileMeta信息
     */
    public void upDateFileMeta(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFileMetaPath()));
            bw.write("size: " + getFileSize() + "\n");
            bw.write("block size: " + FileBlockSize + "\n");
            bw.write("logic size:\n");
            for (List<Block> blockList : FileBlockLists) {
                for (Object block : blockList) {
                    bw.write("{" + ((Block)block).getIndexId().getManagerNum() + "," + ((Block)block).getIndexId().getNum() + "}");
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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