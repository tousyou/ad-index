package lids.ad.wuliang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WuliangApplication {
    public static void main(String[] args) {
        SpringApplication.run(WuliangApplication.class, args);
    }
}
