package SmartTools;

import java.math.BigInteger;
import java.security.MessageDigest;

public class EnCode {
    /**
     * 对数字进行MD5加密
     * @param num 数字
     * @return 进过MD5加密后的字符串
     */
    public static String getMD5Code(int num){
        byte[] code = null;

        try{
            MessageDigest md5 = MessageDigest.getInstance("md5");
            code = md5.digest((num + "").getBytes());
        }
        catch (Exception e){
            new Exception("No MD5 Algorithm.").printStackTrace();//打印异常和异常出现的位置
        }

        StringBuilder result = new StringBuilder(new BigInteger(1, code).toString(16));//code的byte数组以正数转为BigInteger再转化为16进制字符串
        for (int i = 0; i < 32 - result.length(); i++){//不足32位在前面补0
            result.insert(0, "0");
        }

        return result.toString();
    }
}
