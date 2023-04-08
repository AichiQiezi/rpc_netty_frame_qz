# rpc_netty_frame_qz
rpc
# hello
![image](https://user-images.githubusercontent.com/84713423/224462191-aec3aa6d-867e-4cae-a72a-ee8a56aabae1.png)


# 自定义rpc传输协议

  0     1     2     3     4        5     6   7    8      9          10      11        12    13   14   15   16
+-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+----+---+
|   magic   code        |version |   full length       |messageType| codec |compress|    RequestId       |
+-----------------------+--------+---------------------+-----------+-----------+-----------+-------------+
| Extended field length |           Extended field .....              .......
+-----------------------+--------+---------------------+-----------+-----------+-----------+-------------+
|                                                                                                        |
|                                         body                                                           |
|                                                                                                        |
|                                        ... ...                                                         |
+--------------------------------------------------------------------------------------------------------+
4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）  4B Extended field length （拓展字段的长度）

# 熔断方法


# 
