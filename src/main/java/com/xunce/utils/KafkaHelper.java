package com.xunce.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;
import java.util.Properties;

public class KafkaHelper implements Serializable {
    private  static Properties props = new Properties();
    private static Producer<String, String> producer;
    static{
        props.put("bootstrap.servers", "node1.trms.com:9092, node2.trms.com:9092, node3.trms.com:9092");
        // acks=0 配置适用于实现非常高的吞吐量 , acks=all 这是最安全的模式
        props.put("acks", "all");
        //当生产者发送消息收到一个可恢复异常时，会进行重试，这个参数指定了重试的次数。
        props.put("retries", 3);
        props.put("enable.idempotence", true);
        //发送到同一个partition的消息会被先存储在batch中，该参数指定一个batch可以使用的内存大小，单位是	byte。不一定需要等到batch被填满才能发送  默认16384=16KB
        props.put("batch.size", 16384);
        // 当linger.ms>0时，延时性会增加，但会提高吞吐量，因为会减少消息发送频率
        props.put("linger.ms", 1);
        // 32MB
        props.put("buffer.memory", 33554432);
        // 默认不压缩，该参数可以设置成snappy、gzip或lz4对发送给broker的消息进行压缩.Snappy压缩技术是Google开发的，它可以在提供较好的压缩比的同时，减少对CPU的使用率并保证好的性能，所以建议在同时考虑性能和带宽的情况下使用。
        props.put("compression.type","snappy");
        //props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //Thread.currentThread().setContextClassLoader(null);
        producer = new KafkaProducer<>(props);
    }

    public static void sendLog(String topic, String key,String log){
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, log);

        producer.send(producerRecord);
    }

    private static void saveData(String topic,String value){
        ProducerRecord<String,String> record = new ProducerRecord<>(topic, value);
        producer.send(record);
    }

    public static void saveData(String topic,String key, String value){
        ProducerRecord<String,String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
    }

}

