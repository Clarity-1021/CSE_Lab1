package ErrorManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException {
    public static final int IO_EXCEPTION = 1;//io异常
    public static final int CHECKSUM_CHECK_FAILED = 2;//Block的内容被损坏
    public static final int INSERT_LENGTH_OVERFLOW = 4;//输入过长
    public static final int NO_MD5_ALGORITHM = 5;//没有MD5加密算法
    public static final int BLOCK_MANAGER_DIR_CONSTRUCT_FAILED = 6;//BlockManager的目录创建失败
    public static final int FILE_MANAGER_DIR_CONSTRUCT_FAILED = 7;//FileManager的目录创建失败
    public static final int LOGIC_BLOCK_EMPTY = 8;//Block的所有副本内容都被损坏
    public static final int FILECURR_OVERFLOW = 9;//File的当前游标超出文件的开始和结尾的范围
    public static final int DELETE_FAILED = 10;//删除失败
    public static final int NEW_FILE_SIZE_NEGATIVE = 11;//新设置的File大小超出Integer.MAX_VALUE的限度
    public static final int FILE_NOT_EXIST = 12;//File不存在
    public static final int FILE_EXIST = 13;//File存在

    public static final int UNKNOWN = 1000;//未知错误
    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();//ErrorCode->ErrorMessage的Map
    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO exception");
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block checksum check failed");
        ErrorCodeMap.put(INSERT_LENGTH_OVERFLOW, "insert length overflow");
        ErrorCodeMap.put(NO_MD5_ALGORITHM, "no MD5 algorithm");
        ErrorCodeMap.put(BLOCK_MANAGER_DIR_CONSTRUCT_FAILED, "block manager dir construct failed");
        ErrorCodeMap.put(FILE_MANAGER_DIR_CONSTRUCT_FAILED, "file manager dir construct failed");
        ErrorCodeMap.put(LOGIC_BLOCK_EMPTY, "file is damaged");
        ErrorCodeMap.put(FILECURR_OVERFLOW, "file curr point overflow");
        ErrorCodeMap.put(DELETE_FAILED, "delete failed");
        ErrorCodeMap.put(NEW_FILE_SIZE_NEGATIVE, "file size is set to be negative");
        ErrorCodeMap.put(FILE_NOT_EXIST, "file does not exist");
        ErrorCodeMap.put(FILE_EXIST, "file exists");

        ErrorCodeMap.put(UNKNOWN, "unknown");
    }

    private int errorCode;//ErrorCode

    public ErrorCode(int errorCode) {
        super(String.format("ERROR_CODE[%d] \"%s\"", errorCode, getErrorText(errorCode)));
        logErrorMessage(getErrorText(errorCode));//把错误记录在日志里
        this.errorCode = errorCode;
    }

    /**
     *
     * @return ErrorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 通过ErrorCode获得ErrorMessage
     * @param errorCode ErrorCode
     * @return ErrorCode对应的ErrorMessage，如果ErrorCode不合法返回“invalid”
     */
    public static String getErrorText(int errorCode) {
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }

    /**
     * 把错误信息记录在日志里
     * @param errorMessage 错误信息
     */
    private static void logErrorMessage(String errorMessage){
        String logPath = "./output/Logging/Log.txt";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logPath, true));//追加写入
            bw.write("[" + TimeStampToDate() + "]: " + errorMessage + "\n");
            bw.close();
        }
        catch(IOException e){
//            e.printStackTrace();
            new ErrorCode(ErrorCode.IO_EXCEPTION).printStackTrace();
        }
    }

    /**
     * 得到当前时间的日期表示形式
     * @return 当前时间的日期表示形式
     */
    private static String TimeStampToDate(){
        Long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }
}
