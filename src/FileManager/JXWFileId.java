package FileManager;

import Id.Id;

public class JXWFileId implements Id {
    /**
     * File的Name
     */
    private String FileName;
    /**
     * File的meta文件的路径
     */
    private String FileMetaPath;

    public JXWFileId(String fileName) {
        FileName = fileName;
    }

    @Override
    public String getName() {
        return FileName;
    }

    /**
     * indexId是否与此Id相等
     * @return 相等返回true，不相等返回false
     */
    @Override
    public boolean equals(Id indexId) {
        return indexId instanceof JXWFileId && indexId.getName().equals(FileName);
    }
}
