package lids.ad.wuliang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndexTimestamp {
    private long timestamp;
    private int incremental_sequence;
}
