package com.xunce.stream;

import com.xunce.utils.JSONUtil;
import com.xunce.vo.TFundBondsVO;
import com.xunce.vo.TFundCashInfoVO;
import com.xunce.vo.TFundSharesVO;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;
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

        FlinkKafkaConsumer010<ObjectNode> nodeFlinkKafkaConsumer010 = new FlinkKafkaConsumer010<>(topic, new JSONKeyValueDeserializationSchema(false), prop);
        //设置消费策略
        nodeFlinkKafkaConsumer010.setStartFromGroupOffsets();
        DataStreamSource<ObjectNode> source = env.addSource(nodeFlinkKafkaConsumer010);

//        OutputTag<TFundBondsVO> tFundBondsVOOutputTag= new OutputTag<TFundBondsVO>("tFundBondsVO");
//        OutputTag<TFundSharesVO> tFundSharesVOOutputTag = new OutputTag<>("tFundSharesVO");
//        OutputTag<TFundCashInfoVO> tFundCashInfoVOOutputTag = new OutputTag<TFundCashInfoVO>("tFundCashInfoVO");
//
//        SingleOutputStreamOperator<Tuple2<String, TFundBondsVO>> processTFundBondsVO = source.process(new ProcessFunction<ObjectNode, Tuple2<String, TFundBondsVO>>() {
//            @Override
//            public void processElement(ObjectNode jsonNodes, Context context, Collector<Tuple2<String, TFundBondsVO>> collector) throws Exception {
//
//            }
//        });


        // 拆分流
        SplitStream<ObjectNode> splitStream = source.split(new OutputSelector<ObjectNode>() {
            @Override
            public Iterable<String> select(ObjectNode jsonNodes) {
                ArrayList<String> list = new ArrayList<>();
                switch (jsonNodes.get("key").asInt()){
                    case 1:
                        list.add("tFundBondsVO");
                        break;
                    case 2:
                        list.add("tFundSharesVO");
                        break;
                    case 3:
                        list.add("tFundCashInfoVO");
                        break;
                    default:
                        break;
                }
                return list;
            }
        });

        SingleOutputStreamOperator<TFundBondsVO> tFundBondsVOOperator = splitStream.select("tFundBondsVO").map(new MapFunction<ObjectNode, TFundBondsVO>() {

            @Override
            public TFundBondsVO map(ObjectNode jsonNodes) throws Exception {
                String value = jsonNodes.get("value").toString();
                return JSONUtil.getJsonToBean(value,TFundBondsVO.class);
            }
        });

        SingleOutputStreamOperator<TFundSharesVO> tFundSharesVOOperator = splitStream.select("tFundSharesVO").map(new MapFunction<ObjectNode, TFundSharesVO>() {
            @Override
            public TFundSharesVO map(ObjectNode jsonNodes) throws Exception {
                String value = jsonNodes.get("value").toString();
                return JSONUtil.getJsonToBean(value, TFundSharesVO.class);
            }
        });

        SingleOutputStreamOperator<TFundCashInfoVO> tFundCashInfoVOOperator = splitStream.select("tFundCashInfoVO").map(new MapFunction<ObjectNode, TFundCashInfoVO>() {
            @Override
            public TFundCashInfoVO map(ObjectNode jsonNodes) throws Exception {
                String value = jsonNodes.get("value").toString();
                return JSONUtil.getJsonToBean(value, TFundCashInfoVO.class);
            }
        });

        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        tableEnv.registerDataStream("t_fund_bonds",tFundBondsVOOperator);

        String query = "select * from t_fund_bonds";
        Table table = tableEnv.sqlQuery(query);

        DataStream<TFundBondsVO> tFundBondsVODataStream = tableEnv.toAppendStream(table, TFundBondsVO.class);
        tFundBondsVODataStream.print();


//        tFundCashInfoVOOperator.print().setParallelism(1);



        env.execute("StreamingFromCollection");

    }
}
