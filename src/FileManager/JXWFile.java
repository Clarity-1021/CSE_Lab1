package FileManager;

import BlockManager.Block;
import BlockManager.JXWBlock;
import BlockManager.JXWBlockId;
import ErrorManager.ErrorLog;
import Id.Id;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JXWFile implements File {
    private static final String root = "./output/FileManagers/";//Block文件输出的默认根目录

    /**
     * File中Block的统一BlockSize
     */
    private static final int FileBlockSize = 8;//每个Block的大小

    private static final int copySize = 2;//副本个数

    /**
     * File的Id
     */
    private Id FileId;

    /**
     * File的FileManager
     */
    private FileManager FileManager;

    private String FileMetaPath;

    /**
     * File游标的当前位置
     */
    private long FileCurr = 0;

    /**
     * File的开始位置
     */
    private long FileStart = 0;

    /**
     * File的结束位置的后一个位置
     */
    private long FileEnd = 0;

    private List<List<Block>> FileBlockLists;

    private byte[] FileBuffer = null;

    private boolean isDirty = false;//缓冲区里的数据有没有脏

    /**
     * 创建FileId为fileId的File
     * @param fileManager 这个File的FileManager
     * @param fileId FileId
     */
    public JXWFile(FileManager fileManager, Id fileId){
        FileManager = fileManager;
        FileId = fileId;
        FileMetaPath = root + fileManager.getFileManagerName() + "/" + FileId.getName() + ".meta";

        //如果File已经存在，通过FileMeta获得FileBlockLists和FileEnd
        if ((new java.io.File(FileMetaPath)).exists()) {
            readMetaGetInfo();
        }
        else {//如果File不存在，写入FileMeta
            FileBlockLists = new ArrayList<>();
            upDateFileMeta();
        }
    }

    /**
     * 创建FileId为fileIdTo的File，FileMeta和fileIdFrom的一致
     * @param fileManager 这个File的FileManager
     * @param fileIdFrom 被Copy的File的Id
     * @param fileIdTo 副本File的Id
     */
    public JXWFile(FileManager fileManager, Id fileIdFrom, Id fileIdTo){
        FileManager = fileManager;
        FileId = fileIdTo;
        FileMetaPath = root + fileManager.getFileManagerName() + "/" + fileIdFrom.getName() + ".meta";
        //如果FileFrom已经存在，通过FileFrom的FileMeta获得FileBlockLists和FileEnd
        readMetaGetInfo();
        FileMetaPath = root + fileManager.getFileManagerName() + "/" + FileId.getName() + ".meta";
        //更新FileTo的FileMeta
        upDateFileMeta();
    }

    /**
     * 获取File的meta文件的地址
     * @return FileMeta的地址
     */
    public String getFileMetaPath(){
        return FileMetaPath;
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
     * 通过Block的副本List获得这个BlockContent的长度
     * @param blockList Block的副本List
     * @return BlockContent的长度
     */
    private int getBlockContentSize(List<Block> blockList){
        return blockList.get(0).blockSize();
    }

    /**
     * 通过Block的副本List获得这个Block的Data
     * @param blockList Block的副本List
     * @return BlockData
     */
    private byte[] getBlockContent(List<Block> blockList){
        byte[] result = new byte[0];

        for (Block block : blockList) {//顺序查找Block的每一个副本
            if (!block.isBlockDamaged()){//这个副本的Data没有损坏
                result = block.read();
                break;
            }
            else {//此副本被损坏,从副本List中移除，并更新FileMeta文件
                ErrorLog.logErrorMessage(FileManager.getFileManagerName() + " " + FileId.getName() + " " + block.getBlockManager().getBlockManagerName() + " " + block.getIndexId().getName() + " is damaged.");
                blockList.remove(block);
                upDateFileMeta();//更新FileMeta
            }
        }

        return result;
    }

    /**
     * 获得FileCurr游标当前位置所在的Block的Index和在当前Block中的offset
     * @return List.get(0)是BlockIndex，List.get(1)是offset
     */
    public List<Integer> getFileCurrBlockPos(){
        long blockCurr = 0;
        long startPosInFileCurrBlock = FileCurr;
        int blockIndex = 0;

        for (List<Block> blockList : FileBlockLists){//顺序查找每一个Block的副本List，获得FileCurr所在的Block的Index和再这个Block中的offset
            if (FileCurr >= blockCurr){
                int currentBlockContentSize = getBlockContentSize(blockList);
                if (startPosInFileCurrBlock < currentBlockContentSize){
                    break;
                }
                blockCurr += currentBlockContentSize;
                startPosInFileCurrBlock = FileCurr - blockCurr;
                blockIndex++;
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(blockIndex);
        result.add((int)startPosInFileCurrBlock);

        return result;
    }

    private void loadFile(){
        if (FileBuffer == null){
            FileBuffer = new byte[(int)FileEnd];//Todo 先不考虑溢出
            int bufferPos = 0;
            for (List<Block> blockList : FileBlockLists){
                byte[] content = getBlockContent(blockList);
                int copyLength = content.length;
                System.arraycopy(content, 0, FileBuffer, bufferPos, copyLength);
                bufferPos += copyLength;
            }
        }
    }

    /**
     * 从文件的开始位置开始读到结束位置，读最多length长度的文件内容
     * @param length 长度限制
     * @return 最多length长度的文件内容
     */
    @Override
    public byte[] read(int length) {
        if (length == 0) {
            return new byte[0];
        }

        length = (int)Math.min(FileEnd - FileCurr, length);//length是int,取小总是int
        byte[] result = new byte[length];

        int blockIndex = (int)(FileCurr / FileBlockSize);
        int copyStartPos = (int)(FileCurr % FileBlockSize);

        int resultCurr = 0;
        while (resultCurr < length){
            List<Block> blockList = FileBlockLists.get(blockIndex++);

            byte[] blockContent = getBlockContent(blockList);
            int copyLength = Math.min(blockContent.length - copyStartPos, length - resultCurr);
            System.arraycopy(blockContent, copyStartPos, result, resultCurr, copyLength);
            resultCurr += copyLength;
            if (copyStartPos != 0) copyStartPos = 0;
        }

        return result;
    }

    @Override
    public byte[] bufferedRead(int length) {
        if (length == 0) {
            return new byte[0];
        }

        loadFile();//检查是否初次读写，初次读写对File进行缓存

        length = (int)Math.min(FileBuffer.length - FileCurr, length);//length是int,取小总是int
        byte[] result = new byte[length];
        System.arraycopy(FileBuffer, (int)FileCurr, result, 0, length);

        return result;
    }

    /**
     * 在文件游标的当前位置插入byte数组b
     * @param b 需要被写入文件的数据
     */
    @Override
    public void write(byte[] b) {
        int bLength = b.length;
        if (bLength == 0) return;

        List<List<Block>> newBlockLists = new ArrayList<>();

        int fileCurrBlockIndex = (int)(FileCurr / FileBlockSize);
        int fileCurrBlockCurrPos = (int)(FileCurr % FileBlockSize);

        for (int i = 0; i < fileCurrBlockIndex; i++) {//FileCurr所在当前Block之前的BlockList保持不变
            newBlockLists.add(FileBlockLists.get(i));
        }

        byte[] contentToWrite = new byte[(int)(FileEnd + bLength - fileCurrBlockIndex * FileBlockSize)];
        int copyPos = 0;
        if (fileCurrBlockCurrPos > 0) {
            byte[] fileCurrBlockContent = getBlockContent(FileBlockLists.get(fileCurrBlockIndex));
            System.arraycopy(fileCurrBlockContent, 0, contentToWrite, copyPos, fileCurrBlockCurrPos);
            copyPos += fileCurrBlockCurrPos;
        }
        System.arraycopy(b, 0, contentToWrite, copyPos, bLength);
        copyPos += bLength;
        if (FileCurr < FileEnd) {
            System.arraycopy(read((int)(FileEnd - FileCurr)), 0, contentToWrite, copyPos, (int)(FileEnd - FileCurr));
        }

        int writeLenrgh = contentToWrite.length;

        //更新后面的块儿
        copyPos = 0;
        int completeBlockCount = (int)((writeLenrgh - copyPos) / FileBlockSize);
        int remainBlockLength = (int)((writeLenrgh - copyPos) % FileBlockSize);

        for (int i = 0; i < completeBlockCount; i++) {
            byte[] content = new byte[FileBlockSize];
            System.arraycopy(contentToWrite, copyPos, content, 0, FileBlockSize);
            copyPos += FileBlockSize;

            newBlockLists.add(newBlockList(content));
        }

        if (remainBlockLength > 0) {
            byte[] content = new byte[remainBlockLength];
            System.arraycopy(contentToWrite, copyPos, content, 0, remainBlockLength);

            newBlockLists.add(newBlockList(content));
        }

        FileBlockLists = newBlockLists;
        FileEnd += bLength;
        //更新FileMeta
        upDateFileMeta();
    }

    /**
     * 更新FileBuffer
     * @param b 插入到FileCurr后的内容
     */
    @Override
    public void bufferedWrite(byte[] b) {
        if (b.length == 0) {
            return;
        }
        loadFile();

        int newLength = b.length + FileBuffer.length;
        byte[] newContent = new byte[newLength];
        int copyPos = 0;
        if ((int)FileCurr != 0){
            System.arraycopy(FileBuffer, 0, newContent, copyPos, (int)(FileCurr));
            copyPos += (int)(FileCurr);
        }

        System.arraycopy(b, 0, newContent, copyPos, b.length);
        copyPos += b.length;

        if (FileCurr < FileBuffer.length) {
            System.arraycopy(FileBuffer, (int)(FileCurr), newContent, copyPos, (int)(FileBuffer.length - FileCurr));
        }

        FileBuffer = newContent;
        if (!isDirty) {
            isDirty = true;
        }
    }

    /**
     * 获得有备份个数个的内容是content的Block的List
     * @param content Block中的内容
     * @return 备份个数个的Block的BlockList
     */
    private List<Block> newBlockList(byte[] content) {
        List<Block> blockList = new ArrayList<>();
        for (int i = 0 ; i < copySize; i++) {
            blockList.add(((JXWFileManager)FileManager).getRandomBlockManager().newBlock(content));
        }
        return blockList;
    }

    /**
     * 更新FileMeta信息
     */
    public void upDateFileMeta(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFileMetaPath()));
            bw.write("size: " + FileEnd + "\n");
            bw.write("block size: " + FileBlockSize + "\n");
            bw.write("logic size:\n");
            for (List<Block> blockList : FileBlockLists) {
                for (Block block : blockList) {
                    bw.write(block.getBlockManager().getBlockManagerName() + "," + block.getIndexId().getName() + " ");
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读FileMeta获得FileBlockLists个FileEnd
    private void readMetaGetInfo() {
        FileBlockLists = new ArrayList<>();
        try {
            FileReader fr = new FileReader(FileMetaPath);
            BufferedReader br = new BufferedReader(fr);

            String line =  br.readLine();//读第一行获得FileSize
            String[] args = line.split(" ");
            FileEnd = Long.parseLong(args[1]);

            for (int i = 0; i < 2; i++){//跳过两行
                br.readLine();
            }

            while ((line = br.readLine()) != null){//如果非空，读FileBlockList
                String[] cols = line.split(" ");
                List<Block> blockList = new ArrayList<>();
                for (String bmBlock : cols){
                    args = bmBlock.split(",");
                    blockList.add(new JXWBlock(args[0], new JXWBlockId(args[1])));
                }
                FileBlockLists.add(blockList);
            }
            br.close();
            fr.close();
        }
        catch (IOException e){
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
                break;
            case MOVE_HEAD:
                result = FileStart + offset;
                result = getValidPosition(result);
                break;
            case MOVE_TAIL:
                if (FileBuffer != null) {
                    result = FileBuffer.length + offset;
                }
                else {
                    result = FileEnd + offset;
                }
                result = getValidPosition(result);
                break;
        }

        FileCurr = result;
        return FileCurr;
    }

    /**
     * 确保该位置必须在旧的文件开始和结束位置之间
     * @param pos 现在的位置
     * @return 合法的位置
     */
    private long getValidPosition(long pos) {
        if (pos < FileStart)
            pos = FileStart;
        else if (FileBuffer != null) {
            if (pos > FileBuffer.length) {
                pos = FileBuffer.length;
            }
        }
        else if (pos > FileEnd){
            pos = FileEnd;
        }

        return pos;
    }

    /**
     * 如果Buffer中的数据脏了，写回并更新FileMeta
     */
    @Override
    public void close() {
        if (isDirty) {
            if (FileBuffer.length != 0) {
                FileEnd = FileBuffer.length;

                FileBlockLists = new ArrayList<>();
                int copyPos = 0;
                int completeBlockCount = (int)((FileEnd - copyPos) / FileBlockSize);
                int remainBlockLength = (int)((FileEnd - copyPos) % FileBlockSize);

                for (int i = 0; i < completeBlockCount; i++) {
                    byte[] content = new byte[FileBlockSize];
                    System.arraycopy(FileBuffer, copyPos, content, 0, FileBlockSize);
                    copyPos += FileBlockSize;

                    FileBlockLists.add(newBlockList(content));
                }

                if (remainBlockLength > 0) {
                    byte[] content = new byte[remainBlockLength];
                    System.arraycopy(FileBuffer, copyPos, content, 0, remainBlockLength);

                    FileBlockLists.add(newBlockList(content));
                }
            }
            else {
                FileEnd = 0;
                FileBlockLists = new ArrayList<>();
            }
            FileBuffer = null;
            upDateFileMeta();
        }
    }

    @Override
    public long size() {
        return FileEnd;
    }

//    如果size⼤于原来的file size，那么新增的字节应该全为0x00
//    如果size⼩于原来的file size，注意修改file meta中对应的logic block，且被删除的数据不应该能够再次被访问
    //需要关闭文件才能保存更改
    @Override
    public void setSize(long newSize) {
        loadFile();
        if (newSize > FileBuffer.length) {//大于原大小，补零
            byte[] newContent = new byte[(int)newSize];
            System.arraycopy(FileBuffer, 0, newContent, 0, FileBuffer.length);
            FileBuffer = newContent;

            isDirty = true;
        }
        else if (newSize < FileBuffer.length){//小于原大小，截断
            byte[] newContent = new byte[(int)newSize];
            System.arraycopy(FileBuffer, 0, newContent, 0, (int)newSize);
            FileBuffer = newContent;

            isDirty = true;
        }
    }
}