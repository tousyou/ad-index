package lids.ad.wuliang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class AdRequest {
    private long reqId;
    private long uid;
    //userProfile的格式要求：
    // {"genders":101,"ages":201,"interests":[10002,10003,10005],"packages":[1000002]}
    private HashMap<String,Object> userProfile;
    private HashMap<String,Object> location;
    private HashMap<String,Object> device;
    private HashMap<String,Object> app;
    private HashMap<String,Object> limit;
    @Override
    public String toString(){
        return "AdRequest [reqId=" + reqId + ", uid=" + uid + ", userProfile=" + userProfile + ", location=" + location
                + ", device=" + device + ", app=" + app + ", limit=" + limit +"]";
    }
}
