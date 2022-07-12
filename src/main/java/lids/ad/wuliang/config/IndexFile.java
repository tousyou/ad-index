package lids.ad.wuliang.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "index-file")
public class IndexFile {
    private String index_name;
    private String snapshot_path;
    private String history_path;
    private String dumper_path;
    @Override
    public String toString(){
        return "IndexFile [index_name=" + index_name + ", snapshot_path=" + snapshot_path + ", history_path=" + history_path + ", dumper_path=" + dumper_path +"]";
    }
}
