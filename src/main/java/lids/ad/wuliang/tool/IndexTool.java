package lids.ad.wuliang.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdRequest;

import java.io.IOException;
import java.util.*;

@NoArgsConstructor
public class IndexTool {
    public static int AD_MAX_COUNT = 100;
    public static AdRequest createAdRequest(Target targetCfg){
        Random random = new Random();
        AdRequest adReq = new AdRequest();
        int id = random.nextInt(Integer.MAX_VALUE);
        adReq.setReqId(id);
        adReq.setUid(id/10);
        //adReq.setUser(createUserTarget(targetCfg));
        adReq.setUserProfile(createUserProfileTarget(targetCfg));
        return adReq;
    }
    public static HashMap<String,Object> createUserProfileTarget(Target targetCfg){
        HashMap<String, Object> targets = new HashMap<>();
        for (String targetName : targetCfg.getCalc_sequence()) {
            Random random = new Random();
            int dim_max = targetCfg.getCategory().get(targetName).getDim_max();
            int dim_unknown = targetCfg.getCategory().get(targetName).getDim_unknown();
            boolean isNor = targetCfg.getCategory().get(targetName).isOperator_nor();
            if(isNor){
                continue;
            }
            boolean isSingle = targetCfg.getCategory().get(targetName).isSingle_value();
            int bound = dim_max - dim_unknown;
            int total = random.nextInt(bound);

            Set<Integer> set = new HashSet<>();
            for (int i = 0; i < total; i++) {
                set.add(dim_unknown + random.nextInt(bound) + 1);
            }
            if (set.size() > 0) {
                if(isSingle) {
                    targets.put(targetName, set.toArray()[0]);
                }else {
                    ArrayList<Integer> dims = new ArrayList<>(set);
                    targets.put(targetName, dims);
                }
            }
        }
        return targets;
    }
    public static String createUserTarget(Target targetCfg){
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(createUserProfileTarget(targetCfg));
            //System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
    public static AdInfo createAdInfo(Target targetCfg){
        Random random = new Random();
        AdInfo ad = new AdInfo();
        int adId = random.nextInt(Integer.MAX_VALUE);
        ad.setAdId(adId);
        ad.setAdName("adName" + adId);
        ad.setStatus(adId%2);
        //ad.setTarget(createTarget(targetCfg));
        ad.setTargets(createAdTarget(targetCfg));
        return ad;
    }
    public static void mockIndex(Target targetCfg){
        for(int i=0; i<AD_MAX_COUNT;i++){
            AdInfo ad = createAdInfo(targetCfg);
            try {
                ObjectMapper mapper = new ObjectMapper();
                System.out.println(mapper.writeValueAsString(ad));
            }catch (IOException e) {
                e.printStackTrace();
            }
            //WuliangApplication.indexService.put(ad,targetCfg);
        }
        //WuliangApplication.indexService.dump(targetCfg);
    }
    public static String createTarget(Target targetConfig){
        String json = "";
        HashMap<String, HashSet<Integer>> targets = createAdTarget(targetConfig);
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(targets);
            //System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
    public static HashMap<String, HashSet<Integer>> createAdTarget(Target targetCfg) {
        HashMap<String, HashSet<Integer>> targets = new HashMap<>();
        for (String targetName : targetCfg.getCalc_sequence()) {
            Random random = new Random();
            int dim_max = targetCfg.getCategory().get(targetName).getDim_max();
            int dim_unknown = targetCfg.getCategory().get(targetName).getDim_unknown();
            boolean isNor = targetCfg.getCategory().get(targetName).isOperator_nor();
            int bound = dim_max - dim_unknown;
            int total = random.nextInt(bound);

            HashSet<Integer> set = new HashSet<>();
            for (int i = 0; i < total; i++) {
                if (isNor) {
                    set.add(dim_unknown + random.nextInt(bound) + 1);
                } else {
                    set.add(dim_unknown + random.nextInt(bound+1));
                }
            }
            if (set.size() > 0) {
                targets.put(targetName, set);
            }
        }
        return targets;
    }
    public static String toJsonString(Object obj){
        String json = "";
        try{
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(obj);
        }catch (IOException e){
            e.printStackTrace();
        }
        return json;
    }
}
