package com.icourt.alpha.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.IMContactAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.DataChangeAdapterObserver;
import com.icourt.alpha.base.BaseActivity;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.db.dbmodel.ContactDbModel;
import com.icourt.alpha.db.dbservice.ContactDbService;
import com.icourt.alpha.entity.bean.GroupContactBean;
import com.icourt.alpha.entity.bean.GroupDetailEntity;
import com.icourt.alpha.entity.bean.GroupEntity;
import com.icourt.alpha.entity.event.GroupActionEvent;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.utils.JsonUtils;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.api.RequestUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

import static com.icourt.alpha.constants.Const.CHAT_TYPE_TEAM;

/**
 * Description  群组详情
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/4/23
 * version 1.0.0
 */
public class GroupDetailActivity extends BaseActivity {
    private static final String KEY_GROUP_ID = "key_group_id";
    private static final String KEY_TID = "key_tid";//云信id

    private static final int REQ_CODE_INVITATION_MEMBER = 1002;

    @BindView(R.id.group_name_tv)
    TextView groupNameTv;
    @BindView(R.id.group_desc_tv)
    TextView groupDescTv;
    @BindView(R.id.group_ding_tv)
    TextView groupDingTv;
    @BindView(R.id.group_file_tv)
    TextView groupFileTv;
    @BindView(R.id.group_member_num_tv)
    TextView groupMemberNumTv;
    @BindView(R.id.group_member_invite_tv)
    TextView groupMemberInviteTv;
    @BindView(R.id.group_title_divider)
    View groupTitleDivider;
    @BindView(R.id.group_member_recyclerView)
    RecyclerView groupMemberRecyclerView;
    @BindView(R.id.group_member_arrow_iv)
    ImageView groupMemberArrowIv;
    @BindView(R.id.group_setTop_switch)
    Switch groupSetTopSwitch;
    @BindView(R.id.group_setTop_ll)
    LinearLayout groupSetTopLl;
    @BindView(R.id.group_not_disturb_switch)
    Switch groupNotDisturbSwitch;
    @BindView(R.id.group_disturb_ll)
    LinearLayout groupDisturbLl;
    @BindView(R.id.titleBack)
    ImageView titleBack;
    @BindView(R.id.titleContent)
    TextView titleContent;
    @BindView(R.id.titleAction)
    ImageView titleAction;
    GroupEntity groupEntity;
    IMContactAdapter contactAdapter;
    @BindView(R.id.group_join_or_quit_btn)
    Button groupJoinOrQuitBtn;
    final ArrayList<GroupContactBean> groupContactBeens = new ArrayList<>();
    ContactDbService contactDbService;
    DataChangeAdapterObserver dataChangeAdapterObserver = new DataChangeAdapterObserver() {
        @Override
        protected void updateUI() {
            groupMemberNumTv.setText(String.format("成员(%s)", contactAdapter.getItemCount()));
        }
    };
    GroupDetailEntity groupDetailEntity;


    public static void launchTEAM(@NonNull Context context, String groupId, String tid) {
        if (context == null) return;
        if (TextUtils.isEmpty(groupId)) return;
        if (TextUtils.isEmpty(tid)) return;
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(KEY_GROUP_ID, groupId);
        intent.putExtra(KEY_TID, tid);
        context.startActivity(intent);
    }

