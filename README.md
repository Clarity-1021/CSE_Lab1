# CSE_Lab1_SmartFileSystem

##### 18307130152_蒋晓雯

###### 文档里包括：异常处理+buffer设计+文件系统基础设计（比如一些地方做的决策，整个流程的介绍

## ErrorCode

##### 1. IO_EXCEPTION： IO异常

- 使用`buffer`或者`stream`对文件进行读写的时候可能会产生io异常
- 从控制台读取输入的字符时可能会产生io异常

##### 2. CHECKSUM_CHECK_FAILED： Block的内容被损坏

- Block的Data可能会出于某些原因被修改，通过与Block的Meta中存储的`checksum`比对，如果不一致说明Date被损坏

##### 3. BLOCK_NOT_EXIST： Block不存在

- 读Block的时候被读的Block不存在。

##### 4. INSERT_LENGTH_OVERFLOW： 输入过长

- 插入的内容的大小加上当前文件buffer中的数据的大小如果超出了`Integer.MAX_VALUE`，会溢出int的范围，无法写入文件，需要终止写入。

##### 5. NO_MD5_ALGORITHM： 没有MD5加密算法

- `checksum`是用`MD5`加密的，如果没有这种算法无法创建Block。

##### 6. BLOCK_MANAGER_DIR_CONSTRUCT_FAILED： BlockManager的目录创建失败

- ``java.io.File``的``mkdir``方法可能会创建目录失败，目录创建失败后续就无法创建Block。

##### 7. FILE_MANAGER_DIR_CONSTRUCT_FAILED： FileManager的目录创建失败

- ``java.io.File``的``mkdir``方法可能会创建目录失败，目录创建失败后续就无法创建File。

##### 8. LOGIC_BLOCK_EMPTY： Block的所有副本内容都被损坏

- LogicBlock中存的Block的所有副本都被损坏，文件被损坏。

##### 9. FILECURR_OVERFLOW： File的当前游标超出的限度

- 在`move`文件的当前游标时可能会超出文件开始和结束的范围。

##### 10. DELETE_FAILED： 删除失败

- ``java.io.File``的`delete`方法可能会删除文件失败，文件再打开状态或等等。

##### 11. NEW_FILE_SIZE_NEGATIVE： 新设置的File大小小于0

- 在使用`setSize`重置文件长度时，设置了负值。

##### 12. FILE_NOT_EXIST： File不存在

- `copy`文件的时候被复制的文件不存在。
- 读文件的时候被读的文件不存在。

##### 13. FILE_EXIST： File存在

- 在同一个FileManager下创建新文件时，如果重名，即文件已存在。
- `copy`文件的时候副本的名字和当前FileManager下的文件重名，即文件已存在。

## FileSystem Design

### FileManager

- 保留设置static且final的`root`字段，为FileManager的根目录，所有FileManager在`root`下面创建。

- FileManager由`FileManagerName`来唯一确定，通过`FileManagerName`可以得到FileManager的路径`root/{FileManagerName}`。

- 保留一个static的`FileManagerNumCount`字段，默认为1，在初始生成一系列FileManager时候，每新建一个FileManager时++，能够默认生成从FM-1到FM-{`FileManagerCount`}的`FileManagerCount`个FileManager。当然，也可以指定生成FileManager，FileManager的名字默认为`BM-{FileManagerNum}`，新建FileManager的时候需要给一个int类型的数值作为FileManager名字的一部分。或者可以给一个`FileManagerName`来生成名字为`FileManagerName`的FileManager。

- 保有BlockManager的个数`BlockManagerCount`，在创建FileManager时要给`BlockManagerCount`，用来在为文件分配新的Block时，在名字为BM-1~BM-{`BlockManagerCount`}中随机选择一个BlockManager来新建Block。

### File

- 设置一个static且final的`root`字段，为FileManager的根目录，所有FileManager在`root`下面创建，而File在对应的FileManager的路径下创建。

- File由`FileManager`和`FileId`来唯一确定，通过`FileManager`和`FileId`可以得到File的Meta文件的路径`root/{FileManager.getFileManagerName}/{FileId.getName()}.meta`。
- File的Meta文件中存了四个内容：size为`FileEnd`，block size为`FileBlockSize`，logic block size为`FileBlockLists.size()`以及`FileBlockLists`中每一个副本的`BlockManagerName`和`BlockId.getName()`。
- File的创建有GET_MODE和NEW_MODE以及通过另一个File来创建副本三个模式。
  1. GET_MODE：通过File的Meta信息来获得更新File保有的内容，如果File不存在则抛出异常。
  2. NEW_MODE：写入空File的Meta信息，如果File存在则抛出异常。
  3. 拷贝模式：通过被拷贝的File的Meta信息来更新被新File保有的内容，并写入新File的Meta文件中，如果被拷贝的File不存在或者新File以存在则抛出相应的异常。
