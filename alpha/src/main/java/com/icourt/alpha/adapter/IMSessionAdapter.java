package com.icourt.alpha.adapter;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.asange.recyclerviewadapter.BaseViewHolder;
import com.icourt.alpha.R;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.entity.bean.AlphaUserInfo;
import com.icourt.alpha.entity.bean.GroupContactBean;
import com.icourt.alpha.entity.bean.IMMessageCustomBody;
import com.icourt.alpha.entity.bean.IMSessionEntity;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.GlideUtils;
import com.icourt.alpha.utils.IMUtils;
import com.icourt.alpha.utils.LoginInfoUtils;
import com.icourt.alpha.utils.SpannableUtils;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.alpha.view.bgabadgeview.BGABadgeImageView;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.List;

import static com.icourt.alpha.constants.Const.CHAT_TYPE_P2P;
import static com.icourt.alpha.constants.Const.CHAT_TYPE_TEAM;

/**
 * Description 消息通知回话列表
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/4/10
 * version 1.0.0
 */
public class IMSessionAdapter extends ContactBaseAdapter<IMSessionEntity> {

    AlphaUserInfo alphaUserInfo;
    int foregroundColor = 0xFFed6c00;

    private String getLoginUserId() {
        if (alphaUserInfo != null) {
            return alphaUserInfo.getUserId();
        }
        return null;
    }

    private List<String> localSetTops;
    private List<String> localNoDisturbs;


    public IMSessionAdapter(@NonNull List<String> localSetTops, @NonNull List<String> localNoDisturbs) {
        this.localNoDisturbs = localNoDisturbs;
        this.localSetTops = localSetTops;
        alphaUserInfo = LoginInfoUtils.getLoginUserInfo();
    }

    @Nullable
    @CheckResult
    public GroupContactBean getContact(int index) {
        IMSessionEntity item = getItem(index);
        if (item != null && item.recentContact != null) {
            return getContactByAccid(item.recentContact.getContactId());
        }
        return null;
    }

    /**
     * 刷新team
     */
    public void refreshTeams() {
        initTeams();
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.adapter_item_im_session;
    }

    @Override
    public void onBindHolder(BaseViewHolder holder, @Nullable IMSessionEntity imSessionEntity, int i) {
        if (imSessionEntity == null) {
            return;
        }
        ImageView ivSessionIcon = holder.obtainView(R.id.iv_session_icon);
        TextView tvSessionTime = holder.obtainView(R.id.tv_session_time);
        TextView tvSessionTitle = holder.obtainView(R.id.tv_session_title);
        TextView tvSessionContent = holder.obtainView(R.id.tv_session_content);
        ImageView ivSessionNotDisturb = holder.obtainView(R.id.iv_session_not_disturb);
        tvSessionContent.setText(imSessionEntity.recentContact.getContent());
        if (imSessionEntity.recentContact != null) {
            //1.设置提示数量
            setUnreadCount(holder, imSessionEntity, i);

            //3.设置消息展示的时间
            setTimeView(tvSessionTime, imSessionEntity.recentContact.getTime(), i);


            //4.设置消息体展示
            setSessionTitle(imSessionEntity, tvSessionTitle);
            setSessionIcon(imSessionEntity, ivSessionIcon);
            setItemData(imSessionEntity,
                    tvSessionContent);


            //5.设置消息免打扰
            setItemDontDisturbs(imSessionEntity, ivSessionNotDisturb);

            //6设置置顶状态
            setItemSetTop(holder, imSessionEntity, i);
        }
    }


    private void setItemSetTop(BaseViewHolder holder, IMSessionEntity imSessionEntity, int position) {
        if (holder == null) {
            return;
        }
        if (imSessionEntity == null) {
            return;
        }
        if (imSessionEntity.recentContact == null) {
            return;
        }
        boolean isSetToped = localSetTops.contains(imSessionEntity.recentContact.getContactId());
        holder.itemView.setBackgroundResource(isSetToped ? R.drawable.list_view_item_other_touch_bg : R.drawable.list_view_item_touch_bg);
    }


    /**
     * 设置消息提示红点
     *
     * @param holder
     * @param imSessionEntity
     * @param position
     */
    private void setUnreadCount(BaseViewHolder holder, IMSessionEntity imSessionEntity, int position) {
        if (imSessionEntity == null) {
            return;
        }
        if (imSessionEntity.recentContact == null) {
            return;
        }
        BGABadgeImageView iv_session_icon = holder.obtainView(R.id.iv_session_icon);
        //草稿
        if (imSessionEntity.recentContact.getMsgStatus() == MsgStatusEnum.draft) {
            if (iv_session_icon.isShowBadge()) {
                iv_session_icon.hiddenBadge();
            }
            return;
        }
        int unreadCount = imSessionEntity.recentContact.getUnreadCount();
        //消息免打扰为小红点
        if (localNoDisturbs.contains(imSessionEntity.recentContact.getContactId()) && unreadCount > 0) {
            iv_session_icon.showCirclePointBadge();
        } else {
            if (unreadCount > 0 && unreadCount <= 99) {
                //直接显示
                iv_session_icon.showTextBadge(String.valueOf(unreadCount));
            } else if (unreadCount > 99) {
                // 显示99+
                iv_session_icon.showTextBadge("99+");
            } else if (unreadCount <= 0) {
                //隐藏
                iv_session_icon.hiddenBadge();
            }
        }

    }

