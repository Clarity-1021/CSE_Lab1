package BlockManager;

import Id.Id;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;

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
            //创建并把信息写入meta文件
            writeBlockMeta(BlockSize, content.length, BlockId.getMetaPath());
            //创建并把content写入data文件
            writeBlockData(content, BlockId.getDataPath());
        }
        else {//content的长度超出BlockSize，截取前面BlockSize长度的部分
            byte[] newContent = new byte[BlockSize];
            System.arraycopy(content, 0, newContent, 0, BlockSize);
            //创建并把信息写入meta文件
            writeBlockMeta(BlockSize, BlockSize, BlockId.getMetaPath());
            //创建并把newContent写入data文件
            writeBlockData(newContent, BlockId.getDataPath());
        }
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
     * 获得BlockData的数据
     * @return BlockDate中存的byte数组
     */
    @Override
    public byte[] read() {
        return new byte[0];
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
     * 对数字进行MD5加密
     * @param num 数字
     * @return 进过MD5加密后的字符串
     */
    public static String getMD5Code(int num){
        byte[] code = null;

        try{
            MessageDigest md5 = MessageDigest.getInstance("md5");
            code = md5.digest((num + "").getBytes());
        }
        catch (Exception e){
            new Exception("No MD5 Algorithm.").printStackTrace();//打印异常和异常出现的位置
        }

        StringBuilder result = new StringBuilder(new BigInteger(1, code).toString(16));//code的byte数组以正数转为BigInteger再转化为16进制字符串
        for (int i = 0; i < 32 - result.length(); i++){//不足32位在前面补0
            result.insert(0, "0");
        }

        return result.toString();
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
