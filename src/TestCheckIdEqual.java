import BlockManager.JXWBlockId;
import FileManager.JXWFileId;

public class TestCheckIdEqual {
    public static void main(String[] args){
        JXWBlockId BId0 = new JXWBlockId(1, 1);
        JXWBlockId BId1 = new JXWBlockId(2, 1);
        JXWBlockId BId2 = new JXWBlockId(1, 2);
        JXWFileId FId0 = new JXWFileId(1, 1);

        System.out.println(BId0.equals(BId0));
        System.out.println(BId0.equals(BId1));
        System.out.println(BId0.equals(BId2));
        System.out.println(BId0.equals(FId0));
    }
}
