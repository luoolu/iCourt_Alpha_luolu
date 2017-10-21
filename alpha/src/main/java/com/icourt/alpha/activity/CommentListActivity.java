package com.icourt.alpha.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.CommentListAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.HeaderFooterAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.DataChangeAdapterObserver;
import com.icourt.alpha.base.BaseActivity;
import com.icourt.alpha.entity.bean.CommentEntity;
import com.icourt.alpha.entity.bean.TaskEntity;
import com.icourt.alpha.entity.event.TaskActionEvent;
import com.icourt.alpha.fragment.dialogfragment.ContactDialogFragment;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.utils.ActionConstants;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.ItemDecorationUtils;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.alpha.utils.SystemUtils;
import com.icourt.alpha.view.SoftKeyboardSizeWatchLayout;
import com.icourt.alpha.widget.dialog.BottomActionDialog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;
import com.zhaol.refreshlayout.EmptyRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Description  评论列表页
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/12
 * version 2.0.0
 */

public class CommentListActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemChildClickListener, BaseRecyclerAdapter.OnItemLongClickListener {

    private static final String KEY_TASK_ID = "key_task_id";
    private static final String KEY_OPEN_SOFT_KEYBOARD = "key_open_Soft_Keyboard";
    @BindView(R.id.titleView)
    AppBarLayout titleView;
    @BindView(R.id.recyclerview)
    EmptyRecyclerView recyclerview;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.comment_edit)
    EditText commentEdit;
    @BindView(R.id.comment_tv)
    TextView commentTv;
    @BindView(R.id.send_tv)
    TextView sendTv;

    TaskEntity.TaskItemEntity taskItemEntity;
    int pageIndex, commentCount;
    CommentListAdapter commentListAdapter;
    @BindView(R.id.titleBack)
    ImageView titleBack;
    @BindView(R.id.titleContent)
    TextView titleContent;
    HeaderFooterAdapter<CommentListAdapter> headerFooterAdapter;
    @BindView(R.id.softKeyboardSizeWatchLayout)
    SoftKeyboardSizeWatchLayout softKeyboardSizeWatchLayout;
    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;


    /**
     * 返回最终评论数
     *
     * @param context
     * @param taskItemEntity
     * @param requestCode
     * @param openSoftKeyboard
     */
    public static void launchForResult(@NonNull Activity context,
                                       @NonNull TaskEntity.TaskItemEntity taskItemEntity,
                                       int requestCode,
                                       boolean openSoftKeyboard) {
        if (context == null) return;
        if (taskItemEntity == null) return;
        Intent intent = new Intent(context, CommentListActivity.class);
        intent.putExtra(KEY_TASK_ID, taskItemEntity);
        intent.putExtra(KEY_OPEN_SOFT_KEYBOARD, openSoftKeyboard);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 是否打开软键盘
     *
     * @return
     */
    private boolean shouldOpenSoftKeyboard() {
        return getIntent().getBooleanExtra(KEY_OPEN_SOFT_KEYBOARD, true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (shouldOpenSoftKeyboard()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        setContentView(R.layout.activity_comment_list_layout);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle("查看评论");
        taskItemEntity = (TaskEntity.TaskItemEntity) getIntent().getSerializableExtra(KEY_TASK_ID);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        headerFooterAdapter = new HeaderFooterAdapter<>(commentListAdapter = new CommentListAdapter());
        View footerview = HeaderFooterAdapter.inflaterView(this, R.layout.footer_comment_list_layout, recyclerview.getRecyclerView());
        TextView contentTv = (TextView) footerview.findViewById(R.id.content_tv);
        if (taskItemEntity != null) {
            if (taskItemEntity.createUser != null)
                contentTv.setText(String.format("%s 创建了任务 %s", taskItemEntity.createUser.userName, DateUtils.getTimeDateFormatMm(taskItemEntity.createTime)));
            bottomLayout.setVisibility(taskItemEntity.valid ? View.VISIBLE : View.GONE);
            commentCount = taskItemEntity.commentCount;
            commentTv.setText(String.format("%s条动态", commentCount));
        }
        headerFooterAdapter.addFooter(footerview);

        recyclerview.setNoticeEmpty(R.mipmap.bg_no_task, R.string.task_no_comment_text);

        recyclerview.addItemDecoration(ItemDecorationUtils.getCommFull05Divider(this, true));
        commentListAdapter.registerAdapterDataObserver(new DataChangeAdapterObserver() {
            @Override
            protected void updateUI() {
                if (getIntent() != null) {
                    Intent intent = getIntent();
                    int count = commentListAdapter.getItemCount();
                    intent.putExtra(KEY_ACTIVITY_RESULT, count);
                    setResult(RESULT_OK, intent);
                }
            }
        });
//        commentListAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, commentListAdapter));
        commentListAdapter.setOnItemChildClickListener(this);
        commentListAdapter.setOnItemLongClickListener(this);
        recyclerview.setAdapter(headerFooterAdapter);
        refreshLayout.setOnRefreshLoadmoreListener(new OnRefreshLoadmoreListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getData(true);
            }

            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                getData(false);
            }
        });
        commentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    if (s.toString().length() > 0) {
                        commentTv.setVisibility(View.GONE);
                        sendTv.setVisibility(View.VISIBLE);
                    } else {
                        commentTv.setVisibility(View.VISIBLE);
                        sendTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        refreshLayout.autoRefresh();
        commentEdit.setMaxEms(1500);
        recyclerview.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING: {
                        SystemUtils.hideSoftKeyBoard(CommentListActivity.this);
                    }
                    break;
                }
            }
        });
    }

    @Override
    protected void getData(final boolean isRefresh) {
        super.getData(isRefresh);
        if (isRefresh) {
            pageIndex = 1;
        }
        if (taskItemEntity == null) return;
        callEnqueue(getApi().commentListQuery(
                100,
                taskItemEntity.id,
                pageIndex,
                ActionConstants.DEFAULT_PAGE_SIZE),
                new SimpleCallBack<CommentEntity>() {
                    @Override
                    public void onSuccess(Call<ResEntity<CommentEntity>> call, Response<ResEntity<CommentEntity>> response) {
                        stopRefresh();
                        commentListAdapter.bindData(isRefresh, response.body().result.items);
                        pageIndex += 1;
                        enableLoadMore(response.body().result.items);
                    }

                    @Override
                    public void onFailure(Call<ResEntity<CommentEntity>> call, Throwable t) {
                        super.onFailure(call, t);
                        stopRefresh();
                    }
                });
    }

    @OnClick({R.id.send_tv})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_tv:
                sendComment();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void enableLoadMore(List result) {
        if (refreshLayout != null) {
            refreshLayout.setEnableLoadmore(result != null
                    && result.size() >= ActionConstants.DEFAULT_PAGE_SIZE);
        }
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadmore();
        }
    }

    /**
     * 添加评论
     */
    private void sendComment() {
        if (taskItemEntity == null) return;
        if (TextUtils.isEmpty(commentEdit.getText())) return;
        String content = commentEdit.getText().toString();

        if (StringUtils.isEmpty(content)) {
            showTopSnackBar("请输入评论内容");
            commentEdit.setText("");
            return;
        } else if (commentEdit.getText().length() > 1500) {
            showTopSnackBar("评论内容不能超过1500字");
            return;
        }
        showLoadingDialog(null);
        callEnqueue(getApi().commentCreate(100, taskItemEntity.id, content),
                new SimpleCallBack<JsonElement>() {
                    @Override
                    public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                        dismissLoadingDialog();
                        CommentEntity.CommentItemEntity commentItemEntity = getNewComment();
                        commentItemEntity.id = response.body().result.getAsString();
                        commentListAdapter.addItem(0, commentItemEntity);
                        commentEdit.setText("");
                        commentEdit.clearFocus();
                        recyclerview.getRecyclerView().scrollToPosition(0);
                        commentTv.setVisibility(View.VISIBLE);
                        sendTv.setVisibility(View.GONE);
                        commentCount += 1;
                        commentTv.setText(String.format("%s条动态", commentCount));
                        EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
                    }

                    @Override
                    public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                        super.onFailure(call, t);
                        dismissLoadingDialog();
                        showTopSnackBar("添加评论失败");
                    }
                });
    }

    /**
     * 获取新的评论实体
     *
     * @return
     */
    private CommentEntity.CommentItemEntity getNewComment() {
        CommentEntity.CommentItemEntity commentItemEntity = new CommentEntity.CommentItemEntity();
        commentItemEntity.createTime = DateUtils.millis();
        commentItemEntity.content = commentEdit.getText().toString();
        CommentEntity.CommentItemEntity.CreateUser createUser = new CommentEntity.CommentItemEntity.CreateUser();
        createUser.userId = getLoginUserId();
        createUser.userName = getLoginUserInfo().getName();
        createUser.pic = getLoginUserInfo().getPic();
        commentItemEntity.createUser = createUser;
        return commentItemEntity;
    }

    /**
     * 展示联系人对话框
     *
     * @param accid
     * @param hiddenChatBtn
     */
    public void showContactDialogFragment(String accid, boolean hiddenChatBtn) {
        String tag = ContactDialogFragment.class.getSimpleName();
        FragmentTransaction mFragTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        ContactDialogFragment.newInstance(accid, "成员资料", hiddenChatBtn)
                .show(mFragTransaction, tag);
    }

    @Override
    public void onItemChildClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        CommentEntity.CommentItemEntity commentItemEntity = (CommentEntity.CommentItemEntity) adapter.getItem(position);
        switch (view.getId()) {
            case R.id.user_photo_image:
                if (commentItemEntity.createUser != null) {
                    if (!TextUtils.isEmpty(commentItemEntity.createUser.userId))
                        showContactDialogFragment(commentItemEntity.createUser.userId.toLowerCase(), true);
                }
                break;
        }
    }

    @Override
    public boolean onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        CommentEntity.CommentItemEntity entity = (CommentEntity.CommentItemEntity) adapter.getItem(adapter.getRealPos(position));
        if (entity != null) {
            if (entity.createUser == null) {
                return false;
            }
            if (TextUtils.isEmpty(entity.createUser.userId)) return false;
            if (TextUtils.isEmpty(getLoginUserId())) return false;
            if (TextUtils.equals(entity.createUser.userId.toLowerCase(), getLoginUserId().toLowerCase())) {
                showSelfBottomMenu(entity);
            } else {
                showOthersBottomMenu(entity);
            }
        }
        return true;
    }

    /**
     * 显示我的底部菜单
     */
    private void showSelfBottomMenu(final CommentEntity.CommentItemEntity commentItemEntity) {
        if (commentItemEntity == null) return;
        new BottomActionDialog(getContext(),
                null,
                Arrays.asList("复制", "删除"),
                new BottomActionDialog.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(BottomActionDialog dialog, BottomActionDialog.ActionItemAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                commentActionCopy(commentItemEntity.content);
                                break;
                            case 1:
                                showDeleteDialog("是非成败转头空，确定要删除吗?", commentItemEntity);
                                break;
                        }
                    }
                }).show();
    }

    /**
     * 显示他人底部菜单
     */
    private void showOthersBottomMenu(final CommentEntity.CommentItemEntity commentItemEntity) {
        if (commentItemEntity == null) return;
        new BottomActionDialog(getContext(),
                null,
                Arrays.asList("复制"),
                new BottomActionDialog.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(BottomActionDialog dialog, BottomActionDialog.ActionItemAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                commentActionCopy(commentItemEntity.content);
                                break;
                        }
                    }
                }).show();
    }

    /**
     * 删除评论确定弹框
     *
     * @param message
     * @param commentItemEntity
     */
    private void showDeleteDialog(String message, final CommentEntity.CommentItemEntity commentItemEntity) {
        DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE://确定
                        deleteComment(commentItemEntity);
                        break;
                    case Dialog.BUTTON_NEGATIVE://取消
                        break;
                }
            }
        };
        //dialog参数设置
        new AlertDialog.Builder(this)
                .setTitle("提示")//设置标题
                .setMessage(message) //设置内容
                .setPositiveButton("确认", dialogOnclicListener)
                .setNegativeButton("取消", dialogOnclicListener)
                .show();
    }

    /**
     * 评论复制
     *
     * @param charSequence
     */
    protected final void commentActionCopy(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) return;
        SystemUtils.copyToClipboard(getContext(), "comment", charSequence);
        showTopSnackBar("复制成功");
    }

    /**
     * 删除评论
     *
     * @param commentItemEntity
     */
    private void deleteComment(final CommentEntity.CommentItemEntity commentItemEntity) {
        if (commentItemEntity == null) return;
        showLoadingDialog(null);
        callEnqueue(getApi().taskDeleteComment(commentItemEntity.id), new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                if (commentListAdapter != null) {
                    commentListAdapter.removeItem(commentItemEntity);
                    commentCount -= 1;
                    commentTv.setText(String.format("%s条动态", commentCount));
                    EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
                }
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
            }
        });
    }
}
