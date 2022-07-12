package lids.ad.wuliang.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lids.ad.wuliang.config.Category;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.exception.RequestMsgFormatException;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.model.AdRequest;
import lids.ad.wuliang.service.AppRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/index")
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private final AppRunner app;
    private final Target target;
    @Autowired
    public QueryController(AppRunner app,Target target){
        this.app = app;
        this.target = target;
    }
    @PostMapping
    public List<AdInfo> query(@RequestBody AdRequest req) throws RequestMsgFormatException {
        if (!checkFormat(req,target)) {
            throw new RequestMsgFormatException("Request format error");
        }
        return app.getIndexServiceManager().getService().query(req);
    }

    public boolean checkFormat(AdRequest req,Target targetCfg) {
        return checkFormat(req.getUserProfile(),targetCfg);
    }
    private boolean checkFormat(HashMap<String, Object> taMap,Target targetCfg) {
        boolean blRet = true;
        ObjectMapper objectMapper = new ObjectMapper();
        for (String targetName : taMap.keySet()) {
            Category category = targetCfg.getCategory().get(targetName);
            if (category == null) {
                logger.warn("index service not support this target: "+ targetName);
                continue;
            }
            try {
                if (category.isSingle_value()) {
                    Integer dim = objectMapper.readValue(taMap.get(targetName).toString(),Integer.class);
                    logger.debug("targetName=" + targetName + ", value=" + dim);
                } else {
                    var dims = objectMapper.readValue(taMap.get(targetName).toString(),new TypeReference<ArrayList<Integer>>(){});
                    logger.debug("targetName=" + targetName + ", value=" +  dims);
                }
            } catch (IOException e) {
                blRet = false;
                logger.debug("checkFormat exception: "+e.getMessage());
            }
        }
        return blRet;
    }
}
