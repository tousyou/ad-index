package lids.ad.wuliang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.Kafka;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class CanalKafkaConsumerTest {
    @Autowired
    IndexServiceManager indexServiceManager;
    @Autowired
    Kafka kafka;
    private final ScheduledExecutorService kafkaScheduler = Executors.newSingleThreadScheduledExecutor();
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void run() {
        try {
            //start
            CanalKafkaConsumer kafkaConsumer = new CanalKafkaConsumer(kafka);
            IndexService indexService = indexServiceManager.getService();
            indexService.start(indexServiceManager,IndexServiceManager::startCallback);
            kafkaConsumer.setIndexService(indexService);
            kafkaConsumer.setIndexServiceManager(indexServiceManager);
            kafkaConsumer.seekOffsetsForTimes(1652671385793L);
            kafkaConsumer.setCallback(IndexServiceManager::startCallback);
            kafkaScheduler.scheduleWithFixedDelay(kafkaConsumer, 0, 100, TimeUnit.MILLISECONDS);
            if(!kafkaScheduler.awaitTermination(2000,TimeUnit.MILLISECONDS)){
                System.err.println("kafka thread shutdown failed");
            }
            Thread.sleep(100);

            //reload
            for(int i=0; i<5;i++) {
                kafkaConsumer = new CanalKafkaConsumer(kafka);
                IndexService standbyIndexService = indexServiceManager.getStandbyService();
                kafkaConsumer.setIndexService(standbyIndexService);
                kafkaConsumer.setIndexServiceManager(indexServiceManager);
                kafkaConsumer.seekOffsetsForTimes(1652671385793L);
                kafkaConsumer.setCallback(IndexServiceManager::reloadCallback);
                kafkaScheduler.scheduleWithFixedDelay(kafkaConsumer, 0, 100, TimeUnit.MILLISECONDS);
                if(!kafkaScheduler.awaitTermination(2000,TimeUnit.MILLISECONDS)){
                    System.err.println("kafka thread shutdown failed");
                }
                Thread.sleep(100);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}