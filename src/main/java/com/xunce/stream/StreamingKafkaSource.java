package com.xunce.stream;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.Properties;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/7/11
 */
public class StreamingKafkaSource {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //checkpoint配置
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //设置statebackend

        //env.setStateBackend(new RocksDBStateBackend("hdfs://hadoop100:9000/flink/checkpoints",true));


        String topic = "xc-producer01";
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers","node1.trms.com:9092, node2.trms.com:9092, node3.trms.com:9092");
        prop.setProperty("group.id","con1");

        FlinkKafkaConsumer010<ObjectNode> nodeFlinkKafkaConsumer010 = new FlinkKafkaConsumer010<>(topic, new JSONKeyValueDeserializationSchema(true), prop);
        //默认消费策略
        nodeFlinkKafkaConsumer010.setStartFromGroupOffsets();
        DataStreamSource<ObjectNode> objectNodeDataStreamSource = env.addSource(nodeFlinkKafkaConsumer010);



        objectNodeDataStreamSource.print().setParallelism(1);

        SingleOutputStreamOperator<ObjectNode> filter = objectNodeDataStreamSource.filter(new FilterFunction<ObjectNode>() {
            @Override
            public boolean filter(ObjectNode jsonNodes) throws Exception {

                return false;
            }
        });
        objectNodeDataStreamSource.map(new MapFunction<ObjectNode, Object>() {
            @Override
            public Object map(ObjectNode jsonNodes) throws Exception {
                return null;
            }
        });

        env.execute("StreamingFromCollection");

    }
}