    protected String getIMChatId() {
        return getIntent().getStringExtra(KEY_TID);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        contactDbService = new ContactDbService(getLoginUserId());
        ImageView titleActionImage = getTitleActionImage();
        setViewVisible(titleActionImage, false);
        setViewVisible(groupMemberInviteTv, false);
        setViewVisible(groupJoinOrQuitBtn, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setAutoMeasureEnabled(true);
        groupMemberRecyclerView.setLayoutManager(linearLayoutManager);
        groupMemberRecyclerView.setAdapter(contactAdapter = new IMContactAdapter(Const.VIEW_TYPE_GRID));
        contactAdapter.registerAdapterDataObserver(dataChangeAdapterObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData(true);
    }

    @Override
    protected void getData(boolean isRefresh) {
        super.getData(isRefresh);
        showLoadingDialog(null);
        getApi().groupQueryDetail(getIntent().getStringExtra(KEY_TID))
                .enqueue(new SimpleCallBack<GroupDetailEntity>() {
                    @Override
                    public void onSuccess(Call<ResEntity<GroupDetailEntity>> call, Response<ResEntity<GroupDetailEntity>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null) {
                            groupDetailEntity = response.body().result;

                            groupNameTv.setText(response.body().result.name);
                            groupDescTv.setText(response.body().result.intro);
                            groupJoinOrQuitBtn.setSelected(response.body().result.isJoin == 1);
                            groupJoinOrQuitBtn.setText(groupJoinOrQuitBtn.isSelected() ? "退出讨论组" : "加入讨论组");
                            ImageView titleActionImage = getTitleActionImage();

                            boolean isAdmin = StringUtils.equalsIgnoreCase(getLoginUserId(), response.body().result.admin_id, false);

                            //管理员设置按钮展示
                            setViewVisible(titleActionImage, isAdmin);

                            //邀请按钮展示
                            setViewVisible(groupMemberInviteTv, !response.body().result.is_private);

                            //加入/退出的展示
                            if (isAdmin) {
                                setViewVisible(groupJoinOrQuitBtn, false);
                            } else {
                                setViewVisible(groupJoinOrQuitBtn, true);
                                boolean joined = StringUtils.containsIgnoreCase(response.body().result.members, getLoginUserId());
                                groupJoinOrQuitBtn.setText(joined ? "退出讨论组" : "加入讨论组");
                            }

                            //查询本地uid对应的头像
                            queryMembersByUids(response.body().result.members);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<GroupDetailEntity>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
        getSetTopSessions();
        getIsSetGroupNoDisturbing();
    }


    /**
     * 根据uid 查询本地联系人
     *
     * @param members
     */
    private void queryMembersByUids(List<String> members) {
        if (members != null) {
            groupContactBeens.clear();
            if (contactDbService != null) {
                //最多展示20个
                for (int i = 0; i < Math.min(members.size(), 20); i++) {
                    String uid = members.get(i);
                    if (!TextUtils.isEmpty(uid)) {
                        ContactDbModel contactDbModel = contactDbService.queryFirst("accid", uid);
                        if (contactDbModel != null) {
                            groupContactBeens.add(contactDbModel.convert2Model());
                        }
                    }
                }
            }
            contactAdapter.bindData(true, groupContactBeens);
        }
    }


    @OnClick({R.id.group_ding_tv,
            R.id.group_file_tv,
            R.id.group_member_invite_tv,
            R.id.group_member_arrow_iv,
            R.id.group_setTop_switch,
            R.id.group_not_disturb_switch,
            R.id.group_join_or_quit_btn})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_ding_tv:
                ChatMsgClassfyActivity.launchDing(getContext(),
                        CHAT_TYPE_TEAM,
                        getIntent().getStringExtra(KEY_TID));
                break;
            case R.id.group_file_tv:
                //TODO  文件列表
                showTopSnackBar("未完成");
                break;
            case R.id.group_member_invite_tv:
                ContactListActivity.launchSelect(getActivity(),
                        Const.CHOICE_TYPE_MULTIPLE,
                        REQ_CODE_INVITATION_MEMBER);
                break;
            case R.id.group_member_arrow_iv:
                GroupMemberDelActivity.launchForResult(getActivity(),
                        getIntent().getStringExtra(KEY_TID),
                        (ArrayList<GroupContactBean>) contactAdapter.getData(),
                        true, 2001);
                break;
            case R.id.group_setTop_switch:
                if (!groupSetTopSwitch.isChecked()) {
                    setGroupTopCancel();
                } else {
                    setGroupTop();
                }
                break;
            case R.id.group_not_disturb_switch:
                if (!groupNotDisturbSwitch.isChecked()) {
                    setGroupNoDisturbingCancel();
                } else {
                    setGroupNoDisturbing();
                }
                break;
            case R.id.group_join_or_quit_btn:
                if (v.isSelected()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("提示")
                            .setMessage("是否离开讨论组?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    quitGroup();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    joinGroup();
                }
                break;
            case R.id.titleAction:
                GroupSettingActivity.launch(getContext(), groupDetailEntity);
                break;
            default:
                super.onClick(v);
                break;
        }
    }


    /**
     * 获取所有置顶的会话ids
     */
    private void getSetTopSessions() {
        getApi().setTopQueryAllIds()
                .enqueue(new SimpleCallBack<List<String>>() {
                    @Override
                    public void onSuccess(Call<ResEntity<List<String>>> call, Response<ResEntity<List<String>>> response) {
                        if (response.body().result != null) {
                            groupSetTopSwitch.setChecked(response.body()
                                    .result.contains(getIMChatId()));
                        }
                    }
                });
    }


    /**
     * 云信状态码  http://dev.netease.im/docs?doc=nim_status_code
     * 获取讨论组 是否免打扰
     */
    private void getIsSetGroupNoDisturbing() {
        NIMClient.getService(TeamService.class)
                .queryTeam(getIntent().getStringExtra(KEY_TID))
                .setCallback(new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(Team param) {
                        groupNotDisturbSwitch.setChecked(param != null && param.mute());
                    }

                    @Override
                    public void onFailed(int code) {
                        log("-------->onFailed:" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        log("-------->onException:" + exception);
                    }
                });
    }

    /**
     * 讨论组聊天置顶
     */
    private void setGroupTop() {
        showLoadingDialog(null);
        getApi().sessionSetTop(CHAT_TYPE_TEAM, getIMChatId())
                .enqueue(new SimpleCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Call<ResEntity<Boolean>> call, Response<ResEntity<Boolean>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null && response.body().result.booleanValue()) {
                            groupSetTopSwitch.setChecked(true);
                        } else {
                            groupSetTopSwitch.setChecked(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<Boolean>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     *
     */
    private void setGroupTopCancel() {
        showLoadingDialog(null);
        getApi().sessionSetTopCancel(CHAT_TYPE_TEAM, getIMChatId())
                .enqueue(new SimpleCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Call<ResEntity<Boolean>> call, Response<ResEntity<Boolean>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null && response.body().result.booleanValue()) {
                            groupSetTopSwitch.setChecked(false);
                        } else {
                            groupSetTopSwitch.setChecked(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<Boolean>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 讨论组聊天免打扰
     */
    private void setGroupNoDisturbing() {
        showLoadingDialog(null);
        getApi().sessionNoDisturbing(CHAT_TYPE_TEAM, getIMChatId())
                .enqueue(new SimpleCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Call<ResEntity<Boolean>> call, Response<ResEntity<Boolean>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null && response.body().result) {
                            groupNotDisturbSwitch.setChecked(true);
                            NIMClient.getService(TeamService.class).muteTeam(getIntent().getStringExtra(KEY_TID), true);
                        } else {
                            groupNotDisturbSwitch.setChecked(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<Boolean>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 讨论组聊天取消免打扰
     */
    private void setGroupNoDisturbingCancel() {
        showLoadingDialog(null);
        getApi().sessionNoDisturbingCancel(CHAT_TYPE_TEAM, getIMChatId())
                .enqueue(new SimpleCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Call<ResEntity<Boolean>> call, Response<ResEntity<Boolean>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null && response.body().result) {
                            groupNotDisturbSwitch.setChecked(false);
                            NIMClient.getService(TeamService.class).muteTeam(getIntent().getStringExtra(KEY_TID), false);
                        } else {
                            groupNotDisturbSwitch.setChecked(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<Boolean>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 加入该讨论组
     */
    private void joinGroup() {
        showLoadingDialog(null);
        getApi().joinGroup(getIntent().getStringExtra(KEY_GROUP_ID))
                .enqueue(new SimpleCallBack<JsonElement>() {
                    @Override
                    public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                        dismissLoadingDialog();
                        getData(true);
                        EventBus.getDefault().post(
                                new GroupActionEvent(GroupActionEvent.GROUP_ACTION_JOIN, getIntent().getStringExtra(KEY_GROUP_ID)));
                    }

                    @Override
                    public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 退出讨论组
     */
    private void quitGroup() {
        showLoadingDialog(null);
        getApi().quitGroup(getIntent().getStringExtra(KEY_GROUP_ID))
                .enqueue(new SimpleCallBack<Integer>() {
                    @Override
                    public void onSuccess(Call<ResEntity<Integer>> call, Response<ResEntity<Integer>> response) {
                        dismissLoadingDialog();
                        if (response.body().result != null
                                && response.body().result == 1) {
                            EventBus.getDefault().post(
                                    new GroupActionEvent(GroupActionEvent.GROUP_ACTION_QUIT, getIntent().getStringExtra(KEY_GROUP_ID)));
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResEntity<Integer>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_INVITATION_MEMBER:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    List<GroupContactBean> result = (List<GroupContactBean>) data.getSerializableExtra(KEY_ACTIVITY_RESULT);
                    if (result != null) {
                        invitationMembers(result);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * 邀请成员
     *
     * @param contactBeanArrayList
     */
    private void invitationMembers(List<GroupContactBean> contactBeanArrayList) {
        if (contactBeanArrayList == null) return;
        JsonArray userIdArray = new JsonArray();//使用accid
        for (GroupContactBean groupContactBean : contactBeanArrayList) {
            if (groupContactBean != null) {
                userIdArray.add(groupContactBean.accid);
            }
        }
        JsonObject param = new JsonObject();
        param.add("members", userIdArray);
        String paramJsonStr = null;
        try {
            paramJsonStr = JsonUtils.Gson2String(param);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        showLoadingDialog(null);
        getApi().groupMemberAdd(getIntent().getStringExtra(KEY_TID), RequestUtils.createJsonBody(paramJsonStr))
                .enqueue(new SimpleCallBack<JsonElement>() {
                    @Override
                    public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contactDbService != null) {
            contactDbService.releaseService();
        }
    }
}
