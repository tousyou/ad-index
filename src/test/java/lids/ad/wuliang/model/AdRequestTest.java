package lids.ad.wuliang.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.tool.IndexTool;

import java.util.Random;

@SpringBootTest
class AdRequestTest {
    @Autowired
    Target targetCfg;
    @Test
    void checkFormat() {
        AdRequest adReq = new AdRequest();
        Random random = new Random();
        int id = random.nextInt(Integer.MAX_VALUE);
        adReq.setReqId(id);
        adReq.setUid(id/10);
        adReq.setUserProfile(IndexTool.createUserProfileTarget(targetCfg));
        System.out.println(IndexTool.toJsonString(adReq));
        //{"reqId":1889436086,"uid":188943608,"user":null,"location":null,"device":null,"app":null,"limit":null,"userProfile":{"ages":202,"interests":[10002,10003,10004,10005]}}
    }
}