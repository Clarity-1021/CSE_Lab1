package BlockManager;

import Id.Id;

public interface BlockManager {
    String getBlockManagerName();
    Block getBlock(Id indexId);
    Block newBlock(byte[] b);
    default Block newEmptyBlock(int blockSize) {
        return newBlock(new byte[blockSize]);
    }
}
