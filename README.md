1.  如何实现一个简易的倒排索引？

倒排索引的基础存储结构是一个N行M列的0/1矩阵Matrix，HashMap<Integer,RoaringBitmap>
```
         ad1        ad2        ad3      .......    .......     adM
-----|----------|----------|----------|----------|----------|----------|
dim1 |    1     |    1     |    0     |    0     |    1     |    1     |   
dim2 |    1     |    0     |    0     |    0     |    1     |    1     |
dim3 |    1     |    1     |    1     |    1     |    1     |    1     |
.... |    1     |    0     |    1     |    1     |    1     |    1     |
.... |    1     |    1     |    0     |    1     |    1     |    1     |
dimN |    1     |    1     |    1     |    1     |    1     |    1     |
```

其中每行为一个[RoaringBitmap](https://github.com/RoaringBitmap/RoaringBitmap)结构；

1）我们可以把Matrix看作广告在某个定向域的倒排索引（N行/M列），其中

行：每行代表该广告在定向域的一个值，假设是定向域"性别"的倒排索引，

   > 性别可取的编码值有（100：全部、101：未知、102：男、103：女）4个，
   > 
   > 那么该倒排索引就有4行，每行代表一个性别的域值

列：每列代表一个计划id，可表示的计划id范围为（0～2,147,483,647），那么矩阵的每个0/1值，表示纵列的计划id是否可以投放在横列的定向值域上

2）我们把广告平台的所有定向域的倒排索引联合起来，就组成了整体的广告倒排索引

HashMap<String,Matrix>, 目前我们支持的定向域包括：

 * 性别：gender
 * 年龄：age
 * 兴趣：interest
 * 人群包：package
 * 排除人群包：excludePackage

后续可以通过配置文件自由扩展

2.  如何让索引做到实时更新？
我们利用[Canal](https://github.com/alibaba/canal.git)来读取业务平台广告库的binlog，
可以近似实时的获取广告库的变更，及时更新到倒排索引，与业务平台做到优雅解耦。

3.  如何让索引服务集群化，具备高并发服务能力？
利用Canal将业务平台广告库的binlog写到到kafka队列，让每个索引节点主动从kafka拉取广告库的变更，让索引服务具备分布式的扩展能力；
同时将索引节点的服务状态维护到zookeeper，让索引服务具备高可用能力
