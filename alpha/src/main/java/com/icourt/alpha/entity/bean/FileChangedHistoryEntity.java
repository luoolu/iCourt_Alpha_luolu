package com.icourt.alpha.entity.bean;

import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.icourt.alpha.widget.comparators.ILongFieldEntity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description  文件修改历史
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/8/17
 * version 2.1.0
 */
public class FileChangedHistoryEntity implements ILongFieldEntity {
    public static final String OP_TYPE_CREATE = "create";
    public static final String OP_TYPE_MOVE = "move";
    public static final String OP_TYPE_DELETE = "delete";
    public static final String OP_TYPE_RECOVER = "recover";
    public static final String OP_TYPE_RENAME = "rename";
    public static final String OP_TYPE_EDIT = "edit";

    @StringDef({
            OP_TYPE_CREATE,
            OP_TYPE_MOVE,
            OP_TYPE_DELETE,
            OP_TYPE_RECOVER,
            OP_TYPE_RENAME,
            OP_TYPE_EDIT,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OP_TYPE {

    }


    /**
     * {
     * "id": 2628144,
     * "obj_type": "file",
     * "op_type": "edit",
     * "operator_id": "9E4BCEF1492E11E7843370106FAECE2E",
     * "operator_name": "王小英",
     * "date": 1502962905000,
     * "repo_id": "9fba65a7-0e05-405b-8300-b73edd3dc1aa",
     * "repo_name": "测试团队：创新工场项目",
     * "path": "/AlphaTeam测试项目/客户端-计时测试用例-王小英.xlsx",
     * "size": 87934,
     * "commit_id": "6fe581bd2df10d042aa53662d21d064cf008c40d",
     * "pre_commit_id": "3415cdb1a16281049152b916b26d8b77c8072f06",
     * "new_path": null,
     * "pic": "https://wx.qlogo.cn/mmopen/SHnMujzj2v8VXia4lzCEWl9eOl5Uj4EbibbBl8mf6DXP8E6bQ7V2eeu1gKBMP8NvZRKlPDcibadEibm3ibxujuYziapFiaxvicicRpEPx/64",
     * "file_name": "客户端-计时测试用例-王小英.xlsx"
     * }
     */
    public long id;
    public String obj_type;
    @OP_TYPE
    public String op_type;
    public String operator_id;
    public String operator_name;
    public long date;
    public String repo_id;
    public String repo_name;
    public String path;//原来的路径
    public long size;
    public String commit_id;
    public String pre_commit_id;
    public String new_path;
    public String pic;
    public String file_name;

    public boolean isDir() {
        return TextUtils.equals(obj_type, "dir");
    }

    @Override
    public Long getCompareLongField() {
        return date;
    }
}
