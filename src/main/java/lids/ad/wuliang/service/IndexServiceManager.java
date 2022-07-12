package lids.ad.wuliang.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class IndexServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(IndexServiceManager.class);

    private static final int INITIALIZING=0;
    private static final int STARTING=1;
    private static final int RUNNING=2;
    private static final int RELOADING=3;

    private final IndexService activeIndexService;
    private final IndexService standbyIndexService;

    private final Lock indexLock = new ReentrantLock();

    private volatile int status = INITIALIZING;
    private final AtomicBoolean activeFace = new AtomicBoolean(true);

    @Autowired()
    public IndexServiceManager(IndexService activeIndexService,IndexService standbyIndexService){
        this.activeIndexService = activeIndexService;
        this.standbyIndexService = standbyIndexService;
        logger.info("IndexServiceManager Constructor");
    }

    public void clear() {
        this.standbyIndexService.clear();
        this.activeIndexService.clear();
        logger.debug("IndexServiceManager clear");
    }
    //系统初始化时，主线程会调用start方法启动索引服务
    public void start(){
        indexLock.lock();
        //确保只有一个初始化线程在启动
        if(status != INITIALIZING){
            indexLock.unlock();
            logger.info("Index Service is not initializing state");
            return;
        }
        status = STARTING;
        IndexService activeIndex = getService();
        if(!activeIndex.start(this,IndexServiceManager::startCallback)){
            this.status = INITIALIZING;
            logger.error("index service start failed");
        }
        indexLock.unlock();
    }
    //索引启动完成回掉函数
    public void startCallback(boolean success){
        indexLock.lock();
        if(success) {
            this.status = RUNNING;
            //通知zookeeper，加入索引服务集群，接受查询服务
            logger.info("index service start success");
        }else{
            this.status = INITIALIZING;
            logger.error("index service start failed");
        }
        indexLock.unlock();
    }

    //周期（每日凌晨）或者收到紧急请求时，重建索引
    public boolean reload(){
        indexLock.lock();
        boolean reloadSuccess = true;
        //确保只有一个索引重置线程在运行
        if(this.status != RUNNING){
            logger.info("index service is not running state");
            indexLock.unlock();
            return false;
        }
        this.status = RELOADING;

        IndexService standbyIndex = getStandbyService();
        if(!standbyIndex.start(this,IndexServiceManager::reloadCallback)){
            logger.error("index service reload failed");
            this.status = RUNNING;
            //停止standby的kafka消费线程
            standbyIndex.clear();
            reloadSuccess = false;
        }
        indexLock.unlock();
        return reloadSuccess;
    }
    //reload完成回掉函数，索引切换
    public void reloadCallback(boolean success){
        indexLock.lock();
        if(success) {
            //索引重建成功，交换active/standby
            if (this.activeFace.compareAndSet(true,false)){
                logger.warn("index service load success [active face -----> standby face]");
            }else if(this.activeFace.compareAndSet(false,true)){
                logger.warn("index service load success [standby face -----> active face]");
            }
            try {
                //等待1s，期待前端应用都切换到新索引
                Thread.sleep(1000);
            }catch (InterruptedException e){
                logger.error(e.getMessage());
            }
        }else {
            logger.error("index service reload failed");
        }
        this.status = RUNNING;
        //停止standby的kafka消费线程
        getStandbyService().clear();
        //standbyIndex.getKafkaConsumer().doStop();
        indexLock.unlock();
    }
    public IndexService getService(){
        if (this.activeFace.get()){
            return activeIndexService;
        }else{
            return standbyIndexService;
        }
    }
    public IndexService getStandbyService(){
        if (this.activeFace.get()){
            return standbyIndexService;
        }else{
            return activeIndexService;
        }
    }
    public boolean running(){
        return ((this.status==RUNNING)||(this.status==RELOADING));
    }
    public boolean dump(){
        if(this.status != RUNNING) {
            logger.info("Index service status is not RUNNING:[status="+this.status+"]");
            return false;
        }
        return this.getService().dump();
    }
}
