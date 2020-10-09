package BlockManager;

import Id.Id;

// Block write , immutatable
public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read();
    int blockSize();
}
