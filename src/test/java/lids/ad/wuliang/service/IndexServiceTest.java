package lids.ad.wuliang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.IndexFile;
import lids.ad.wuliang.config.Kafka;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdForwardIndex;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdInvertedIndex;
import lids.ad.wuliang.tool.IndexThreadTest;
import lids.ad.wuliang.tool.IndexTool;

@SpringBootTest
class IndexServiceTest {
    @Autowired
    Target targetCfg;
    @Autowired
    IndexFile indexFile;
    @Autowired
    Kafka kafka;
    private static final int CHECK_TOTAL=10;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void get() {
    }

    @Test
    void put() {
        AdInvertedIndex invertedIndex = new AdInvertedIndex(targetCfg);
        AdForwardIndex forwardIndex = new AdForwardIndex();
        IndexService indexService = new IndexService(forwardIndex,invertedIndex,indexFile,kafka);
        IndexTool tool = new IndexTool();
        for (int i = 0; i < CHECK_TOTAL; i++) {
            AdInfo adSrc = tool.createAdInfo(targetCfg);
            indexService.put(adSrc);
        }

        for(int i=0; i<10; i++){
            IndexThreadTest indexThreadTest = new IndexThreadTest(targetCfg,indexService);
            indexThreadTest.start();
        }
    }

    @Test
    void testGet() {
    }

    @Test
    void remove() {
    }

    @Test
    void query() {
    }

    @Test
    void dump() {
        AdInvertedIndex invertedIndex = new AdInvertedIndex(targetCfg);
        AdForwardIndex forwardIndex = new AdForwardIndex();
        IndexService indexService = new IndexService(forwardIndex,invertedIndex,indexFile,kafka);
        IndexTool tool = new IndexTool();
        for (int i = 0; i < CHECK_TOTAL; i++) {
            AdInfo adSrc = tool.createAdInfo(targetCfg);
            indexService.put(adSrc);
        }
        indexService.dump();
    }
}