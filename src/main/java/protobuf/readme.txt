解压后有两个文件：protobuf-java-2.5.0.jar和protoc.exe。

protobuf-java-2.5.0.jar即protobuf所需要的jar包，
如果用maven的话可以无视这个文件；

protoc.exe是protobuf代码生成工具。





使用文件protoc.exe，cmd命令行运行：

protoc.exe --java_out=E:\Java PersonMsg.proto

输入文件是PersonMsg.proto，也就是定义数据结构的文件；
输出文件夹是E:\java，将java文件生成在E:\java中。
运行命令成功后会生成PersonMsg.java：
