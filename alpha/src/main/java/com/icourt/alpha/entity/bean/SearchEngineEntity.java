package com.icourt.alpha.entity.bean;

import com.icourt.alpha.db.convertor.IConvertModel;
import com.icourt.alpha.db.dbmodel.SearchEngineModel;

import java.io.Serializable;

/**
 * Description 搜索引擎tag
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/4/20
 * version 1.0.0
 */
public class SearchEngineEntity implements IConvertModel<SearchEngineModel>, Serializable {
    public int id;
    public String name;
    public String site;

    public SearchEngineEntity(int id, String name, String site) {
        this.id = id;
        this.name = name;
        this.site = site;
    }

    @Override
    public SearchEngineModel convert2Model() {
        return new SearchEngineModel(id, name, site);
    }
}
