package com.xunce.stream;

import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/27
 */
public class SplitAndSelectTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Long> source = env.generateSequence(0, 20);
        SplitStream<Long> splitStream = source.split(new OutputSelector<Long>() {
            @Override
            public Iterable<String> select(Long value) {
                List<String> out = new ArrayList<>();
                if (value % 2 == 0) {
                    out.add("even");
                } else {
                    out.add("odd");
                }
                return out;

            }
        });
        DataStream<Long> even = splitStream.select("even");
        even.print();
        env.execute();
    }
}
