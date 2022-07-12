package lids.ad.wuliang.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdRequest;
import lids.ad.wuliang.service.AppRunner;
import lids.ad.wuliang.tool.IndexTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class QueryControllerTest {
    @Autowired
    AppRunner app;
    @Autowired
    Target targetCfg;

    @Test
    void checkFormat() {
        for(int i = 0; i<1;i++) {
            AdRequest adReq = new AdRequest();
            Random random = new Random();
            int id = random.nextInt(Integer.MAX_VALUE);
            adReq.setReqId(id);
            adReq.setUid(id / 10);
            adReq.setUserProfile(IndexTool.createUserProfileTarget(targetCfg));
            //System.out.println(IndexTool.toJsonString(adReq));

            QueryController queryController = new QueryController(app,targetCfg);
            assertTrue(queryController.checkFormat(adReq, targetCfg));

            HashMap<String, Object> taMap = adReq.getUserProfile();
            ArrayList<Integer> ages = new ArrayList<>();
            ages.add(1);
            ages.add(2);
            taMap.put("ages", ages);
            adReq.setUserProfile(taMap);
            //assertFalse(queryController.checkFormat(adReq, targetCfg));
        }
    }

}