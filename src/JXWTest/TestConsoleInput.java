package JXWTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestConsoleInput {
    public static void main(String[] args) {
        String ENDMARK = "$$JXW$$";
        int FileBlockSize = 1024;

        byte[] result = new byte[0];
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter Content To Write(length < " + FileBlockSize + " bytes) and End With A Line {" + ENDMARK + "}: ");
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null && !line.equals(ENDMARK)){
                sb.append(line).append("\n");
            }
            String content = sb.toString();
            result = content.substring(0, content.lastIndexOf("\n")).getBytes();
            System.out.println("Your input is: \n" + content.substring(0, content.lastIndexOf("\n")));
            if (result.length > FileBlockSize){
                byte[] newContent = new byte[FileBlockSize];
                System.arraycopy(result, 0, newContent, 0, FileBlockSize);
                result = newContent;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
