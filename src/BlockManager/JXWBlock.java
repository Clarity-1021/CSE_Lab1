package BlockManager;

import ErrorManager.ErrorLog;
import Id.Id;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class JXWBlock implements Block {
    /**
     * Block的Id
     */
    private JXWBlockId BlockId;

    /**
     * Block的BlockManager
     */
    private BlockManager BlockManager;

    //BlockMeta信息
    /**
     * Block的大小，默认为1024个byte
     */
    private int BlockSize = 1024;

    /**
     * Block的data中实际写入的字节数，默认为0
     */
    private int BlockContentSize = 0;

    /**
     * Block的checkSum
     */
    private String BlockCheckSum;

    /**
     * 创建blockSize为blockSize的空Block
     * @param blockSize Block的大小
     */
    JXWBlock(BlockManager blockManager, int blockSize){
        BlockManager = blockManager;
        BlockSize = blockSize;
        BlockId = new JXWBlockId(BlockManager);

        writeBlockMessage(new byte[0]);
    }

    /**
     * 创建写有byte数组b的新Block，Block的大小为默认大小
     * @param content 写入Block的byte数组
     */
    JXWBlock(BlockManager blockManager, byte[] content){
        BlockManager = blockManager;
        BlockId = new JXWBlockId(BlockManager);
        BlockContentSize = content.length;

        byte[] contentToWrite = content;
        if (BlockContentSize > BlockSize){//确保写入BlockData中的内容不超过Block的大小
            BlockContentSize = BlockSize;
            contentToWrite = new byte[BlockContentSize];
            System.arraycopy(content, 0, contentToWrite, 0, BlockContentSize);
        }
        writeBlockMessage(contentToWrite);
    }

    /**
     * 获得Block中存储的data的大小
     */
    @Override
    public int getBlockContentSize() {
        return BlockContentSize;
    }

    /**
     * 获得Block的Id
     * @return BlockId
     */
    @Override
    public Id getIndexId() {
        return BlockId;
    }

    /**
     * 获得Block的BlockManager
     * @return Block的BlockManager
     */
    @Override
    public BlockManager getBlockManager() {
        return BlockManager;
    }

    /**
     * 获得BlockData的数据//原始方法
     * @return BlockDate中存的byte数组
     */
    public byte[] read1() {
        File file = new File(BlockId.getDataPath());
        long fileSize = file.length();//BlockData的字节大小

        if (fileSize > Integer.MAX_VALUE){//应该不存在这个情况，如果出现了可以吧Block的大小改小一点
            ErrorLog.logErrorMessage("BlockData文件大小超出最大整数的大小");
            return null;
        }

        byte[] buffer = null;
        try {
            FileInputStream fi = new FileInputStream(file);
            buffer = new byte[(int)fileSize];
            int offset = 0;
            int numRead = 0;

            //确保所有字节都被读到了
            //每次最多读入length-offset个字节，实际读到的字节数为numRead
            while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0){
                offset += numRead;
            }
            if (offset != buffer.length){
                throw new IOException("BlockData hasn't been completely read");
            }

            fi.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return buffer;
    }

    /**
     * 获得BlockData的数据
     * @return BlockDate中存的byte数组
     */
    @Override
    public byte[] read(){//在大文件处理是可以提升性能
        byte[] result = new byte[0];

        try {
            FileChannel fc = new RandomAccessFile(BlockId.getDataPath(), "r").getChannel();
            //把缓冲区的内容加载到物理内存中
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
            result = new byte[(int)fc.size()];
            if (mbb.remaining() > 0){//缓冲区中存在剩余元素
                mbb.get(result, 0, mbb.remaining());
            }
            fc.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 返回blockSize
     * @return Block的size
     */
    @Override
    public int blockSize() {
        return BlockSize;
    }

    /**
     * 更新Block的信息
     * @param content BlockDate中需要写入的内容
     */
    public void writeBlockMessage(byte[] content) {
        BlockCheckSum = getBlockDateCheckSum(content);
        //创建并把content写入data文件
        writeBlockData(content);
        //创建并把信息写入meta文件
        writeBlockMeta();
    }

    /**
     * 创建Block的meta文件，把BlockSize和checksum写入meta文件
     */
    public void writeBlockMeta(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getMetaPath()));
            bw.write("size: " + BlockSize + "\n");//写入BlockSize
            bw.write("checksum: " + BlockCheckSum);//写入checksum用于后期检验Block是否被损坏
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建Block的data文件，把content写入data文件中
     * @param content 需要被写入BlockData的内容
     */
    public void writeBlockData(byte[] content){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getDataPath()));
            bw.write(new String(content, 0, BlockContentSize));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据给的content计算content的chekcSum[md5加密]
     * @param content 给的字段
     * @return checkSum
     */
    public String getBlockDateCheckSum(byte[] content) {
        byte[] code = new byte[0];

        try{
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(content);
            code = md5.digest();
        }
        catch (Exception e){
            new Exception("No MD5 Algorithm.").printStackTrace();//打印异常和异常出现的位置
        }

        return DatatypeConverter.printHexBinary(code).toLowerCase();
    }

    /**
     * 通过BlockData中的Content计算checkSum并与Block真正的checkSum对比，如果一致则未被损坏
     * @return 被损坏返回true，未被损坏返回false
     */
    public boolean isBlockDamaged() {
        return !getBlockDateCheckSum(read()).equals(BlockCheckSum);
    }
}
