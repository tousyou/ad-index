package lids.ad.wuliang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AdInfo {
    private int adId;
    private String adName;
    private int status;

    //targets的格式要求：
    // {"genders":[101,102],"ages":[201,202,203,205],"interests":[10001,10002,10003,10005],
    // "packages":[1000001,1000002],"excludePackages":[1000003]}
    private HashMap<String, HashSet<Integer>> targets;

    public AdInfo(int adId, String adName, int status){
        this.adId = adId;
        this.adName = adName;
        this.status = status;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof AdInfo)) {
            return false;
        }
        AdInfo ad = (AdInfo) o;
        if (this == ad){
            return true;
        }

        return ((this.adId == ad.adId)
                && (this.status == ad.status)
                && (this.targets.equals(ad.targets)));
    }
    @Override
    public String toString(){
        return "AdInfo [adId=" + adId + ", adName=" + adName + ", status=" + status + ", targets=" + targets + "]";
    }

}
