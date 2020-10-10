package BlockManager;

import ErrorManager.ErrorLog;
import Id.Id;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static SmartTools.EnCode.getMD5Code;

public class JXWBlock implements Block {
    /**
     * Block的Id
     */
    private Id BlockId;

    /**
     * Block的BlockManager
     */
    private BlockManager BlockManager;
    
    /**
     * Block的大小，默认为1024个byte
     */
    private int BlockSize = 1024;

    /**
     * Block的data中实际写入的字节数，默认为0
     */
    private int BlockContentSize = 0;
    
    /**
     * 创建blockSize为blockSize的空Block
     * @param blockSize Block的大小
     */
    JXWBlock(BlockManager blockManager, int blockSize){
        BlockManager = blockManager;
        BlockSize = blockSize;
        BlockId = new JXWBlockId(BlockManager.getBlockManagerNum(), BlockManager.getBlockNumCount());

        //创建并把信息写入meta文件
        writeBlockMeta(BlockSize, 0, BlockId.getMetaPath());
        //创建空的data文件
        writeBlockData(new byte[0], BlockId.getDataPath());
    }

    /**
     * 创建写有byte数组b的新Block，Block的大小为默认大小
     * @param content 写入Block的byte数组
     */
    JXWBlock(BlockManager blockManager, byte[] content){
        BlockManager = blockManager;
        BlockId = new JXWBlockId(BlockManager.getBlockManagerNum(), BlockManager.getBlockNumCount());


        //确保写入BlockData中的内容不超过Block的大小
        if (content.length <= BlockSize){//未超出
            BlockContentSize = content.length;
            //创建并把信息写入meta文件
            writeBlockMeta(BlockSize, BlockContentSize, BlockId.getMetaPath());
            //创建并把content写入data文件
            writeBlockData(content, BlockId.getDataPath());
        }
        else {//content的长度超出BlockSize，截取前面BlockSize长度的部分
            BlockContentSize = BlockSize;
            byte[] newContent = new byte[BlockSize];
            System.arraycopy(content, 0, newContent, 0, BlockSize);
            //创建并把信息写入meta文件
            writeBlockMeta(BlockSize, BlockSize, BlockId.getMetaPath());
            //创建并把newContent写入data文件
            writeBlockData(newContent, BlockId.getDataPath());
        }
    }

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
     * 创建Block的meta文件，把BlockSize和checksum写入meta文件
     * @param blockSize Block的大小
     * @param contentSize Block中实际写入内容的大小
     * @param metaPath Block的meta文件的地址
     */
    public static void writeBlockMeta(int blockSize, int contentSize, String metaPath){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(metaPath));
            bw.write("size: " + blockSize + "\n");//写入BlockSize
            bw.write("checksum: " + getMD5Code(contentSize));//写入checksum用于后期检验Block是否被损坏
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 创建Block的data文件，把content写入data文件中
     * @param content 需要被写入BlockData的内容
     * @param dataPath Block的data文件的地址
     */
    public static void writeBlockData(byte[] content, String dataPath){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(dataPath));
            bw.write(new String(content));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
