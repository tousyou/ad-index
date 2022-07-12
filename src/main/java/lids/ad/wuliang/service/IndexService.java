package lids.ad.wuliang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import lids.ad.wuliang.config.IndexFile;
import lids.ad.wuliang.config.Kafka;
import lids.ad.wuliang.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope("prototype")
public class IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final IndexFile indexFile;
    private final AdForwardIndex forwardIndex;
    private final AdInvertedIndex invertedIndex;
    private final Kafka kafka;
    private final ScheduledExecutorService kafkaScheduler = Executors.newSingleThreadScheduledExecutor();

    interface ServiceCallback{
        void exec(IndexServiceManager indexServiceManager,Boolean success);
    }

    @Autowired
    public IndexService(AdForwardIndex forwardIndex,AdInvertedIndex invertedIndex, IndexFile indexFile, Kafka kafka){
        this.indexFile = indexFile;
        this.kafka = kafka;
        this.forwardIndex = forwardIndex;
        this.invertedIndex = invertedIndex;
        logger.info("IndexService Constructor");
    }

    public AdInfo get(int id) {
        //invIndex.getAdInfo(id);
        return forwardIndex.get(id);
    }

    public AdInfo put(AdInfo ad) {
        invertedIndex.updateIndex(ad);
        return forwardIndex.put(ad);
    }

    public void remove(int id) {
        invertedIndex.remove(id);
        forwardIndex.remove(id);
    }
    public void clear(){
        try {
            if(!kafkaScheduler.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                logger.debug("kafkaScheduler awaitTermination failed");
            }
        }catch (InterruptedException e){
            logger.error(e.getMessage());
        }
        invertedIndex.clear();
        forwardIndex.clear();
    }

    public boolean contains(int id) {
        return forwardIndex.contains(id);
    }

    public List<AdInfo> get() {
        return forwardIndex.get();
    }


    public List<AdInfo> query(AdRequest req) {
        RoaringBitmap adRb = invertedIndex.query(req.getUserProfile());
        List<AdInfo> retArr = new ArrayList<>();
        for (int adId : adRb.toArray()) {
            retArr.add(forwardIndex.get(adId));
        }
        return retArr;
    }
    public boolean start(IndexServiceManager indexServiceManager,ServiceCallback serviceCallback){
        //先清空索引，避免脏数据
        invertedIndex.clear();
        forwardIndex.clear();

        //load索引snapshot,返回snapshot的时间戳
        long timestamp = loadSnapShot();
        if(timestamp == 0L){
            logger.error("index snapshot load failed");
            this.invertedIndex.clear();
            this.forwardIndex.clear();
            return false;
        }
        //启动kafka线程回追最新消息，消息回追完成后注册
        CanalKafkaConsumer kafkaConsumer = new CanalKafkaConsumer(this.kafka);
        kafkaConsumer.setIndexService(this);
        kafkaConsumer.setIndexServiceManager(indexServiceManager);
        kafkaConsumer.setCallback(serviceCallback);
        //从索引快照最后时间戳，开始回追消息
        kafkaConsumer.seekOffsetsForTimes(timestamp);
        kafkaScheduler.scheduleWithFixedDelay(kafkaConsumer, 0,1000,TimeUnit.MILLISECONDS);
        return true;
    }
    private long loadSnapShot(){
        //load索引snapshot，返回snapshot的时间戳
        long timestamp;
        String indexFile = this.indexFile.getSnapshot_path()+this.indexFile.getIndex_name();
        try {
            File file = new File(indexFile);
            if(!file.exists()){
                logger.error("index file is not exist for directory: "+indexFile);
                return 0L;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            ObjectMapper mapper = new ObjectMapper();
            //解析第一行全量索引时间戳、和增量索引编号
            String json = reader.readLine();
            IndexTimestamp indexTimestamp = mapper.readValue(json,IndexTimestamp.class);
            //记录索引snapshot的时间戳
            timestamp = indexTimestamp.getTimestamp();
            while ((json = reader.readLine()) != null){
                AdInfo adInfo = mapper.readValue(json,AdInfo.class);
                this.put(adInfo);
            }
            //索引snapshot读取完成，backup文件
            Timestamp date = new Timestamp(System.currentTimeMillis());
            String indexFile_bak = this.indexFile.getHistory_path()
                    +this.indexFile.getIndex_name()+"_"+timeFormat.format(date);
            mkdir(indexFile_bak);
            //Files.move(Paths.get(indexFile),Paths.get(indexFile_bak), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(indexFile),Paths.get(indexFile_bak), StandardCopyOption.REPLACE_EXISTING);

        }catch (IOException | SecurityException e ){
            e.printStackTrace();
            return 0L;
        }
        return timestamp;
    }
    public boolean dump(){
        String path = this.indexFile.getDumper_path();
        Date date = new Date(System.currentTimeMillis());
        path = path + "dump_" + timeFormat.format(date) +".json";
        try {
            File file = new File(path);
            if(!file.getParentFile().exists()){
                if(!file.getParentFile().mkdir()){
                    logger.error("can not create directory: "+path);
                    return false;
                }
            }
            List<AdInfo> adList = forwardIndex.get();
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            IndexTimestamp timestamp = new IndexTimestamp(System.currentTimeMillis(),0);
            ObjectMapper mapper = new ObjectMapper();
            writer.write(mapper.writeValueAsString(timestamp) + "\n");

            for(AdInfo adInfo : adList){
                AdInfo adInvertedIndex = invertedIndex.getAdInfo(adInfo.getAdId());
                adInvertedIndex.setAdName(adInfo.getAdName());
                String json = mapper.writeValueAsString(adInvertedIndex);
                writer.write(json + "\n");
            }
            writer.flush();
            writer.close();
        }catch (IOException | SecurityException e ){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void mkdir(String path){
        File file = new File(path);
        if((!file.exists()) &&(!file.getParentFile().exists())) {
            if(!file.getParentFile().mkdir()){
                logger.debug("mkdir failed for:"+path);
            }
        }
    }
}
