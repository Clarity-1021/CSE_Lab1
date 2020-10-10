package ErrorManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ErrorLog {
    /**
     * 把错误信息记录在日志里
     * @param errorMessage 错误信息
     */
    public static void logErrorMessage(String errorMessage){
        String logPath = "./output/Logging/log.txt";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logPath, true));//追加写入
            bw.write("[" + TimeStampToDate() + "]: " + errorMessage + "\n");
            bw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 得到当前时间的日期表示形式
     * @return 当前时间的日期表示形式
     */
    public static String TimeStampToDate(){
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }
}
