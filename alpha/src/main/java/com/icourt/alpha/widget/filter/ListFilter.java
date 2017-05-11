package com.icourt.alpha.widget.filter;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Description
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/5/11
 * version 1.0.0
 */
public class ListFilter<T extends IFilterEntity> implements BaseFilter<T> {


    @Nullable
    @Override
    public List<T> filter(@Nullable List<T> data, int type) {
        if (data != null) {
            //过滤机器人
            for (int i = data.size() - 1; i >= 0; i--) {
                T t = data.get(i);
                if (t != null && t.isFilter(type)) {
                    data.remove(i);
                }
            }
        }
        return data;
    }

    @Nullable
    @Override
    public boolean filter(@Nullable List<T> data, T t) {
        if (data != null) {
            return data.remove(t);
        }
        return false;
    }

    @Nullable
    @Override
    public List<T> filterEmpty(@Nullable List<T> data) {
        if (data != null) {
            data.remove(null);
        }
        return data;
    }
}
