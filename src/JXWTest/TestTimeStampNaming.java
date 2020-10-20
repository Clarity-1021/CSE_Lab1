package JXWTest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTimeStampNaming {
    public static void main(String[] args){
        System.out.println(TimeStampNaming());
    }

    /**
     * 得到当前时间的时间戳精确到毫秒
     * @return 当前时间的时间戳
     */
    public static String TimeStampNaming(){
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }
}
