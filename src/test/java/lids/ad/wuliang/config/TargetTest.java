package lids.ad.wuliang.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TargetTest {
    @Autowired
    Target target;
    @BeforeEach
    void setUp() {
        System.out.println(target);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getCalc_sequence() {
        System.out.println(target);
    }

    @Test
    void getCategory() {
    }
}