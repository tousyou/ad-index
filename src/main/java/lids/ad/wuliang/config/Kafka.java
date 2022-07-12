package lids.ad.wuliang.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "kafka")
public class Kafka {
    private String bootstrap_servers;
    private String group_id;
    private String topic;
    private int partition;
    private int batch_size;
    @Override
    public String toString(){
        return "Kafka [bootstrap_servers=" + bootstrap_servers + ", group_id=" + group_id + ", topic=" + topic + ", partition=" + partition + ", batch_size=" + batch_size +"]";
    }
}
