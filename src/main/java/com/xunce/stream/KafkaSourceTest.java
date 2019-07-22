package com.xunce.stream;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/28
 */
public class KafkaSourceTest {
    public static void main(String[] args) throws Exception {
        //定义流处理环境

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment();

        // 非常关键，一定要设置启动检查点！！
        env.enableCheckpointing(5000);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //配置kafka信息
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "node1.xuncetech.com:9092, node2.xuncetech.com:9092, node3.xuncetech.com:9092");
        properties.setProperty("group.id", "flink001");
        //读取数据
        FlinkKafkaConsumer010<String> consumer = new FlinkKafkaConsumer010<>("twidefund", new SimpleStringSchema(), properties);

        //设置只读取最新数据
        //consumer.setStartFromLatest();
        consumer.setStartFromEarliest();
        //添加kafka为数据源
        DataStream<String> stream = env.addSource(consumer);

        FlinkKafkaProducer010<String> producer010 = new FlinkKafkaProducer010<>("node1.xuncetech.com:9092, node2.xuncetech.com:9092, node3.xuncetech.com:9092", "flink-test-sink", new KeyedSerializationSchema<String>() {
            @Override
            public byte[] serializeKey(String s) {
                return new byte[0];
            }

            @Override
            public byte[] serializeValue(String s) {
                return new byte[0];
            }

            @Override
            public String getTargetTopic(String s) {
                return null;
            }
        });
        producer010.setWriteTimestampToKafka(true);
        stream.addSink(producer010);

        env.execute();
    }
}
