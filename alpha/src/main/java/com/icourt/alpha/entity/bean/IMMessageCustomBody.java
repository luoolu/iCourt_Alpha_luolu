package com.icourt.alpha.entity.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.alpha.widget.comparators.ILongFieldEntity;
import com.icourt.alpha.widget.json.URLDecoderTypeAdapter;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static com.icourt.alpha.constants.Const.CHAT_TYPE_P2P;
import static com.icourt.alpha.constants.Const.CHAT_TYPE_TEAM;

/**
 * Description 接口文档 https://www.showdoc.cc/1620156?page_id=14893614
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/4/28
 * version 1.0.0
 */
public class IMMessageCustomBody implements ILongFieldEntity, Serializable {
    public static final String PLATFORM_ANDROID = "ANDROID";

    @Expose(serialize = false, deserialize = false)
    public IMMessage imMessage;

    @Const.MSG_STATU
    public int msg_statu;//消息状态 本地用

    public long id;//消息id
    public String name;// 发送人名字,
    public String from;//发送人id,
    public String to;//接收人id，这个需要根据ope判断

    @Const.CHAT_TYPE
    public int ope;            //0点对点，1群聊

    @Const.MSG_TYPE
    public int show_type;
    public long send_time;
    public String magic_id;
    public String platform;
    public IMMessageExtBody ext;
    @JsonAdapter(URLDecoderTypeAdapter.class)
    public String content;

    public IMMessageCustomBody() {
    }

    private IMMessageCustomBody(
            @Const.CHAT_TYPE int chatType,
            @Const.MSG_TYPE int msgType,
            String name,
            String from,
            String to,
            String content,
            String magic_id,
            String platform) {
        this.ope = chatType;
        this.show_type = msgType;
        this.name = name;
        this.from = from;
        this.to = to;
        this.content = content;
        this.magic_id = magic_id;
        this.platform = platform;
    }


