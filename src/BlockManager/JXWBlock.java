package BlockManager;

import Id.Id;

import java.io.*;
import java.util.Arrays;

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
     * Block的大小
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

        //创建meta文件并写入meta信息
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getMetaPath()));
            //Todo
            bw.write("");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建空的data文件
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getDataPath()));
            bw.write("");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建写有byte数组b的新Block
     * @param b 写入Block的byte数组
     */
    JXWBlock(BlockManager blockManager, byte[] b){
        BlockManager = blockManager;
        BlockId = new JXWBlockId(BlockManager.getBlockManagerNum(), BlockManager.getBlockNumCount());

        //创建meta文件并写入meta信息
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getMetaPath()));
            //Todo
            bw.write("");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建空的data文件
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockId.getDataPath()));

            //Todo
            //不知道这么写data对不对
            if (b.length > BlockSize){
                bw.write(Arrays.toString(b).substring(0, BlockSize));
            }
            else {
                bw.write(Arrays.toString(b));
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
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
}
