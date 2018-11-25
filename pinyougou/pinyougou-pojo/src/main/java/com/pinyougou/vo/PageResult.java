package com.pinyougou.vo;

import java.io.Serializable;
import java.util.List;

//序列化：将一个对象（属性、值）转换为一个有格式的字符串
//反序列化：将一个有格式的字符串按照格式进行切分然后转换为一个java类实例
public class PageResult implements Serializable {
    //记录列表
    private List<?> rows;
    //总记录数
    private long total;

    public PageResult(long total, List<?> rows) {
        this.rows = rows;
        this.total = total;
    }

    public PageResult() {
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
