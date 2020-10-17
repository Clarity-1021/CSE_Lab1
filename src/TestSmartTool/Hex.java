package TestSmartTool;

import BlockManager.*;
import SmartTools.SmartTools;

//HEX打印Block
public class Hex {
    public static final int blockManagerNum = 4;//BlockManager
    public static final String timeStamp = "20201017204351429";//Block时间戳

    public static void main(String[] args) {
        BlockManager bm1 = new JXWBlockManager(blockManagerNum);
        Block block = new JXWBlock(bm1.getBlockManagerName(), new JXWBlockId("B-" + timeStamp));
        SmartTools.smartHex(block);//Block转成HEX打印
    }
}
