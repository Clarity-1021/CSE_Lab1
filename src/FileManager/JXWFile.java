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
    private static String root = "./output/FileManagers/";//Block文件输出的默认根目录

    /**
     * File中Block的统一BlockSize
     */
    private static int FileBlockSize = 1024;

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
     * File的结束位置
     */
    private long FileEnd = 0;

    private List<List<Block>> FileBlockLists;

    /**
     * 创建FileId为fileId的File
     * @param fileId File的Id
     */
    JXWFile(FileManager fileManager, Id fileId){
        FileManager = fileManager;
        FileId = fileId;
        FileMetaPath = root + "FM-" + fileManager.getFileManagerName() + "/" + FileId.getName() + ".meta";

        FileBlockLists = readMateGetFileBlockLists();
        //创建空的meta文件
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

    /**
     * 从文件的开始位置开始读到结束位置，读最多length长度的文件内容
     * @param length 长度限制
     * @return 最多length长度的文件内容
     */
    @Override
    public byte[] read(int length) {
        length = (int)Math.min(FileEnd - FileCurr, length);//length是int,取小总是int
        byte[] result = new byte[length];

        List<Integer> fileCurrBlockPos = getFileCurrBlockPos();

        int blockIndex = getFileCurrBlockPos().get(0);
        int copyStartPos = fileCurrBlockPos.get(1);
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

    /**
     * 在文件游标的当前位置插入byte数组b
     * @param b 需要被写入文件的数据
     */
    @Override
    public void write(byte[] b) {
        int bLength = b.length;

        List<Integer> fileCurrBlockPos = getFileCurrBlockPos();

        int fileCurrBlockIndex = getFileCurrBlockPos().get(0);
        int fileCurrBlockCurrPos = fileCurrBlockPos.get(1);

        int bPos = 0;
        List<List<Block>> BlockListsToInsert = new ArrayList<>();
        if (fileCurrBlockCurrPos == 0){//写入的位置在Block的头上，直接创建新的Block插入原FileBlock的List中
            while (bPos < bLength){
                int writeLength = Math.min(FileBlockSize, bLength - bPos);
                byte[] contentToWrite = new byte[writeLength];
                System.arraycopy(b, bPos, contentToWrite, 0, writeLength);
                bPos += writeLength;

                List<Block> BlockListToInsert = new ArrayList<>();
                BlockListToInsert.add(newBlock(contentToWrite));
                BlockListsToInsert.add(BlockListToInsert);
            }
        }
        else {//从FileCurr所在的Block开始向后写
            List<Block> fileCurrBlock = FileBlockLists.get(fileCurrBlockIndex);
            int fileCurrBlockContentSize = getBlockContentSize(fileCurrBlock);
            int fileCurrBlockRemainLength = fileCurrBlockContentSize - fileCurrBlockCurrPos;

            byte[] fileCurrBlockContent = getBlockContent(fileCurrBlock);
            int insertLength = fileCurrBlockContentSize + bLength;
            if (insertLength <= FileBlockSize){//如果FileCurr所在Block中的Data和要写入的数据的大小小于当前块的大小，只用用一个新块儿替换
                byte[] contentToWrite = new byte[insertLength];
                System.arraycopy(fileCurrBlockContent, 0, contentToWrite, 0, fileCurrBlockCurrPos);
                System.arraycopy(b, bPos, contentToWrite, fileCurrBlockCurrPos, bLength);
                System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, fileCurrBlockCurrPos + bLength, fileCurrBlockRemainLength);

                List<Block> BlockListToInsert = new ArrayList<>();
                BlockListToInsert.add(newBlock(contentToWrite));
                BlockListsToInsert.add(BlockListToInsert);
            }
            else {
                byte[] contentToWrite = new byte[FileBlockSize];
                List<Block> BlockListToInsert = new ArrayList<>();

                if (fileCurrBlockCurrPos + bLength <= FileBlockSize){//FileCurr所在Block被切分的后半部分被分割
                    System.arraycopy(fileCurrBlockContent, 0, contentToWrite, 0, fileCurrBlockCurrPos);
                    System.arraycopy(b, bPos, contentToWrite, fileCurrBlockCurrPos, bLength);
                    if (fileCurrBlockCurrPos + bLength < FileBlockSize){
                        System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, fileCurrBlockCurrPos + bLength, fileCurrBlockRemainLength);
                        fileCurrBlockCurrPos += FileBlockSize - (bLength + fileCurrBlockCurrPos);
                        fileCurrBlockRemainLength = fileCurrBlockContentSize - fileCurrBlockCurrPos;
                    }
                    BlockListToInsert.add(newBlock(contentToWrite));
                    BlockListsToInsert.add(BlockListToInsert);

                    contentToWrite = new byte[fileCurrBlockRemainLength];
                    System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, 0, fileCurrBlockRemainLength);

                    BlockListToInsert = new ArrayList<>();
                    BlockListToInsert.add(newBlock(contentToWrite));
                    BlockListsToInsert.add(BlockListToInsert);
                }
                else {//需要写入的部分被分割
                    System.arraycopy(fileCurrBlockContent, 0, contentToWrite, 0, fileCurrBlockCurrPos);
                    System.arraycopy(b, bPos, contentToWrite, fileCurrBlockCurrPos, FileBlockSize - fileCurrBlockCurrPos);
                    bPos += FileBlockSize - fileCurrBlockCurrPos;

                    BlockListToInsert.add(newBlock(contentToWrite));
                    BlockListsToInsert.add(BlockListToInsert);

                    int completeNewBlockOfB = (bLength - bPos) / FileBlockSize;
                    int remainLengthOfB = (bLength - bPos) % FileBlockSize;
                    for (int i = 0; i < completeNewBlockOfB; i++){
                        contentToWrite = new byte[FileBlockSize];
                        System.arraycopy(b, bPos, contentToWrite, 0, FileBlockSize);
                        bPos += FileBlockSize;

                        BlockListToInsert.add(newBlock(contentToWrite));
                        BlockListsToInsert.add(BlockListToInsert);
                    }

                    if (remainLengthOfB == 0){//没有需要写入的部分了，把FurrCurr的后半部分插入即可
                        contentToWrite = new byte[fileCurrBlockRemainLength];
                        System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, 0, fileCurrBlockRemainLength);

                        BlockListToInsert.add(newBlock(contentToWrite));
                        BlockListsToInsert.add(BlockListToInsert);
                    }
                    else {
                        int remainLengthToWrite = remainLengthOfB + fileCurrBlockRemainLength;
                        if (remainLengthToWrite <= FileBlockSize){
                            contentToWrite = new byte[remainLengthToWrite];
                            System.arraycopy(b, bPos, contentToWrite, 0, remainLengthOfB);
                            System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, remainLengthOfB, fileCurrBlockRemainLength);

                            BlockListToInsert.add(newBlock(contentToWrite));
                            BlockListsToInsert.add(BlockListToInsert);
                        }
                        else {
                            contentToWrite = new byte[FileBlockSize];
                            System.arraycopy(b, bPos, contentToWrite, 0, remainLengthOfB);
                            System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, remainLengthOfB, fileCurrBlockRemainLength);
                            fileCurrBlockCurrPos += (FileBlockSize - remainLengthOfB);
                            fileCurrBlockRemainLength = fileCurrBlockContentSize - fileCurrBlockCurrPos;

                            BlockListToInsert.add(newBlock(contentToWrite));
                            BlockListsToInsert.add(BlockListToInsert);

                            contentToWrite = new byte[fileCurrBlockRemainLength];
                            System.arraycopy(fileCurrBlockContent, fileCurrBlockCurrPos, contentToWrite, 0, fileCurrBlockRemainLength);

                            BlockListToInsert.add(newBlock(contentToWrite));
                            BlockListsToInsert.add(BlockListToInsert);
                        }
                    }
                }
            }
        }

        FileBlockLists.addAll(fileCurrBlockIndex + 1, BlockListsToInsert);
        FileBlockLists.remove(fileCurrBlockIndex);

        upDateFileMeta();
    }

    public Block newBlock(byte[] content){
        return ((JXWFileManager)FileManager).getRandomBlockManager().newBlock(content);
    }

    /**
     * 获得文件的大小，同时更新FileEnd游标
     * @return File的大小
     */
    public long getFileSize(){
        FileEnd = 0;
        for (List<Block> blockList : FileBlockLists){
            FileEnd += getBlockContentSize(blockList);
        }

        return FileEnd;
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
                for (Block block : blockList) {
                    bw.write("{" + block.getBlockManager().getBlockManagerName() + "," + block.getIndexId().getName() + "}");
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<List<Block>> readMateGetFileBlockLists(){
        List<List<Block>> Lists = new ArrayList<>();

        java.io.File file = new java.io.File(FileMetaPath);
        if (!file.exists()){
            return Lists;
        }

        try {
            FileReader fr = new FileReader(FileMetaPath);
            BufferedReader br = new BufferedReader(new FileReader(FileMetaPath));
            for (int i = 0; i < 3; i++){
                br.readLine();
            }

            String line = null;
            while ((line = br.readLine()) != null){
                String[] cols = line.split("{");

                List<Block> blockList = new ArrayList<>();
                blockList.add(new JXWBlock())
            }
            br.close();
            fr.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return result;
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

        FileCurr = result;
        return FileCurr;
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

    @Override
    public long size() {
        return -1;
    }

    @Override
    public void setSize(long newSize) {

    }
}