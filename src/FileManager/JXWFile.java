package FileManager;

import BlockManager.Block;
import BlockManager.JXWBlock;
import BlockManager.JXWBlockId;
import ErrorManager.ErrorCode;
import Id.Id;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JXWFile implements File {

    public static final int GET_MODE = 0;
    public static final int NEW_MODE = 1;

    private static final String root = "./output/FileManagers/";//Block文件输出的默认根目录
    private static final int OVERFLOW_BOUND = Integer.MAX_VALUE;//文件大小溢出的长度，文件大小不能超过int的最大值

    /**
     * File中Block的统一BlockSize
     */
    private int FileBlockSize = 3;//每个Block的大小

    /**
     * LogicBlock的副本数目
     */
    private static final int copySize = 2;//副本个数

    /**
     * File的Id
     */
    private Id FileId;

    /**
     * File的FileManager
     */
    private FileManager FileManager;

    /**
     * FileMeta的路径
     */
    private String FileMetaPath;

    /**
     * File游标的当前位置
     */
    private long FileCurr = 0;

    /**
     * File的开始位置
     */
    private static final long FileStart = 0;

    /**
     * File的结束位置的后一个位置，即文件大小
     */
    private long FileEnd = 0;

    private List<Set<Block>> FileBlockLists;

    private byte[] FileBuffer = null;

    private boolean isDirty = false;//缓冲区里的数据有没有脏

    /**
     * 创建FileId为fileId的File
     * @param fileManager 这个File的FileManager
     * @param fileId FileId
     */
    public JXWFile(FileManager fileManager, Id fileId, int MODE) throws ErrorCode {
        FileManager = fileManager;
        FileId = fileId;
        FileMetaPath = root + fileManager.getFileManagerName() + "/" + FileId.getName() + ".meta";

        switch (MODE) {
            case NEW_MODE:
                //如果File已经存在抛出异常
                if ((new java.io.File(FileMetaPath)).exists()) {
                    System.out.println("[INFO] the file you want to create has been existing");
                    throw new ErrorCode(ErrorCode.FILE_EXIST);
                }
                else {//如果File不存在，写入FileMeta
                    FileBlockLists = new ArrayList<>();//LogicBlock置空
                    upDateFileMeta();
                }
                break;
            case GET_MODE:
                //如果File已经存在，通过FileMeta获得FileBlockLists和FileEnd
                if ((new java.io.File(FileMetaPath)).exists()) {
                    readMetaGetInfo();
                }
                else {//如果File不存在，抛出异常
                    System.out.println("[INFO] the file you want to get does not exist");
                    throw new ErrorCode(ErrorCode.FILE_NOT_EXIST);
                }
                break;
        }
    }

    /**
     * 创建FileId为fileIdTo的File，FileMeta和fileIdFrom的一致
     * @param fileManagerFrom 被Copy的File的FileManager
     * @param fileIdFrom 被Copy的File的Id
     * @param fileManagerTo 副本File的FileManager
     * @param fileIdTo 副本File的Id
     */
    public JXWFile(FileManager fileManagerFrom, Id fileIdFrom, FileManager fileManagerTo, Id fileIdTo) throws ErrorCode{
        FileManager = fileManagerTo;
        FileId = fileIdTo;
        FileMetaPath = root + fileManagerFrom.getFileManagerName() + "/" + fileIdFrom.getName() + ".meta";

        //如果FileFrom存在，通过FileFrom的FileMeta获得FileBlockLists和FileEnd
        if ((new java.io.File(FileMetaPath)).exists()) {
            readMetaGetInfo();

            FileMetaPath = root + fileManagerTo.getFileManagerName() + "/" + FileId.getName() + ".meta";

            //如果FileTo已经存在，抛出异常
            if ((new java.io.File(FileMetaPath)).exists()) {
                System.out.println("[INFO] the file you want to copy to has been existing");
                throw new ErrorCode(ErrorCode.FILE_EXIST);
            }
            else {//如果FileTo不存在，写入FileTo的FileMeta
                upDateFileMeta();
            }
        }
        else {//如果FileFrom不存在，抛出异常
            System.out.println("[INFO] the file you want to copy from does not exist");
            throw new ErrorCode(ErrorCode.FILE_NOT_EXIST);
        }
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
     * @param blockSet Block的副本List
     * @return BlockContent的长度
     */
    private int getBlockContentSize(Set<Block> blockSet){
        int result = 0;
        for (Block block : blockSet) {
            result = block.blockSize();
            break;
        }
        return result;
    }

    /**
     * 通过Block的副本List获得这个Block的Data
     * @param blockSet Block的副本List
     * @return BlockData
     */
    private byte[] getBlockContent(Set<Block> blockSet) throws ErrorCode{
        byte[] result = new byte[0];

        if (blockSet.isEmpty()) {
            throw new ErrorCode(ErrorCode.LOGIC_BLOCK_EMPTY);
        }

        Set<Block> damagedBlocks = new HashSet<>();

        for (Block block : blockSet) {//顺序查找Block的每一个副本
            try {
                if (!block.isBlockDamaged()){//这个副本的Data没有损坏
                    result = block.read();
                    break;
                }
                else {//此副本被损坏,从副本List中移除
                    damagedBlocks.add(block);
                    throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
                }
            }
            catch (ErrorCode e) {
                e.printStackTrace();
            }
        }

        if (!damagedBlocks.isEmpty()) {//有Block被损坏，删去被损坏的Block并更新FileMeta
            blockSet.removeAll(damagedBlocks);
            upDateFileMeta();
        }

        if (blockSet.isEmpty()) {
            System.out.println("[INFO] the file has been damaged and you cannot attach to it anymore");
            throw new ErrorCode(ErrorCode.LOGIC_BLOCK_EMPTY);
        }

        return result;
    }

    private void loadFile() throws ErrorCode{
        if (FileBuffer == null){
            FileBuffer = new byte[(int)FileEnd];
            int bufferPos = 0;
            for (Set<Block> blockSet : FileBlockLists){
                try {
                    byte[] content = getBlockContent(blockSet);
                    int copyLength = content.length;
                    System.arraycopy(content, 0, FileBuffer, bufferPos, copyLength);
                    bufferPos += copyLength;
                }
                catch (ErrorCode e) {
                    upDateFileMeta();//更新原数据
                    throw e;
                }
            }
        }
    }

    @Override
    public byte[] read(int length) throws ErrorCode {
        if (length == 0) {
            return new byte[0];
        }

        loadFile();//检查是否初次读写，初次读写对File进行缓存

        length = Math.min(FileBuffer.length - (int)FileCurr, length);//length是int,取小总是int
        byte[] result = new byte[length];
        System.arraycopy(FileBuffer, (int)FileCurr, result, 0, length);

        return result;
    }

    /**
     * 更新FileBuffer
     * @param b 插入到FileCurr后的内容
     */
    @Override
    public void write(byte[] b) throws ErrorCode{
        if (b.length == 0) {
            return;
        }

        loadFile();//检查是否初次读写，初次读写对File进行缓存

        long newLength = (long)b.length + (long)FileBuffer.length;
        if (newLength > OVERFLOW_BOUND) {
            throw new ErrorCode(ErrorCode.INSERT_LENGTH_OVERFLOW);
        }

        byte[] newContent = new byte[(int)newLength];
        int copyPos = 0;
        if ((int)FileCurr != 0){
            System.arraycopy(FileBuffer, 0, newContent, copyPos, (int)FileCurr);
            copyPos += (int)FileCurr;
        }

        System.arraycopy(b, 0, newContent, copyPos, b.length);
        copyPos += b.length;

        if (FileCurr < FileBuffer.length) {
            System.arraycopy(FileBuffer, (int)FileCurr, newContent, copyPos, FileBuffer.length - (int)FileCurr);
        }

        FileBuffer = newContent;
        if (!isDirty) {
            isDirty = true;
        }
    }

    /**
     * 获得有备份个数个的内容是content的Block的List
     * @param content Block中的内容
     * @return 备份个数个的Block的blockSet
     */
    private Set<Block> newblockSet(byte[] content) {
        Set<Block> blockSet = new HashSet<>();
        for (int i = 0 ; i < copySize; i++) {
            blockSet.add(((JXWFileManager)FileManager).getRandomBlockManager().newBlock(content));
        }
        return blockSet;
    }

    /**
     * 更新FileMeta信息
     */
    public void upDateFileMeta() throws ErrorCode{
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFileMetaPath()));
            bw.write("size: " + FileEnd + "\n");
            bw.write("block size: " + FileBlockSize + "\n");
            bw.write("logic block size: " + FileBlockLists.size() + "\n");
            for (Set<Block> blockSet : FileBlockLists) {
                for (Block block : blockSet) {
                    bw.write(block.getBlockManager().getBlockManagerName() + "," + block.getIndexId().getName() + " ");
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * 读FileMeta获得FileBlockLists和FileEnd
     */
    private void readMetaGetInfo() throws ErrorCode {
        FileBlockLists = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(FileMetaPath));

            FileEnd = Long.parseLong(br.readLine().split(" ")[1]);
            FileBlockSize = Integer.parseInt(br.readLine().split(" ")[2]);
            int logicBlockSize = Integer.parseInt(br.readLine().split(" ")[3]);

            for (int i = 0; i < logicBlockSize; i++) {
                String line = br.readLine();
                if (line.equals("")) {
                    System.out.println("[INFO] the file has been damaged and you cannot attach to it anymore");
                    throw new ErrorCode(ErrorCode.LOGIC_BLOCK_EMPTY);
                }

                String[] cols = line.split(" ");
                Set<Block> blockSet = new HashSet<>();
                for (String bmBlock : cols){
                    String[] args = bmBlock.split(",");
                    blockSet.add(new JXWBlock(args[0], new JXWBlockId(args[1])));
                }
                FileBlockLists.add(blockSet);
            }
            br.close();
        }
        catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * 把File内保有的三个指针之一where移动offset个偏移量，返回移动后的位置
     * @param offset 偏移量
     * @param where 需要移动的指针
     * @return 如果where输入的正确返回新位置，where不为三个指针之一返回-1
     */
    @Override
    public long move(long offset, int where) {
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
        if (pos < FileStart) {
            pos = FileStart;
            new ErrorCode(ErrorCode.FILECURR_OVERFLOW).printStackTrace();
        }
        else if (FileBuffer != null) {
            if (pos > FileBuffer.length) {
                pos = FileBuffer.length;
                new ErrorCode(ErrorCode.FILECURR_OVERFLOW).printStackTrace();
            }
        }
        else if (pos > FileEnd){
            pos = FileEnd;
            new ErrorCode(ErrorCode.FILECURR_OVERFLOW).printStackTrace();
        }

        return pos;
    }

    /**
     * 如果Buffer中的数据脏了，写回并更新FileMeta
     */
    @Override
    public void close() throws ErrorCode {
        if (isDirty) {
            if (FileBuffer.length != 0) {
                FileEnd = FileBuffer.length;

                FileBlockLists = new ArrayList<>();
                int copyPos = 0;
                int completeBlockCount = ((int)FileEnd - copyPos) / FileBlockSize;
//                System.out.println("completeBlockCount=" + completeBlockCount);
                int remainBlockLength = ((int)FileEnd - copyPos) % FileBlockSize;
//                System.out.println(remainBlockLength);

                for (int i = 0; i < completeBlockCount; i++) {
                    byte[] content = new byte[FileBlockSize];
                    System.arraycopy(FileBuffer, copyPos, content, 0, FileBlockSize);
                    copyPos += FileBlockSize;

                    FileBlockLists.add(newblockSet(content));
                }

                if (remainBlockLength > 0) {
                    byte[] content = new byte[remainBlockLength];
                    System.arraycopy(FileBuffer, copyPos, content, 0, remainBlockLength);

                    FileBlockLists.add(newblockSet(content));
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
    public void setSize(long newSize) throws ErrorCode {
        loadFile();//检查是否初次读写，初次读写对File进行缓存

        if (newSize > FileBuffer.length) {//大于原大小，补零
            if (newSize > OVERFLOW_BOUND) {
                throw new ErrorCode(ErrorCode.INSERT_LENGTH_OVERFLOW);
            }

            byte[] newContent = new byte[(int)newSize];
            System.arraycopy(FileBuffer, 0, newContent, 0, FileBuffer.length);
            FileBuffer = newContent;

            isDirty = true;
        }
        else if (newSize < FileBuffer.length){//小于原大小，截断
            if (newSize < 0) {//确保新大小不为负
                newSize = 0;
                new ErrorCode(ErrorCode.NEW_FILE_SIZE_NEGATIVE).printStackTrace();
            }

            byte[] newContent = new byte[(int)newSize];
            System.arraycopy(FileBuffer, 0, newContent, 0, (int)newSize);
            FileBuffer = newContent;

            isDirty = true;
        }
    }












    /**
     * 从文件的开始位置开始读到结束位置，读最多length长度的文件内容
     * @param length 长度限制
     * @return 最多length长度的文件内容
     */
    public byte[] originRead(int length){
        if (length == 0) {
            return new byte[0];
        }

        length = Math.min((int)FileEnd - (int)FileCurr, length);//length是int,取小总是int
        byte[] result = new byte[length];

        int blockIndex = ((int)FileCurr / FileBlockSize);
        int copyStartPos = ((int)FileCurr % FileBlockSize);

        int resultCurr = 0;
        while (resultCurr < length){
            Set<Block> blockSet = FileBlockLists.get(blockIndex++);

            try {
                byte[] blockContent = getBlockContent(blockSet);
                int copyLength = Math.min(blockContent.length - copyStartPos, length - resultCurr);
                System.arraycopy(blockContent, copyStartPos, result, resultCurr, copyLength);
                resultCurr += copyLength;
            }
            catch (ErrorCode e) {
                e.printStackTrace();
            }

            if (copyStartPos != 0) copyStartPos = 0;
        }

        return result;
    }

    /**
     * 在文件游标的当前位置插入byte数组b
     * @param b 需要被写入文件的数据
     */
    public void originWrite(byte[] b) throws ErrorCode {
        int bLength = b.length;
        if (bLength == 0) return;

        List<Set<Block>> newblockSets = new ArrayList<>();

        int fileCurrBlockIndex = (int)FileCurr / FileBlockSize;
        int fileCurrBlockCurrPos = (int)FileCurr % FileBlockSize;

        for (int i = 0; i < fileCurrBlockIndex; i++) {//FileCurr所在当前Block之前的blockSet保持不变
            newblockSets.add(FileBlockLists.get(i));
        }

        long currentFileEnd = FileEnd + bLength;
        if (currentFileEnd > OVERFLOW_BOUND) {
            throw new ErrorCode(ErrorCode.INSERT_LENGTH_OVERFLOW);
        }

        int writeLenrgh = (int)currentFileEnd - fileCurrBlockIndex * FileBlockSize;
        byte[] contentToWrite = new byte[writeLenrgh];
        int copyPos = 0;
        if (fileCurrBlockCurrPos > 0) {
            try {
                byte[] fileCurrBlockContent = getBlockContent(FileBlockLists.get(fileCurrBlockIndex));
                System.arraycopy(fileCurrBlockContent, 0, contentToWrite, copyPos, fileCurrBlockCurrPos);
                copyPos += fileCurrBlockCurrPos;
            }
            catch (ErrorCode e) {
                e.printStackTrace();
            }
        }
        System.arraycopy(b, 0, contentToWrite, copyPos, bLength);
        copyPos += bLength;
        if (FileCurr < FileEnd) {
            System.arraycopy(read((int)FileEnd - (int)FileCurr), 0, contentToWrite, copyPos, (int)FileEnd - (int)FileCurr);
        }

        //更新后面的块儿
        copyPos = 0;
        int completeBlockCount = writeLenrgh - copyPos / FileBlockSize;
        int remainBlockLength = writeLenrgh - copyPos % FileBlockSize;

        for (int i = 0; i < completeBlockCount; i++) {
            byte[] content = new byte[FileBlockSize];
            System.arraycopy(contentToWrite, copyPos, content, 0, FileBlockSize);
            copyPos += FileBlockSize;

            newblockSets.add(newblockSet(content));
        }

        if (remainBlockLength > 0) {
            byte[] content = new byte[remainBlockLength];
            System.arraycopy(contentToWrite, copyPos, content, 0, remainBlockLength);

            newblockSets.add(newblockSet(content));
        }

        FileBlockLists = newblockSets;
        FileEnd += bLength;
        //更新FileMeta
        upDateFileMeta();
    }

    /**
     * 获得FileCurr游标当前位置所在的Block的Index和在当前Block中的offset
     * @return List.get(0)是BlockIndex，List.get(1)是offset
     */
    public List<Integer> getFileCurrBlockPos(){
        int blockCurr = 0;
        int startPosInFileCurrBlock = (int)FileCurr;
        int blockIndex = 0;

        for (Set<Block> blockSet : FileBlockLists){//顺序查找每一个Block的副本List，获得FileCurr所在的Block的Index和再这个Block中的offset
            if (FileCurr >= blockCurr){
                int currentBlockContentSize = getBlockContentSize(blockSet);
                if (startPosInFileCurrBlock < currentBlockContentSize){
                    break;
                }
                blockCurr += currentBlockContentSize;
                startPosInFileCurrBlock = (int)FileCurr - blockCurr;
                blockIndex++;
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(blockIndex);
        result.add(startPosInFileCurrBlock);

        return result;
    }
}