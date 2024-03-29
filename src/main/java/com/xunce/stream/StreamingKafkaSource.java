package com.xunce.stream;

import com.xunce.utils.JSONUtil;
import com.xunce.vo.TFundBondsVO;
import com.xunce.vo.TFundCashInfoVO;
import com.xunce.vo.TFundRiskVO;
import com.xunce.vo.TFundSharesVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
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

        OutputTag<TFundSharesVO> tFundSharesVOOutputTag = new OutputTag<TFundSharesVO>("tFundSharesVO"){};
        OutputTag<TFundCashInfoVO> tFundCashInfoVOOutputTag = new OutputTag<TFundCashInfoVO>("tFundCashInfoVO"){};

        SingleOutputStreamOperator<TFundBondsVO> processTFundBondsVO = source.process(new ProcessFunction<ObjectNode, TFundBondsVO>() {
            @Override
            public void processElement(ObjectNode jsonNodes, Context context, Collector< TFundBondsVO> collector) throws Exception {
                String value = jsonNodes.get("value").toString();
                switch (jsonNodes.get("key").asInt()){
                    case 1:
                        TFundBondsVO tFundBondsVO = JSONUtil.getJsonToBean(value, TFundBondsVO.class);
                        String key = StringUtils.join(tFundBondsVO.getBatchNumber(), "_", tFundBondsVO.getLDate(), "_", tFundBondsVO.getLFundId());
                        // 将数据发送到常规输出
                        collector.collect(tFundBondsVO);
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
                        context.output(tFundCashInfoVOOutputTag,tFundCashInfoVO);
                        break;
                    default:
                        break;
                }
            }
        });

        DataStream<TFundSharesVO> tFundSharesVOSideOutput = processTFundBondsVO.getSideOutput(tFundSharesVOOutputTag);

        DataStream<TFundCashInfoVO> tFundCashInfoVOSideOutput = processTFundBondsVO.getSideOutput(tFundCashInfoVOOutputTag);

        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        String fileds = "batchNumber as batch_number,lDate as l_date,lFundId as l_fund_id,enBondsCash as en_bonds_cash";
        tableEnv.registerDataStream("t_fund_bonds",processTFundBondsVO);
        tableEnv.registerDataStream("t_fund_shares",tFundSharesVOSideOutput);
        tableEnv.registerDataStream("t_fund_cashInfo",tFundCashInfoVOSideOutput);

        String query = "select batchNumber,lDate,lFundId, enBondsCash from t_fund_bonds";
        Table table = tableEnv.sqlQuery(query);

        String resultSql = "select batchNumber,lDate,lFundId, enBondsCash from t_fund_bonds  ";

        Table resultTable = tableEnv.sqlQuery(resultSql);

        DataStream<TFundRiskVO> tFundBondsVODataStream = tableEnv.toAppendStream(resultTable, TFundRiskVO.class);
        tFundBondsVODataStream.print();


        env.execute("StreamingFromCollection");

    }
}
