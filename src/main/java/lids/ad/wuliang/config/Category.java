package lids.ad.wuliang.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String alias;
    private int dim_min;
    private int dim_max;
    private int dim_all;
    private int dim_unknown;
    private boolean single_value;
    private boolean operator_nor;
    @Override
    public String toString(){
        return "Category [alias=" + alias + ", dim_min=" + dim_min + ", dim_max=" + dim_max + ", dim_all=" + dim_all
                + ", dim_unknown=" + dim_unknown+ ", single_value=" + single_value + ", operator_nor=" + operator_nor+"]";
    }
}
