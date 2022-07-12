package lids.ad.wuliang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanalMessage implements Serializable {

    private static final long         serialVersionUID = -3386650678735860050L;
    private long                      id;
    private String                    database;
    private String                    table;
    private List<String> pkNames;
    private Boolean                   isDdl;
    private String                    type;
    // binlog executeTime
    private Long                      es;
    // dml build timeStamp
    private Long                      ts;
    private String                    sql;
    private Map<String, Integer> sqlType;
    private Map<String, String>       mysqlType;
    private List<Map<String, String>> data;
    private List<Map<String, String>> old;

    @Override
    public String toString() {
        return "CanalMessage [id=" + id + ", database=" + database + ", table=" + table + ", isDdl=" + isDdl + ", type="
                + type + ", es=" + es + ", ts=" + ts + ", sql=" + sql + ", sqlType=" + sqlType + ", mysqlType="
                + mysqlType + ", data=" + data + ", old=" + old + "]";
    }
}
