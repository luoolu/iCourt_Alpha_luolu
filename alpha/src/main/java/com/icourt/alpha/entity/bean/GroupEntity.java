package com.icourt.alpha.entity.bean;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.icourt.alpha.view.recyclerviewDivider.ISuspensionAction;
import com.icourt.alpha.view.recyclerviewDivider.ISuspensionInterface;

/**
 * Description
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/4/22
 * version 1.0.0
 */
public class GroupEntity implements ISuspensionInterface, ISuspensionAction {
    public String suspensionTag;
    public String id;
    public String name;

    @Override
    public boolean isShowSuspension() {
        return true;
    }

    @NonNull
    @Override
    public String getSuspensionTag() {
        return TextUtils.isEmpty(suspensionTag) ? "#" : suspensionTag;
    }

    @Nullable
    @Override
    public String getTargetField() {
        return name;
    }

    @Override
    public void setSuspensionTag(@NonNull String suspensionTag) {
        this.suspensionTag = suspensionTag;
    }
}
