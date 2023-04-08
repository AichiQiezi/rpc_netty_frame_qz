package com.acqz.rpc.registry.zk.util;

import cn.hutool.json.JSONUtil;
import com.acqz.common.enums.RpcConfigEnum;
import com.acqz.common.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator(zookeeper client) utils
 */
@Slf4j
public final class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final KafkaProducer<String, String> KAFKA_PRODUCER = KafkaUtils.initProducer();
    private static final KafkaConsumer<String, String> KAFKA_CONSUMER = KafkaUtils.initConsumer();
    private static final String topic = "messageBus";

    private CuratorUtils() {
    }

    /**
     * Create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnects
     *
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * Gets the children under a node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version1
     * @return All child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * Empty the registry of data
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    public static CuratorFramework getZkClient() {
        // check if user has set zk address
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * Registers to listen for changes to the specified node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        // Each zookeeper node corresponds to consumer group
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        // when the node data changes,the local cache is updated and the message sending is triggered
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            long version = System.currentTimeMillis();
            MessageBus messageBus = MessageBus.builder().version(version)
                    .serviceAddresses(serviceAddresses)
                    .rpcServiceName(rpcServiceName).build();
            KAFKA_PRODUCER.send(new ProducerRecord<>(topic, JSONUtil.toJsonStr(messageBus)));
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);

        };
        listenMessageBus();
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener::childEvent);
        pathChildrenCache.start();
    }

    private static void listenMessageBus() {
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = KAFKA_CONSUMER.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    MessageBus messageBus = JSONUtil.toBean(record.value(), MessageBus.class);
                    try {
                        updateServiceRegistry(messageBus);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                KAFKA_CONSUMER.commitAsync();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void updateServiceRegistry(MessageBus messageBus) throws Exception {
        byte[] bytes = zkClient.getData().forPath(ZK_REGISTER_ROOT_PATH + messageBus.getRpcServiceName());
        MessageBus curMessageBus = JSONUtil.toBean(new String(bytes), messageBus.getClass());
        if (curMessageBus.getVersion() > messageBus.getVersion()){
            return;
        }
        SERVICE_ADDRESS_MAP.put(messageBus.getRpcServiceName(),messageBus.getServiceAddresses());
        zkClient.setData().forPath(ZK_REGISTER_ROOT_PATH + messageBus.getRpcServiceName(),JSONUtil.toJsonStr(messageBus).getBytes(StandardCharsets.UTF_8));
    }

//    public static void main(String[] args) throws Exception {
//        CuratorFramework zkClient = getZkClient();
//        List<String> strings = zkClient.getChildren().forPath("/my-rpc/com.acqz.common.service.HelloService_test1_version1");
//        byte[] bytes = zkClient.getData().forPath("/my-rpc/com.acqz.common.service.HelloService_test1_version1");
//        System.out.println(new String(bytes));
//    }

}
