import BlockManager.Block;
import BlockManager.JXWBlock;
import BlockManager.JXWBlockId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestReadFile {
    public static void main(String[] args) {
        String root = "./output/FileManagers/";
        String fileManagerName = "FM-" + 5;
        String fileName = "jxw_1";
        String FileMetaPath = root + fileManagerName + "/" + fileName + ".meta";

        try {
            BufferedReader br = new BufferedReader(new FileReader(FileMetaPath));
            for (int i = 0; i < 3; i++){
                br.readLine();
            }

            String line = null;
            while ((line = br.readLine()) != null){
                System.out.println(line);
                String[] cols = line.split(" ");
                List<Block> blockList = new ArrayList<>();
                for (String bmBlock : cols){
                    String[] a = bmBlock.split(",");
                    blockList.add(new JXWBlock(a[0], new JXWBlockId(a[1])));
                }
            }
            br.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
