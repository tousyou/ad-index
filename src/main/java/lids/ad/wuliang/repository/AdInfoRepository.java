package lids.ad.wuliang.repository;

import lids.ad.wuliang.model.AdInfo;

import java.util.ArrayList;
import java.util.List;

public class AdInfoRepository {
    private final List<AdInfo> adInfoList = new ArrayList<>();
    public void add(AdInfo ad){
        adInfoList.add(ad);
    }
    public boolean delete(AdInfo ad){
        return adInfoList.remove(ad);
    }
    public void update(AdInfo ad){
        adInfoList.remove(ad);
        adInfoList.add(ad);
    }
    public AdInfo get(int  adId){
        AdInfo item = new AdInfo(adId,"",0);
        return adInfoList.get(adInfoList.indexOf(item));
    }
    public boolean contains(int adId){
        AdInfo item = new AdInfo(adId,"",0);
        return adInfoList.contains(item);
    }
    public List<AdInfo> get(){
        return adInfoList;
    }
    public void delete(int adId){
        AdInfo item = new AdInfo(adId,"",0);
        adInfoList.remove(item);
    }
    public void clear() {
        this.adInfoList.clear();
    }
}
