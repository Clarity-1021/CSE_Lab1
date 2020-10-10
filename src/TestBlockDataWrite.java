import java.io.*;

public class TestBlockDataWrite {
    public static void main(String[] args){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("./test.txt"));
            byte[] b = {0, 0, 0, 0};
            byte[] nB = new byte[2];
            System.arraycopy(b, 0, nB, 0, 2);
            bw.write(new String(nB));
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
