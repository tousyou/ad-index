package lids.ad.wuliang.model;

import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/*
       column1    column2    column3    .......    .......    columnM
-----|----------|----------|----------|----------|----------|----------|
row1 |    1     |    1     |    0     |    0     |    1     |    1     |   RoaringBitmap
row2 |    1     |    0     |    0     |    0     |    1     |    1     |
row3 |    1     |    1     |    1     |    1     |    1     |    1     |
.... |    1     |    0     |    1     |    1     |    1     |    1     |
.... |    1     |    1     |    0     |    1     |    1     |    1     |
rowN |    1     |    1     |    1     |    1     |    1     |    1     |

如上图所示Matrix用一个HashMap存储一个N行M列的0/1矩阵，其中每行为一个RoaringBitmap结构；
RoaringBitmap参见：
https://github.com/RoaringBitmap/RoaringBitmap
 */

//1、我们可以把Matrix看作广告在某个定向域的倒排索引（N行/M列），其中
//行：每行代表该广告定向域的一个值，假设是定向域"性别"的倒排索引，
//   那么性别可取的值有（100：全部、101：男、102：女、103：未知）4个，
//   那么该倒排索引就有4行，每行代表一个性别的域值
//列：每列代表一个计划id，可表示的计划id范围为（0～2,147,483,647），
//   那么矩阵的每个值(0/1)，表示纵列的计划id是否可以投放在横列的定向值域上

//2、我们把广告平台的所有定向域的倒排索引联合起来，就组成了整体的广告倒排索引
//   HashMap<String,Matrix>, 目前我们支持的定向域包括：
//   性别：gender、年龄：age、兴趣：interest、人群包：package、排除人群包：excludePackage

public class Matrix {
    private final ConcurrentHashMap<Integer,RoaringBitmapEx> matrix;
    public Matrix() {
        this.matrix = new ConcurrentHashMap<>();
    }
    public void clear() {
        for (RoaringBitmap rowValue: matrix.values()){
            rowValue.clear();
        }
        matrix.clear();
    }
    public String toString() {
        return matrix.toString();
    }
    public String toString(int columnIndex) {
        StringBuilder answer = new StringBuilder();
        answer.append("{");
        Iterator<HashMap.Entry<Integer,RoaringBitmapEx>> iter = matrix.entrySet().iterator();
        boolean firstRowIndex = true;
        while (iter.hasNext()){
            HashMap.Entry<Integer,RoaringBitmapEx> entry = iter.next();
            RoaringBitmap rb = entry.getValue();
            if(rb == null) {
                continue;
            }
            if (rb.contains(columnIndex)) {
                if (firstRowIndex) {
                    answer.append(entry.getKey());
                    firstRowIndex = false;
                }else{
                    answer.append(",");
                    answer.append(entry.getKey());
                }
            }
        }
        answer.append("}");
        return answer.toString();
    }
    public void add(int rowIndex,int columnIndex) {
        RoaringBitmapEx rb = matrix.get(rowIndex);
        if (rb == null) {
            rb = new RoaringBitmapEx();
            matrix.put(rowIndex,rb);
        }
        rb.add(columnIndex);
    }
    private void add(int columnIndex){
        for (RoaringBitmap rowValue: matrix.values()){
            rowValue.add(columnIndex);
        }
    }
    private void remove(int rowIndex,int columnIndex) {
        RoaringBitmap rb = matrix.get(rowIndex);
        if (rb != null) {
            rb.remove(columnIndex);
        }
    }
    public void remove(int columnIndex){
        for (RoaringBitmap rowValue: matrix.values()){
            rowValue.remove(columnIndex);
        }
    }
    public void updateColumn(int columnIndex, HashSet<Integer> rowArray) {
        remove(columnIndex);
        for (int rowIndex: rowArray){
            RoaringBitmapEx rb = matrix.get(rowIndex);
            if (rb == null) {
                rb = new RoaringBitmapEx();
                matrix.put(rowIndex,rb);
            }
            rb.add(columnIndex);
        }
    }
    public String getColumnString(int columnIndex){
        StringBuilder answer = new StringBuilder();
        HashSet<Integer> dims = getColumn(columnIndex);
        if (dims != null){
            answer.append(dims);
        }
        return answer.toString();
    }
    public HashSet<Integer> getColumn(int columnIndex) {
        if (matrix.size()==0){
            return null;
        }
        HashSet<Integer> retSet = new HashSet<>();
        for (Integer rowIndex: matrix.keySet()){
            RoaringBitmap rb = matrix.get(rowIndex);
            if (rb != null){
                if (rb.contains(columnIndex)) {
                    retSet.add(rowIndex);
                }
            }
        }
        return retSet;
    }
    public RoaringBitmap getRow(int rowIndex) {
        RoaringBitmap retRb = new RoaringBitmap();
        RoaringBitmapEx rb = matrix.get(rowIndex);
        if (rb != null) {
            rb.threadSafeOr(retRb);
        }
        return retRb;
    }

    public RoaringBitmap unionRows(ArrayList<Integer> rowArray) {
        RoaringBitmap retRb = new RoaringBitmap();
        for (int j : rowArray) {
            RoaringBitmapEx rb = matrix.get(j);
            if (rb != null) {
                rb.threadSafeOr(retRb);
            }
        }
        return retRb;
    }
}
