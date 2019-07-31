package com.xunce.stream;

import com.xunce.utils.JSONUtil;
import com.xunce.vo.TFundBondsVO;
import com.xunce.vo.TFundCashInfoVO;
import com.xunce.vo.TFundRiskVO;
import com.xunce.vo.TFundSharesVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Properties;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/7/11
 */
public class StreamingKafkaSource2 {
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

        OutputTag valueStr = new OutputTag<Tuple2<String,>>("valueStr"){};

        SingleOutputStreamOperator<Tuple2<String, TFundBondsVO>> map = source.map(new MapFunction<ObjectNode, Tuple2<String, TFundBondsVO>>() {
            @Override
            public Tuple2<String, TFundBondsVO> map(ObjectNode jsonNodes) throws Exception {

                return new Tuple2<>();
            }
        });

        WindowedStream<Tuple2<String, TFundBondsVO>, Tuple, GlobalWindow> countWindow = map.keyBy(0).countWindow(3);
        countWindow.



        SingleOutputStreamOperator<String> mainOutPutStream = source.process(new ProcessFunction<ObjectNode, String>() {
            @Override
            public void processElement(ObjectNode jsonNodes, Context context, Collector<String> collector) throws Exception {
                String value = jsonNodes.get("value").toString();
                switch (jsonNodes.get("key").asInt()){
                    case 1:
                        TFundBondsVO tFundBondsVO = JSONUtil.getJsonToBean(value, TFundBondsVO.class);
                        String key = StringUtils.join(tFundBondsVO.getBatchNumber(), "_", tFundBondsVO.getLDate(), "_", tFundBondsVO.getLFundId());
                        // 将数据发送到常规输出

                        break;
                    case 2:
                        TFundSharesVO tFundSharesVO = JSONUtil.getJsonToBean(value, TFundSharesVO.class);
                        String tFundSharesVOKey = StringUtils.join(tFundSharesVO.getBatchNumber(), "_", tFundSharesVO.getLDate(), "_", tFundSharesVO.getLFundId());
                        // 将数据发送到侧流输出
                        context.output(tFundSharesVOOutputTag,tFundSharesVO);
                        break;
                    case 3:
                        TFundCashInfoVO tFundCashInfoVO = JSONUtil.getJsonToBean(value, TFundCashInfoVO.class);
                        String tFundCashInfoVOKey = StringUtils.join(tFundCashInfoVO.getBatchNumber(), "_", tFundCashInfoVO.getLDate(), "_", tFundCashInfoVO.getLFundId());
                        break;
                    default:
                        break;
                }
                collector.collect(value);

            }
        });





        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        String fileds = "batchNumber as batch_number,lDate as l_date,lFundId as l_fund_id,enBondsCash as en_bonds_cash";
        tableEnv.fromDataStream(processTFundBondsVO,"t_fund_bonds");
        tableEnv.fromDataStream(tFundSharesVOSideOutput,"t_fund_shares");
        tableEnv.fromDataStream(tFundCashInfoVOSideOutput,"t_fund_cashInfo");

        String query = "select batchNumber as batch_number,lDate as l_date,lFundId as l_fund_id,enBondsCash as en_bonds_cash from t_fund_bonds";
        Table table = tableEnv.sqlQuery(query);

        DataStream<Row> rowDataStream = tableEnv.toAppendStream(table, Row.class);

        rowDataStream.print();


        env.execute("StreamingFromCollection");

    }
}
