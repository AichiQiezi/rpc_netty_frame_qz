package com.acqz.rpc.registry.zk.util;

import com.acqz.rpc.remoting.transport.socket.T;
import io.protostuff.StringSerializer;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * @author haofeng
 * @date 2023/4/7 9:39
 */

public class KafkaUtils{

    static KafkaProducer<String,String> kafkaProducer;
    static KafkaConsumer<String,String> kafkaConsumer;
    static Properties properties;

    static {
        properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.133:9092");

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    }


    public static KafkaProducer initProducer() {
        kafkaProducer = new KafkaProducer(properties);
        return kafkaProducer;
    }

    public static KafkaConsumer initConsumer() {
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test1");
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.StickyAssignor");
        kafkaConsumer = new KafkaConsumer<>(properties);
        return kafkaConsumer;
    }

    public static void main(String[] args) {

    }

}
