package lids.ad.wuliang.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdRequest;

import java.io.IOException;

@SpringBootTest
class IndexToolTest {
    @Autowired
    Target target;
    @Test
    void createAdInfo() {
        AdInfo ad = IndexTool.createAdInfo(target);
        System.out.println("adId="+ad.getAdId());
    }

    @Test
    void mockIndex() {
    }

    @Test
    void createTarget() {
    }

    @Test
    void createAdRequest() {
        AdRequest adReq = IndexTool.createAdRequest(target);
        System.out.println(adReq.getReqId());
        try{
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(adReq));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Test
    void createUserTarget() {
    }
}