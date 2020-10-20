# ErrorDode

##### 1. IO_EXCEPTION： IO异常

- 使用`buffer`或者`stream`对文件进行读写的时候可能会产生io异常
- 从控制台读取输入的字符时可能会产生io异常

##### 2. CHECKSUM_CHECK_FAILED： Block的内容被损坏

- Block的Data可能会出于某些原因被修改，通过与Block的Meta中存储的`checksum`比对，如果不一致说明Date被损坏

##### 3. INSERT_LENGTH_OVERFLOW： 输入过长

- 插入的内容的大小加上当前文件buffer中的数据的大小如果超出了`Integer.MAX_VALUE`，会溢出int的范围，无法写入文件，需要终止写入

##### 4. NO_MD5_ALGORITHM： 没有MD5加密算法

- `checksum`是用`MD5`加密的，如果没有这种算法无法创建Block

##### 5. BLOCK_MANAGER_DIR_CONSTRUCT_FAILED： BlockManager的目录创建失败

- ``java.io.File``的``mkdir``方法可能会创建目录失败，目录创建失败后续就无法创建Block

##### 6. FILE_MANAGER_DIR_CONSTRUCT_FAILED： FileManager的目录创建失败

- ``java.io.File``的``mkdir``方法可能会创建目录失败，目录创建失败后续就无法创建File

##### 7. LOGIC_BLOCK_EMPTY： Block的所有副本内容都被损坏

- LogicBlock中存的Block的所有副本都被损坏，文件被损坏

##### 8. FILECURR_OVERFLOW： File的当前游标超出的限度

- 在`move`文件的当前游标时可能会超出文件开始和结束的范围

##### 9. DELETE_FAILED： 删除失败

- ``java.io.File``的`delete`方法可能会删除文件失败，文件再打开状态或等等

##### 10. NEW_FILE_SIZE_NEGATIVE： 新设置的File大小小于0

- 在使用`setSize`重置文件长度时，设置了负值

##### 11. FILE_NOT_EXIST： File不存在

- `copy`文件的时候被复制的文件不存在
- 读文件的时候被读的文件不存在

##### 12. FILE_EXIST： File存在

- 在同一个FileManager下创建新文件时，如果重名，即文件已存在
- `copy`文件的时候副本的名字和当前FileManager下的文件重名，即文件已存在