    /**
     * 展示群消息免打扰的icon
     *
     * @param imSessionEntity
     * @param ivSessionNotDisturb
     */
    private void setItemDontDisturbs(IMSessionEntity imSessionEntity, ImageView ivSessionNotDisturb) {
        if (imSessionEntity == null) {
            return;
        }
        if (ivSessionNotDisturb == null) {
            return;
        }
        if (imSessionEntity.customIMBody == null) {
            return;
        }
        ivSessionNotDisturb.setVisibility(localNoDisturbs.contains(imSessionEntity.customIMBody.to)
                ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置session 头像
     *
     * @param imSessionEntity
     * @param ivSessionIcon
     */
    public void setSessionIcon(IMSessionEntity imSessionEntity, ImageView ivSessionIcon) {
        if (ivSessionIcon == null) {
            return;
        }
        if (imSessionEntity == null) {
            return;
        }
        if (imSessionEntity.customIMBody == null) {
            return;
        }
        if (imSessionEntity.recentContact == null) {
            return;
        }
        switch (imSessionEntity.customIMBody.ope) {
            case CHAT_TYPE_P2P:
                GroupContactBean user = getContactByAccid(imSessionEntity.recentContact.getContactId());
                if (user != null) {
                    GlideUtils.loadUser(ivSessionIcon.getContext(),
                            user.pic,
                            ivSessionIcon);
                }
                break;
            case CHAT_TYPE_TEAM:
                Team team = getTeamById(imSessionEntity.recentContact.getContactId());
                if (team != null) {
                    if (!TextUtils.isEmpty(team.getIcon())) {
                        GlideUtils.loadGroup(ivSessionIcon.getContext(),
                                team.getIcon(),
                                ivSessionIcon);
                    } else {
                        setTeamIcon(team.getName(), ivSessionIcon);
                    }
                }
                break;
        }
    }

    /**
     * 设置team头像
     *
     * @param teamName
     * @param ivSessionIcon
     */
    private void setTeamIcon(String teamName, ImageView ivSessionIcon) {
        if (TextUtils.isEmpty(teamName)) {
            return;
        }
        if (ivSessionIcon == null) {
            return;
        }
        IMUtils.setTeamIcon(teamName, ivSessionIcon);
    }

    /**
     * 设置标题
     *
     * @param imSessionEntity
     * @param tvSessionTitle
     */
    public void setSessionTitle(IMSessionEntity imSessionEntity, TextView tvSessionTitle) {
        if (tvSessionTitle == null) {
            return;
        }
        if (imSessionEntity == null) {
            return;
        }
        if (imSessionEntity.customIMBody == null) {
            return;
        }
        if (imSessionEntity.recentContact == null) {
            return;
        }
        switch (imSessionEntity.customIMBody.ope) {
            case CHAT_TYPE_P2P:
                GroupContactBean user = getContactByAccid(imSessionEntity.recentContact.getContactId());
                if (user != null) {
                    tvSessionTitle.setText(user.name);
                } else {
                    if (imSessionEntity.customIMBody.show_type == Const.MSG_TYPE_ALPHA_HELPER) {
                        tvSessionTitle.setText("Alpha小助手");
                    } else {
                        tvSessionTitle.setText("name field null");
                    }
                }
                break;
            case CHAT_TYPE_TEAM:
                Team team = getTeamById(imSessionEntity.customIMBody.to);
                if (team != null) {
                    tvSessionTitle.setText(team.getName());
                }
                break;
        }
    }


    /**
     * 展示自定义消息
     *
     * @param imSessionEntity
     * @param tvSessionContent
     */
    private void setItemData(IMSessionEntity imSessionEntity, TextView tvSessionContent) {
        if (imSessionEntity == null) {
            return;
        }
        if (imSessionEntity.recentContact == null) {
            return;
        }
        if (imSessionEntity.customIMBody == null) {
            return;
        }
        if (tvSessionContent == null) {
            return;
        }
        IMMessageCustomBody customIMBody = imSessionEntity.customIMBody;
        //草稿
        if (customIMBody.msg_statu == Const.MSG_STATU_DRAFT) {
            String targetText = "[草稿]";
            SpannableStringBuilder ssb = new SpannableStringBuilder(String.format("%s %s", targetText, customIMBody.content));
            ssb.setSpan(new ForegroundColorSpan(foregroundColor), 0, targetText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tvSessionContent.setText(ssb);
            return;
        }


        StringBuilder stringBuilder = new StringBuilder();
        if (localNoDisturbs.contains(imSessionEntity.recentContact.getContactId())
                && imSessionEntity.recentContact.getUnreadCount() > 0) {
            stringBuilder.append(String.format("[%s条] ", imSessionEntity.recentContact.getUnreadCount()));
        }
        //内容
        switch (customIMBody.show_type) {
            case Const.MSG_TYPE_ALPHA_HELPER:
                stringBuilder.append(customIMBody.content);
                tvSessionContent.setText(stringBuilder.toString());
                break;
            case Const.MSG_TYPE_TXT:    //文本消息
                stringBuilder.append(getSessionContent(customIMBody));
                stringBuilder.append(customIMBody.content);
                tvSessionContent.setText(stringBuilder.toString());
                break;
            case Const.MSG_TYPE_IMAGE:
                stringBuilder.append(getSessionContent(customIMBody));
                stringBuilder.append("[ 图片 ]");
                tvSessionContent.setText(stringBuilder.toString());
                break;
            case Const.MSG_TYPE_FILE:     //文件消息
                stringBuilder.append(getSessionContent(customIMBody));
                stringBuilder.append("[ 文件 ]");
                tvSessionContent.setText(stringBuilder.toString());
                break;
            case Const.MSG_TYPE_DING:   //钉消息
                if (customIMBody.ext != null) {
                    StringBuilder dingStringBuilder = new StringBuilder(stringBuilder.toString());
                    switch (customIMBody.ope) {
                        case CHAT_TYPE_P2P:
                            break;
                        case CHAT_TYPE_TEAM:
                            dingStringBuilder.append(customIMBody.name + " : ");
                            break;
                    }
                    dingStringBuilder.append(customIMBody.content);
                    tvSessionContent.setText(dingStringBuilder.toString());
                } else {
                    tvSessionContent.setText("钉消息ext null");
                }
                break;
            case Const.MSG_TYPE_AT://@消息
                if (customIMBody.ext != null) {
                    //已经阅读了 就不标红
                    if (imSessionEntity.recentContact.getUnreadCount() <= 0) {
                        tvSessionContent.setText(String.format("%s : %s", customIMBody.name, customIMBody.content));
                    } else {
                        if (customIMBody.ext.is_all) {
                            stringBuilder.append("有人@了你");
                            String originalText = stringBuilder.toString();
                            tvSessionContent.setText(originalText);
                            SpannableUtils.setTextForegroundColorSpan(tvSessionContent, originalText, "有人@了你", foregroundColor);
                        } else if (customIMBody.ext.users != null
                                && StringUtils.containsIgnoreCase(customIMBody.ext.users, getLoginUserId())) {
                            stringBuilder.append("有人@了你");
                            String originalText = stringBuilder.toString();
                            tvSessionContent.setText(originalText);
                            SpannableUtils.setTextForegroundColorSpan(tvSessionContent, originalText, "有人@了你", foregroundColor);
                        } else {
                            stringBuilder.append(String.format("%s : %s", customIMBody.name, customIMBody.content));
                            tvSessionContent.setText(stringBuilder.toString());
                        }
                    }
                } else {
                    tvSessionContent.setText("@消息ext null");
                }
                break;
            case Const.MSG_TYPE_SYS:     //系统辅助消息
                if (customIMBody.ext != null) {
                    stringBuilder.append(customIMBody.ext.content);
                    tvSessionContent.setText(stringBuilder.toString());
                } else {
                    tvSessionContent.setText("sys消息ext null");
                }
                break;
            case Const.MSG_TYPE_LINK://链接消息
                if (customIMBody.ext != null) {
                    stringBuilder.append(getSessionContent(customIMBody));
                    stringBuilder.append(customIMBody.ext.url);
                    tvSessionContent.setText(stringBuilder.toString());
                } else {
                    tvSessionContent.setText("link消息ext null");
                }
                break;
            case Const.MSG_TYPE_ALPHA:   //alpha系统内业务消息 2.0.0暂时不处理
                tvSessionContent.setText("alpha系统内业务消息 2.0.0不支持");
                break;
            case Const.MSG_TYPE_VOICE:   //alpha语音消息 2.0.0暂时不处理
                tvSessionContent.setText("alpha语音消息 2.0.0不支持");
                break;
        }
    }

    /**
     * ope ＝＝ team时，content显示发送者名称
     * <p>
     * 获取sessioncontent
     *
     * @param customIMBody
     * @return
     */
    private String getSessionContent(IMMessageCustomBody customIMBody) {
        String sessionName = "";
        if (customIMBody == null) {
            return sessionName;
        }
        if (customIMBody.ope == Const.CHAT_TYPE_TEAM) {
            if (!StringUtils.equalsIgnoreCase(customIMBody.from, getLoginUserId(), false)) {
                sessionName = customIMBody.name + " : ";
            }
        }
        return sessionName;
    }

    /**
     * 设置消息展示的时间
     *
     * @param tvSessionTime
     * @param time
     */
    private void setTimeView(TextView tvSessionTime, long time, int pos) {
        if (tvSessionTime == null) {
            return;
        }
        tvSessionTime.setText(DateUtils.getFormatChatTimeSimple(time));
    }


}
