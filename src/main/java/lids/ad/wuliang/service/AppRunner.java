package lids.ad.wuliang.service;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lids.ad.wuliang.config.Category;
import lids.ad.wuliang.config.Kafka;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdRequest;
import lids.ad.wuliang.tool.IndexTool;

import java.util.List;
import java.util.Properties;

@Getter
@Service
public class AppRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);
    private final IndexServiceManager indexServiceManager;
    private final Target target;
    private final Kafka kafka;

    @Autowired
    public AppRunner(IndexServiceManager indexServiceManager,Target target,Kafka kafka) {
        this.indexServiceManager = indexServiceManager;
        this.target = target;
        this.kafka = kafka;
        logger.debug("AppRunner Constructor");
    }

    //每日凌晨3点执行一次
    @Scheduled(cron = "0 0 3 * * ?")
    public void reload(){
        logger.debug("index service reload start");
        if(this.indexServiceManager.reload()){
            logger.info("index service reload success");
        }else{
            logger.warn("index service reload failed");
        }
        logger.debug("index service reload finish");
    }

    //每30分钟执行一次
    @Scheduled(cron = "0 30 * * * ?")
    public void dump(){
        logger.debug("index service dump start");
        if (this.indexServiceManager.dump()){
            logger.info("index service dump success");
        }else{
            logger.warn("index service dump failed");
        }
        logger.debug("index service dump finish");
    }

    //每1分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void query(){
        AdInfo adInfo = IndexTool.createAdInfo(target);
        this.indexServiceManager.getService().put(adInfo);
        AdRequest adRequest = IndexTool.createAdRequest(target);
        logger.info("return ad.size=" + this.indexServiceManager.getService().query(adRequest).size() + ",query by " + adRequest);
    }

    @Override
    public void run(String... args) throws Exception {
        if(!checkTargetConfigFormat()){
            logger.error("Target config format error");
            return;
        }
        if(!checkKafka()){
            logger.error("Kafka config format error");
            return;
        }
        this.indexServiceManager.start();
    }

    public boolean checkTargetConfigFormat(){
        //1、计算序列中的定向必须有明确的配置
        //2、排除包必须在计算序列的末尾
        boolean excludeIsLast = false;
        for(String targetName: this.target.getCalc_sequence()){
            Category category = this.target.getCategory().get(targetName);
            if (category == null) {
                logger.error("you must config target[" + targetName + "] " + "in category");
                return false;
            }
            if (!excludeIsLast && category.isOperator_nor()) {
                excludeIsLast = true;
            }
            if (excludeIsLast){
                if (!category.isOperator_nor()){
                    logger.error("you must config operator_nor target[" + targetName + "] " + "in the last sequence");
                    return false;
                }
            }
        }
        return  true;
    }
    public boolean checkKafka(){
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
        //topic列表
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        //循环判断是否存在
        List<PartitionInfo> partitionInfoList = consumer.partitionsFor(kafka.getTopic());
        if (partitionInfoList == null){
            logger.error("kafka topic["+kafka.getTopic()+"] is not exist");
            consumer.close();
            return false;
        }

        for (PartitionInfo e :partitionInfoList){
            if(e.partition() == kafka.getPartition()){
                consumer.close();
                return true;
            }
        }
        logger.error("kafka topic["+kafka.getTopic()+"] partition["+kafka.getPartition()+"] is not exist");
        consumer.close();
        return false;
    }

}
