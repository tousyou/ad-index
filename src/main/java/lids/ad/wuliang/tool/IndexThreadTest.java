package lids.ad.wuliang.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdRequest;
import lids.ad.wuliang.service.IndexService;

import java.util.List;
import java.util.Random;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class IndexThreadTest extends Thread{
    @Autowired
    Target targetCfg;
    private static int MAX_LOOP = 1000;
    private IndexService indexService;
    @Override
    public void run(){
        for(int i = 0; i<MAX_LOOP; i++){
            Random random = new Random();
            indexService.put(IndexTool.createAdInfo(targetCfg));
            List<AdInfo> adList = indexService.get();
            int count = adList.size();
            if(count != 0) {
                AdInfo adInfo = adList.get(random.nextInt(count));
                indexService.get(adInfo.getAdId());
                indexService.remove(adInfo.getAdId());
            }
            AdRequest adReq = IndexTool.createAdRequest(targetCfg);
            indexService.query(adReq);
            try {
                sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
