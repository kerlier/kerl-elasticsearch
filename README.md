# elasticsearch


#### ES聚合
##### 桶
桶就是满足特定条件的文档的集合。
比如说一个雇员属于 男性桶以及女性桶
当聚合开始被执行，每个文档的值通过计算来决定符合哪个桶的条件。如果匹配到，文档将放入
相应的桶并接着聚合操作。

ES中有很多类型的桶，能让你通过很多种方式来划分

一般要使用high rest level-client 
https://www.cnblogs.com/tigerlion/p/12961737.html

##### 
