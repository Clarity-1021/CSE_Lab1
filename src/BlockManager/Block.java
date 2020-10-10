package BlockManager;

import Id.Id;

// Block write , immutatable
public interface Block {
    int getBlockContentSize();
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read();
    int blockSize();
}
