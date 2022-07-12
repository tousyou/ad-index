package lids.ad.wuliang.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "targets")
public class Target {
    private List<String> calc_sequence;
    private Map<String,Category> category;
    @Override
    public String toString(){
        return "Target [calc_sequence=" + calc_sequence + ", category=" + category +"]";
    }
}