- 设置一个static且final的`copySize`字段，控制创建Block时同时的需要创建副本个数。
- File有三个游标，`FileStart`、`FileEnd`、`FileCurr`
  1. `FileStart`为static且final的，永远为0
  2. `FileEnd`为File的最后一个字节的下标加一，即为File的大小
  3. `FileCurr`为当前读写游标，可以通过move来改变位置，但是move的范围不能超出[`FileStart`, `FileEnd`]，且当移动到`FileEnd`时做写操作（write），可以在文件的最后追加内容。当`FileCurr`移动到FileStart之前，打印异常信息并把`FileCurr`置为`FileStart`。当`FileCurr`移动到`FileEnd`之后时，打印异常信息并把`FileCurr`置为`FileEnd`。
- 保有一个List<Set<Block>>类型的`FileBlockLists`顺序存储存储了File的内容的Block和它的副本
- 保有一个byte[]类型的`FileBuffer`，实现通过缓存来读写和重置文件大小，在此File初次被读写或者重置大小时缓存File的内容到`FileBuffer`中，这之后再进行读写和重置文件大小的操作都无需从Block中读取，也不会立即写回，而是更改或直接读取`FileBuffer`中的内容。
- 保有一个boolean类型的`isDirty`的旗，默认为false表示FileBuffer未被更改过。进行写操作或重置文件大小的操作时，如果改变了`FileBuffer`的内容，吧`isDirty`置为true。File做close操作时，判断缓存中是否有脏数据，如果发现缓存被修改过，就重新写回File的内容，并更新File的Meta信息。只有做close操作时`FileBuffer`才会被写回，如果不close，所有的改变将不会出应用于File，File的内容不会被改变。
- 设置一个static且final的`FileBlockSize`字段，为File的每个Block的大小。
- 设置一个static且final的`OVERFLOW_BOUND`字段，为`Integer.MAX_VALUE`。当写操作或重置文件大小的操作会使得`FileBuffer.length`大于这个长度，直接close()如果当前`FileBuffer`中有脏数据，把`FileBuffer`写回并并抛出异常。

### FileId

- 只存了一个`FileName`的字段，可以通过接口getName()来获得。
- 只能通过给定`FileName`来生成创建FileId。
- 可以通过接口equals(Id)接口来确认Id是否为FileId且Id的getName()和此FileId的FileName一致时才判断两个Id相同。

### BlockManager

- 设置一个static且final的`root`字段，为BlockManager的根目录，所有BlockManager在`root`下面创建。

- BlockManager由`BlockManagerName`来唯一确定，通过`BlockManagerName`可以得到FileManager的路径`root/{BlockManagerName}`。

- 同FileManager一样，保留一个static的`BlockManagerNumCount`字段，默认为1，在初始生成一系列BlockManager时候，每新建一个BlockManager时++，能够默认生成从BM-1到BM-{`BlockManagerCount`}的`BlockManagerCount`个BlockManager。当然，也可以指定生成BlockManager，BlockManager的名字固定为`BM-{BlockManagerNum}`，新建BlockManager的时候需要给一个int类型的数值作为BlockManager名字的一部分。或者可以给一个`BlockManagerName`来生成名字为`BlockManagerName`的BlockManager，但如果命名格式不为BM-Num的形式则不会被File在创建Block时被用到。

### Block

- 设置一个static且final的`root`字段，为BlockManager的根目录，所有BlockManager在`root`下面创建，而Block在对应的FileManager的路径下创建。

- Block是由`BlockManagerName`和`BlockId`唯一确定的，通过`BlockManagerName`和`FileId`可以得到Block的Meta文件的路径`BlockMetaPath`为`root/{BlockManagerName}/{BlockId.getName()}.meta`和Block的Data文件的路径`BlockDataPath`为`root/{BlockManagerName}/{BlockId.getName()}.data`。
- Block的Meta文件中存了两个内容：size为`BlockContentSize`，checksum为`BlockCheckSum`。
- Block的Data文件中存Block中需要写入的内容。
- File的创建有写入和通过Id来获取两个模式。
  1. 写入（创建）模式：在Block的Data文件中写入需要写入Block的内容，并在Block的Meta文件中写入Meta信息。
  2. 通过Id获取：通过Block的Meta信息来获得更新Block保有的内容，如果Block不存在则抛出异常。
- 保有`BlockContentSize`存储BlockData文件中写入的内容的大小。
- 保有`BlockCheckSum`，用MD5加密生成写入BlockData中内容的验证码，用以之后读Block中内容时，确认Block的Data是否被损坏。

### BlockId

- 只存了一个BlockName的字段，可以通过接口getName()来获得。
- 默认创建BlockId的BlockName为`B-{当前时间的时间戳}`，来避免同一个BlockManager中出现Block重名的问题。也可以通过给定`BlockName`或Id来创建BlockId。
- 可以通过接口equals(Id)接口来确认Id是否为FileId且Id的getName()和此FileId的FileName一致时才判断两个Id相同。