    /**
     * 构建文本消息体
     *
     * @param name    发送方的名字
     * @param to      接收人id，这个需要根据ope判断
     * @param content 文本内容
     * @return
     */
    public static IMMessageCustomBody createTextMsg(@Const.CHAT_TYPE int chatType,
                                                    String name,
                                                    String from,
                                                    String to,
                                                    String content) {
        IMMessageCustomBody imMessageCustomBody = new IMMessageCustomBody(chatType,
                Const.MSG_TYPE_TXT,
                name,
                from,
                to,
                content,
                UUID.randomUUID().toString(),
                PLATFORM_ANDROID);
        imMessageCustomBody.msg_statu = Const.MSG_STATU_SENDING;
        imMessageCustomBody.send_time = System.currentTimeMillis();
        switch (chatType) {
            case CHAT_TYPE_P2P:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.P2P, "");
                break;
            case CHAT_TYPE_TEAM:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.Team, "");
                break;
        }
        return imMessageCustomBody;
    }

    /**
     * 构建文件消息体
     *
     * @param name     发送方的名字
     * @param to       接收人id，这个需要根据ope判断
     * @param filePath 文件本地路径
     * @return
     */
    public static IMMessageCustomBody createFileMsg(@Const.CHAT_TYPE int chatType,
                                                    String name,
                                                    String from,
                                                    String to,
                                                    String filePath) {
        IMMessageCustomBody imMessageCustomBody = new IMMessageCustomBody(chatType,
                Const.MSG_TYPE_IMAGE,
                name,
                from,
                to,
                null,
                UUID.randomUUID().toString(),
                PLATFORM_ANDROID);
        imMessageCustomBody.send_time = System.currentTimeMillis();
        imMessageCustomBody.msg_statu = Const.MSG_STATU_SENDING;
        imMessageCustomBody.ext = IMMessageExtBody.createPicExtBody(filePath);
        switch (chatType) {
            case CHAT_TYPE_P2P:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.P2P, "");
                break;
            case CHAT_TYPE_TEAM:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.Team, "");
                break;
        }
        return imMessageCustomBody;
    }


    /**
     * 构建文件消息体
     *
     * @param name 发送方的名字
     * @param to   接收人id，这个需要根据ope判断
     * @param url  链接
     * @return
     */
    public static IMMessageCustomBody createLinkMsg(@Const.CHAT_TYPE int chatType,
                                                    String name,
                                                    String from,
                                                    String to,
                                                    String url,
                                                    String htmlTitle,
                                                    String htmlDescription,
                                                    String htmlImage) {
        IMMessageCustomBody imMessageCustomBody = new IMMessageCustomBody(chatType,
                Const.MSG_TYPE_LINK,
                name,
                from,
                to,
                null,
                UUID.randomUUID().toString(),
                PLATFORM_ANDROID);
        imMessageCustomBody.send_time = System.currentTimeMillis();
        imMessageCustomBody.msg_statu = Const.MSG_STATU_SENDING;
        imMessageCustomBody.ext = IMMessageExtBody.createLinkExtBody(htmlTitle, htmlImage, htmlDescription, url);
        switch (chatType) {
            case CHAT_TYPE_P2P:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.P2P, "");
                break;
            case CHAT_TYPE_TEAM:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.Team, "");
                break;
        }
        return imMessageCustomBody;
    }

    /**
     * 构建AT消息
     *
     * @param chatType
     * @param name
     * @param to
     * @param content
     * @param isAtall
     * @param usersAccids
     * @return
     */
    public static IMMessageCustomBody createAtMsg(@Const.CHAT_TYPE int chatType,
                                                  String name,
                                                  String from,
                                                  String to,
                                                  String content,
                                                  boolean isAtall,
                                                  List<String> usersAccids) {
        IMMessageCustomBody imMessageCustomBody = new IMMessageCustomBody(chatType,
                Const.MSG_TYPE_AT,
                name,
                from,
                to,
                content,
                UUID.randomUUID().toString(),
                PLATFORM_ANDROID);
        imMessageCustomBody.send_time = System.currentTimeMillis();
        imMessageCustomBody.msg_statu = Const.MSG_STATU_SENDING;
        imMessageCustomBody.ext = IMMessageExtBody.createAtExtBody(usersAccids, isAtall);
        switch (chatType) {
            case CHAT_TYPE_P2P:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.P2P, "");
                break;
            case CHAT_TYPE_TEAM:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.Team, "");
                break;
        }
        return imMessageCustomBody;
    }

    /**
     * 构建 钉的消息
     *
     * @param chatType
     * @param name
     * @param to
     * @param isDing
     * @param dingMsgId
     * @return
     */
    public static IMMessageCustomBody createDingMsg(@Const.CHAT_TYPE int chatType,
                                                    String name,
                                                    String from,
                                                    String to,
                                                    boolean isDing,
                                                    long dingMsgId) {
        IMMessageCustomBody imMessageCustomBody = new IMMessageCustomBody(chatType,
                Const.MSG_TYPE_DING,
                name,
                from,
                to,
                null,
                UUID.randomUUID().toString(),
                PLATFORM_ANDROID);
        imMessageCustomBody.send_time = System.currentTimeMillis();
        imMessageCustomBody.msg_statu = Const.MSG_STATU_SENDING;
        imMessageCustomBody.ext = IMMessageExtBody.createDingExtBody(isDing, dingMsgId);
        switch (chatType) {
            case CHAT_TYPE_P2P:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.P2P, "");
                break;
            case CHAT_TYPE_TEAM:
                imMessageCustomBody.imMessage = MessageBuilder.createTextMessage(to, SessionTypeEnum.Team, "");
                break;
        }
        return imMessageCustomBody;
    }

    @Override
    public Long getCompareLongField() {
        return send_time;
    }

    @Override
    public String toString() {
        return "IMMessageCustomBody{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", ope=" + ope +
                ", show_type=" + show_type +
                ", send_time=" + send_time +
                ", magic_id='" + magic_id + '\'' +
                ", platform='" + platform + '\'' +
                ", ext=" + ext +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null) return false;
        if (getClass() != o.getClass())
            return false;
        final IMMessageCustomBody other = (IMMessageCustomBody) o;
        return other.id == this.id || StringUtils.equalsIgnoreCase(other.magic_id, this.magic_id, false);
    }
}
