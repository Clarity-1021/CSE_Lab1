package BlockManager;

import Id.Id;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JXWBlockId implements Id {
    /**
     * Block的编号
     */
    private String BlockName;

    public JXWBlockId() {
        BlockName = TimeStampNaming();
    }

    public JXWBlockId(String blockName){
        BlockName = blockName;
    }

    public JXWBlockId(Id indexId) {
        BlockName = indexId.getName();
    }

    @Override
    public String getName() {
        return BlockName;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWBlockId && indexId.getName().equals(BlockName);
    }

    /**
     * 得到当前时间的时间戳精确到毫秒，生成block的名字
     * @return BlockName
     */
    private static String TimeStampNaming(){
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "B-" + sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }
}
