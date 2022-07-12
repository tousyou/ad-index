package lids.ad.wuliang.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
public class AdForwardIndex {
    private final ConcurrentHashMap<Integer,AdInfo> idx=new ConcurrentHashMap<>();
    public AdInfo put(AdInfo ad){
        return idx.put(ad.getAdId(),ad);
    }
    public AdInfo remove(int adId){
        return idx.remove(adId);
    }
    public AdInfo get(int  adId){
        return idx.get(adId);
    }
    public List<AdInfo> get(){
        return new ArrayList<>(idx.values());
    }
    public boolean contains(int adId){
        return idx.containsKey(adId);
    }
    public void clear() {
        idx.clear();
    }

}
