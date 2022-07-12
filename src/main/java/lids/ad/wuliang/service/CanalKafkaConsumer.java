package lids.ad.wuliang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import lids.ad.wuliang.config.Kafka;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.CanalMessage;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Setter
@Service
@Scope("prototype")
public class CanalKafkaConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CanalKafkaConsumer.class);
    private static final String SQL_TYPE_DELETE = "DELETE";
    private static final String SQL_TYPE_UPDATE = "UPDATE";
    private static final String SQL_TYPE_INSERT = "INSERT";

    private Kafka kafka;
    private KafkaConsumer<Integer, String> consumer;

    //kafka客户端消费线程启动后，会从索引快照时间戳开始回追消息，该变量控制消息是否回追完成
    private boolean consumerFinish = false;

    private IndexService indexService;
    private IndexServiceManager indexServiceManager;
    private IndexService.ServiceCallback callback;

    @Autowired
    public CanalKafkaConsumer(Kafka kafka){
        this.kafka = kafka;

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrap_servers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafka.getGroup_id());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "40000"); // 必须大于session.timeout.ms的设置
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");  // "latest":如果没有offset则从最后的offset开始读
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(kafka.getBatch_size()));

        this.consumer = new KafkaConsumer<>(properties);
        TopicPartition topicPartition = new TopicPartition(this.kafka.getTopic(), this.kafka.getPartition());
        if (consumer != null) {
            consumer.assign(Collections.singletonList(topicPartition));
        }

        logger.info("CanalKafkaConsumer Constructor");
    }

    @PreDestroy
    public void destroy(){
        if(consumer != null){
            consumer.close();
            consumer = null;
        }
    }

    public void seekOffsetsForTimes(long timestamp) {
        Map<TopicPartition, Long> timestamps = new HashMap<>();
        TopicPartition topicPartition = new TopicPartition(this.kafka.getTopic(), this.kafka.getPartition());
        timestamps.put(topicPartition, timestamp);
        Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes = consumer.offsetsForTimes(timestamps);
        OffsetAndTimestamp offsetAndTimestamp = offsetsForTimes.get(topicPartition);
        if (offsetAndTimestamp != null){
            consumer.seek(topicPartition,offsetAndTimestamp.offset());
        }else{
            //当指定Topic+Partition还没有消息时，会返回如下错误
            logger.error("Topic["+this.kafka.getTopic()+"] Partition["+this.kafka.getPartition()+"] can not find offset by timestamp:"+timestamp);
        }
        logger.info(offsetsForTimes.toString());
    }

    @Override
    public void run() {
        logger.debug("kafka consumer scheduleWithFixedDelay run start");
        try{
            ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofSeconds(5));
            for (ConsumerRecord<Integer, String> record : records) {
                //System.out.println(groupId + " received message : from partition " + record.partition() + ", (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
                ObjectMapper objectMapper = new ObjectMapper();
                CanalMessage msg = objectMapper.readValue(record.value(), CanalMessage.class);
                logger.debug(msg.toString());
                //更新索引
                exec(msg);
            }
            consumer.commitSync();
            if ((!this.consumerFinish) && (records.count() < this.kafka.getBatch_size())) {
                //从索引快照时间戳回追队列中最新消息完成，索引服务启动
                this.callback.exec(this.indexServiceManager,true);
                this.consumerFinish = true;
            }
        } catch (Exception e) {
            logger.error("Thread exiting with uncaught exception:"+e);
            if(!this.consumerFinish) {
                this.callback.exec(this.indexServiceManager, false);
            }
        }
        logger.debug("kafka consumer scheduleWithFixedDelay run stop");
    }
    private void exec(CanalMessage msg){
        String sqlType = msg.getType();
        for(Map<String,String> data : msg.getData()){
            if (sqlType.equals(SQL_TYPE_INSERT) || sqlType.equals(SQL_TYPE_UPDATE)){
                AdInfo adInfo = convert(data);
                this.indexService.put(adInfo);
            }else if (sqlType.equals(SQL_TYPE_DELETE)){
                AdInfo adInfo = convert(data);
                this.indexService.remove(adInfo.getAdId());
            }else{
                logger.error("msg type not supported for type: "+ sqlType);
            }
        }
    }
    private AdInfo convert(Map<String,String> data){
        AdInfo adInfo = new AdInfo();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            adInfo = objectMapper.readValue(json,AdInfo.class);
            logger.debug(adInfo.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return adInfo;
    }
}
