package lids.ad.wuliang.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import lids.ad.wuliang.config.Category;
import lids.ad.wuliang.config.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
public class AdInvertedIndex {
    private static final Logger logger = LoggerFactory.getLogger(AdInvertedIndex.class);
    private final Target target;
    //广告是否可以投放{1：可以投放 0：不可以}
    private final RoaringBitmapEx onLineRb;
    private final ConcurrentHashMap<String, Matrix> adIndex;

    @Autowired
    public AdInvertedIndex(Target target){
        onLineRb = new RoaringBitmapEx();
        adIndex = new ConcurrentHashMap<>();
        this.target = target;
        logger.info("AdInvertedIndex Constructor");
    }

    public void clear(){
        onLineRb.clear();
        for(Matrix mtx : adIndex.values()){
            mtx.clear();
        }
    }
    public void remove(int adId){
        for(Matrix mtx : adIndex.values()){
            mtx.remove(adId);
        }
        onLineRb.remove(adId);
    }

    public void updateIndex(AdInfo adInfo) {
        //广告定向，格式如:{"genders":[101,102],"ages":[201,202,203,205]}
        HashMap<String,HashSet<Integer>> taMap = adInfo.getTargets();
        if(taMap == null){
            //表明该计划全人群通投
            logger.debug("targets is null for: "+adInfo);
        }
        //计划先暂停再更新
        this.onLineRb.remove(adInfo.getAdId());
        this.remove(adInfo.getAdId());

        //根据配置文件中支持的定向域，更新倒排索引
        for (String targetName : this.target.getCalc_sequence()) {
            Category category = this.target.getCategory().get(targetName);
            int dim_min = category.getDim_min();
            int dim_max = category.getDim_max();

            HashSet<Integer> dims = null;
            if(taMap != null) {
                dims = taMap.get(targetName);
            }
            if (dims == null) {
                //表明计划在改定向域通投
                if (category.isOperator_nor()) {
                    //如果是排除定向，默认都置为0（所有人群都不排除）
                    continue;
                }
                dims = new HashSet<>();
                dims.add(category.getDim_all());
            }

            //定向纬度校验
            for (Integer dim : dims) {
                if ((dim < dim_min) || (dim > dim_max)) {
                    logger.warn("Target[" + targetName + "] not support this dim:" + dim);
                    dims.remove(dim);
                }
            }

            //获取改定向域对应的向量矩阵Matrix
            Matrix mtx = adIndex.get(targetName);
            if (mtx == null) {
                logger.debug("Matrix not exist this target:" + targetName);
                mtx = new Matrix();
                adIndex.put(targetName, mtx);
            }
            //更新Matrix
            mtx.updateColumn(adInfo.getAdId(), dims);
        }

        //更新在线状态
        if(adInfo.getStatus() == 1){
            this.onLineRb.add(adInfo.getAdId());
        }else{
            this.onLineRb.remove(adInfo.getAdId());
        }
    }

    public RoaringBitmap query(HashMap<String,Object> targets) {
        RoaringBitmap retRb = new RoaringBitmap();
        this.onLineRb.threadSafeOr(retRb);
        ObjectMapper objectMapper = new ObjectMapper();

        for(String targetName : this.target.getCalc_sequence()){
            Category category = this.target.getCategory().get(targetName);
            Matrix mtx = adIndex.get(targetName);
            if (mtx == null){
                //当前线上计划无视用户在该定向的值，也就是说所有计划在该定向上通投
                logger.debug("inverted index not include target [" + targetName + "]");
                continue;
            }

            ArrayList<Integer> rows = new ArrayList<>();
            rows.add(category.getDim_all());

            String alias = category.getAlias();
            Object obj = targets.get(targetName);
            if ((obj == null)&&(alias != null)){
                obj = targets.get(alias);
            }
            if (obj == null){
                //用户没有当前定向值，取在该定向域的通投计划
                logger.debug("user's query request not include target [" + targetName + "]");
                //给用户设置默认值
                rows.add(category.getDim_unknown());
            } else {
                if (category.isSingle_value()) {
                    rows.add((int) obj);
                } else {
                    try {
                        var dims = objectMapper.readValue(obj.toString(),new TypeReference<ArrayList<Integer>>(){});
                        rows.addAll(dims);
                    }catch (IOException e){
                        logger.error("user's query request format error for:"+ targetName+", msg:"+e.getMessage());
                    }
                }
            }
            RoaringBitmap targetRb = mtx.unionRows(rows);
            if (category.isOperator_nor()){
                retRb = RoaringBitmap.andNot(retRb, targetRb);
            }else {
                retRb = RoaringBitmap.and(retRb,targetRb);
            }
        }
        return retRb;
    }

    public AdInfo getAdInfo(int adId){
        AdInfo ad = new AdInfo();
        ad.setAdId(adId);
        if (onLineRb.contains(adId)){
            ad.setStatus(1);
        }else {
            ad.setStatus(0);
        }
        HashMap<String,HashSet<Integer>> taMap = new HashMap<>();
        for(String targetName : this.target.getCalc_sequence()){
            Matrix mtx = adIndex.get(targetName);
            if (mtx == null) {
                continue;
            }
            HashSet<Integer> dims = mtx.getColumn(adId);
            if ((dims != null) && (dims.size() > 0)) {
                if ((dims.size() == 1) &&
                        (dims.contains(this.target.getCategory().get(targetName).getDim_all()))){
                    continue;
                }
                taMap.put(targetName, dims);
            }
        }
        ad.setTargets(taMap);
        return ad;
    }

    private String getTargets(int adId){
        StringBuilder answer = new StringBuilder();
        answer.append("{");
        for(String key : adIndex.keySet()){
            Matrix mtx = adIndex.get(key);
            answer.append(key).append(":").append(mtx.getColumnString(adId)).append(",");
        }
        if (adIndex.size() > 0){
            answer.deleteCharAt(answer.length()-1);
        }
        answer.append("}");
        return answer.toString();
    }

    public String toString() {
        StringBuilder answer = new StringBuilder();
        answer.append("{");
        for(int adId : onLineRb.toArray()){
            answer.append("adId:").append(adId).append(", status:1, ").append("{");
            for(String targetName : this.target.getCalc_sequence()){
                Matrix mtx = adIndex.get(targetName);
                if (mtx != null){
                    answer.append(targetName).append(":").append(mtx.getColumnString(adId));
                }
            }
            answer.append("}");
        }
        answer.append("}");
        return answer.toString();
    }
}
