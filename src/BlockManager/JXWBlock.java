package BlockManager;

import ErrorManager.ErrorCode;
import Id.Id;
import SmartTools.SmartTools;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class JXWBlock implements Block {
    private static String root = "./output/BlockManagers/";//Block文件输出的默认根目录

    /**
     * Block的Id
     */
    private JXWBlockId BlockId;
    private String BlockMetaPath;
    private String BlockDataPath;

    /**
     * Block的BlockManager
     */
    private String BlockManagerName;

    //BlockMeta信息
    /**
     * Block的data中实际写入的字节数，默认为0
     */
    private int BlockContentSize = 0;

    /**
     * Block的checkSum
     */
    private String BlockCheckSum;

    /**
     * 创建写有byte数组b的新Block
     * @param blockManagerName BlockManagerName
     * @param content 写入Block的byte数组
     */
    JXWBlock(String blockManagerName, byte[] content) throws ErrorCode{
        BlockManagerName = blockManagerName;
        BlockContentSize = content.length;
        BlockId = new JXWBlockId();
        BlockMetaPath = root + BlockManagerName + "/" + BlockId.getName() + ".meta";
        BlockDataPath = root + BlockManagerName + "/" + BlockId.getName() + ".data";

        writeBlockMessage(content);
    }

    /**
     * 通过BlockId获得Block
     * @param blockManagerName BlockManagerName
     * @param blockId BlockId
     */
    public JXWBlock(String blockManagerName, Id blockId) throws ErrorCode {
        BlockManagerName = blockManagerName;
        BlockId = new JXWBlockId(blockId);
        BlockMetaPath = root + BlockManagerName + "/" + BlockId.getName() + ".meta";
        BlockDataPath = root + BlockManagerName + "/" + BlockId.getName() + ".data";

        //读Meta获得BlockContentSize和BlockCheckSum
        readMetaGetInfo();
    }

    /**
     * 读BlockMeta获得BlockContentSize和BlockCheckSum
     */
    private void readMetaGetInfo() throws ErrorCode {
        File file = new File(BlockMetaPath);
        if (!file.exists()){
            return;
        }

        try {
            FileReader fr = new FileReader(BlockMetaPath);
            BufferedReader br = new BufferedReader(new FileReader(BlockMetaPath));
            BlockContentSize = Integer.parseInt(br.readLine().split(" ")[1]);
            BlockCheckSum = br.readLine().split(" ")[1];
            br.close();
            fr.close();
        }
        catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
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
        return new JXWBlockManager(BlockManagerName);
    }

    /**
     * 获得BlockData的数据
     * @return BlockDate中存的byte数组
     */
    @Override
    public byte[] read() throws ErrorCode{//在大文件处理是可以提升性能
        byte[] result = new byte[0];

        try {
            FileChannel fc = new RandomAccessFile(BlockDataPath, "r").getChannel();
            //把缓冲区的内容加载到物理内存中
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
//            System.out.println("fileLength=" + fc.size());
            result = new byte[(int)fc.size()];
            if (mbb.remaining() > 0){//缓冲区中存在剩余元素
                mbb.get(result, 0, mbb.remaining());
            }
//            System.out.println("String=" + new String(result));
            fc.close();
        }
        catch (IOException e){
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
//        File file = new File(BlockDataPath);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream((int)file.length());
//        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
//            byte[] buffer = new byte[(int)file.length()];
//            int len = 0;
//            while (-1 != (len = bis.read(buffer, 0, (int)file.length()))) {
//                bos.write(buffer, 0, len);
//            }
//            result = bos.toByteArray();
//        }
//        catch (IOException e) {
//            new ErrorCode(ErrorCode.IO_EXCEPTION).printStackTrace();
//        }

        return result;
    }

    /**
     * 返回blockSize
     * @return Block的size
     */
    @Override
    public int blockSize() {
        return BlockContentSize;
    }

    /**
     * 更新Block的信息
     * @param content BlockDate中需要写入的内容
     */
    public void writeBlockMessage(byte[] content) throws ErrorCode {
        BlockCheckSum = getBlockDateCheckSum(content);
        //创建并把content写入data文件
        writeBlockData(content);
        //创建并把信息写入meta文件
        writeBlockMeta();
    }

    /**
     * 创建Block的meta文件，把BlockSize和checksum写入meta文件
     */
    public void writeBlockMeta() throws ErrorCode{
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BlockMetaPath));
            bw.write("size: " + BlockContentSize + "\n");//写入BlockContentSize
            bw.write("checksum: " + BlockCheckSum);//写入checksum用于后期检验Block是否被损坏
            bw.close();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * 创建Block的data文件，把content写入data文件中
     * @param content 需要被写入BlockData的内容
     */
    public void writeBlockData(byte[] content) throws ErrorCode{
        try {
            FileOutputStream fos = new FileOutputStream(BlockDataPath);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * 根据给的content计算content的chekcSum[md5加密]
     * @param content 给的字段
     * @return checkSum
     */
    public String getBlockDateCheckSum(byte[] content) throws ErrorCode {
//        System.out.println("HEX=" + SmartTools.parseBytesToHex(content));
        byte[] code = new byte[0];

        try{
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(content);
            code = md5.digest();
        }
        catch (Exception e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);//打印异常和异常出现的位置
        }

        String temp = DatatypeConverter.printHexBinary(code).toLowerCase();
//        System.out.println("checkSum=" + temp);

//        return DatatypeConverter.printHexBinary(code).toLowerCase();
        return temp;
    }

    /**
     * 通过BlockData中的Content计算checkSum并与Block真正的checkSum对比，如果一致则未被损坏
     * @return 被损坏返回true，未被损坏返回false
     */
    public boolean isBlockDamaged() throws ErrorCode {
        return !getBlockDateCheckSum(read()).equals(BlockCheckSum);
    }
}
