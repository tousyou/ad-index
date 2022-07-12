package lids.ad.wuliang.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lids.ad.wuliang.config.Target;
import lids.ad.wuliang.tool.IndexTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdInvertedIndexTest {
    @Autowired
    Target targetCfg;
    private static final int CHECK_TOTAL = 10;
    private HashMap<Integer, AdInfo> adInfoHashMap = new HashMap<>();
    private AdInvertedIndex invIdx;

    @BeforeEach
    void setUp() {
        invIdx = new AdInvertedIndex(targetCfg);
        IndexTool tool = new IndexTool();
        for (int i = 0; i < CHECK_TOTAL; i++) {
            AdInfo adSrc = tool.createAdInfo(targetCfg);
            adInfoHashMap.put(adSrc.getAdId(), adSrc);
            invIdx.updateIndex(adSrc);
            //System.out.println(IndexTool.toJsonString(adSrc));
        }
        System.out.println("asList size =" + adInfoHashMap.size());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void init() {
    }

    @Test
    void clear() {
    }

    @Test
    void remove() {

    }

    @Test
    void updateIndex() {
        for (AdInfo adSrc : adInfoHashMap.values()) {
            AdInfo adDst = invIdx.getAdInfo(adSrc.getAdId());
            assertTrue(adSrc.equals(adDst));
        }
    }

    @Test
    void query() {
        //System.out.println(IndexTool.toJsonString(targetCfg));
        for (int i = 0; i < CHECK_TOTAL; i++) {
            AdRequest adReq = IndexTool.createAdRequest(targetCfg);
            HashMap<String, Object> userTargets = adReq.getUserProfile();
            RoaringBitmap rb = invIdx.query(userTargets);
            //System.out.println(rb.toString());
            for (int adId : rb.toArray()) {
                AdInfo ad = adInfoHashMap.get(adId);
                HashMap<String, HashSet<Integer>> adTargets = ad.getTargets();
                for (String targetName : targetCfg.getCategory().keySet()) {
                    if (adTargets.get(targetName) == null) {
                        //计划在该定向通投
                        continue;
                    }
                    ArrayList<Integer> adDims = new ArrayList<>();
                    for(int dim : adTargets.get(targetName)){
                        adDims.add(dim);
                    }
                    boolean isSingle = targetCfg.getCategory().get(targetName).isSingle_value();
                    boolean isNor = targetCfg.getCategory().get(targetName).isOperator_nor();
                    if (isSingle) {
                        int userDim = targetCfg.getCategory().get(targetName).getDim_unknown();
                        if (userTargets.containsKey(targetName)) {
                            userDim = (int) userTargets.get(targetName);
                        }
                        if (!adDims.contains(userDim)) {
                            System.out.println("ad target[" + targetName + "] not include user's dim[ " + userDim + "] error2");
                            System.out.println(IndexTool.toJsonString(adReq));
                            System.out.println(IndexTool.toJsonString(ad));
                        }
                        assertTrue(adDims.contains(userDim));
                    } else {
                        ArrayList<Integer> userDims = new ArrayList<>();
                        userDims.add(targetCfg.getCategory().get(targetName).getDim_unknown());
                        if (userTargets.containsKey(targetName)) {
                            userDims = (ArrayList<Integer>) userTargets.get(targetName);
                        }
                        if (isNor) {
                            /*
                            if (adDims.removeAll(userDims)) {
                                System.out.println("ad target[" + targetName + "] include user's dim error3");
                                System.out.println(IndexTool.toJsonString(adReq));
                                System.out.println(IndexTool.toJsonString(ad));
                            }

                             */
                            assertFalse(adDims.removeAll(userDims));
                        } else {
                            /*
                            if (!adDims.removeAll(userDims)) {
                                System.out.println("ad target[" + targetName + "] not include user's dim error4 " + userDims.toString());
                                System.out.println(IndexTool.toJsonString(adReq));
                                System.out.println(IndexTool.toJsonString(ad));
                            }

                             */
                            assertTrue(adDims.removeAll(userDims));
                        }
                    }
                }
            }
        }
    }

    @Test
    void get() {

    }

    @Test
    void testToString() {
    }

    @Test
    void testUpdateIndex() {
    }

    @Test
    void testGet() {
    }
}