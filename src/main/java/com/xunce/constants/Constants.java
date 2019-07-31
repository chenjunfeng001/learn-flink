package com.xunce.constants;

public interface Constants<struct> {
    /**
     * spark作业相关参数
     */
    String APP_NAME = "app_name";
    String SPARK_MASTER = "spark_master";
    String DURATION = "duration";
    String CHECK_SITE = "check_site";

    /**
     * Kafka配置
     */
    String BOOTSTRAP_SERVERS = "bootstrap_servers";

    String ZOOKEEPER_SERVERS = "zookeeper_servers";

    String KAFKA_TOPICS = "kafka.topics";

    String KAFKA_GROUPID = "kafka.groupId";

    String REDIS_DB_INDEX = "redis.db.index";

    String TDURATIONS_KAFKA_TOPIC = "tdurations_kafka_topic";

    String FETCH_SOURCE_THREAD_NUM = "fetch_source_thread_num";

    /**
     * kafka标识--资产类型分类
     */
    String Stock = "1";
    String Bonds = "2";
    String NoCurrBonds = "2_0";
    String CurrBonds = "2_1";
    String AssetInformation = "3";
    String AssetCash = "4";
    String Others = "5";
    String Foundation = "6";
    String Futures = "7";
    String Hgregister = "8";
    String Options = "9";

    /**
     * redis配置
     */

    String REDIS_MYMASTER = "redis.mymaster";
    String REDIS_SENTINELS = "redis.sentinels";
    String REDIS_SPLIT_PATTERN = "redis.split.pattern";
    String REDIS_PASSWORD = "redis.password";
    String REDIS_TIMEOUT = "redis.timeout";


}
