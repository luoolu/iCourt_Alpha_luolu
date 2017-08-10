package com.icourt.alpha.fragment;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icourt.alpha.R;
import com.icourt.alpha.activity.SearchProjectActivity;
import com.icourt.alpha.adapter.TaskAdapter;
import com.icourt.alpha.adapter.TaskItemAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseArrayRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.HeaderFooterAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.RefreshViewEmptyObserver;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.entity.bean.ProjectEntity;
import com.icourt.alpha.entity.bean.TaskEntity;
import com.icourt.alpha.entity.bean.TaskGroupEntity;
import com.icourt.alpha.entity.bean.TaskReminderEntity;
import com.icourt.alpha.entity.bean.TimeEntity;
import com.icourt.alpha.entity.event.TaskActionEvent;
import com.icourt.alpha.entity.event.TimingEvent;
import com.icourt.alpha.fragment.dialogfragment.DateSelectDialogFragment;
import com.icourt.alpha.fragment.dialogfragment.ProjectSelectDialogFragment;
import com.icourt.alpha.fragment.dialogfragment.TaskAllotSelectDialogFragment;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.interfaces.OnTasksChangeListener;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.DensityUtil;
import com.icourt.alpha.utils.ItemDecorationUtils;
import com.icourt.alpha.utils.JsonUtils;
import com.icourt.alpha.view.xrefreshlayout.RefreshLayout;
import com.icourt.alpha.widget.manager.TimerManager;
import com.icourt.api.RequestUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

import static com.icourt.alpha.fragment.TabTaskFragment.select_position;

/**
 * Description 任务列表
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/3
 * version 2.0.0
 */

public class TaskListFragment extends BaseFragment implements TaskAdapter.OnShowFragmenDialogListener,
        OnFragmentCallBackListener, ProjectSelectDialogFragment.OnProjectTaskGroupSelectListener {

    public static final int TYPE_ALL = 0;//全部
    public static final int TYPE_NEW = 1;//新任务
    public static final int TYPE_MY_ATTENTION = 2;//我关注的
    Unbinder unbinder;
    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @Nullable
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;

    LinearLayoutManager linearLayoutManager;
    TaskAdapter taskAdapter;
    TaskItemAdapter taskItemAdapter;
    TaskEntity.TaskItemEntity updateTaskItemEntity;
    List<TaskEntity> allTaskEntities;
    List<TaskEntity.TaskItemEntity> todayTaskEntities;//今天到期
    List<TaskEntity.TaskItemEntity> beAboutToTaskEntities;//即将到期
    List<TaskEntity.TaskItemEntity> futureTaskEntities;//未来
    List<TaskEntity.TaskItemEntity> noDueTaskEntities;//为指定到期
    List<TaskEntity.TaskItemEntity> newTaskEntities;//新任务
    List<TaskEntity.TaskItemEntity> datedTaskEntities;//已过期

    int type, stateType = 0;//全部任务：－1；已完成：1；未完成：0；已删除：3；
    HeaderFooterAdapter<TaskAdapter> headerFooterAdapter;
    HeaderFooterAdapter<TaskItemAdapter> headerFooterItemAdapter;
    OnTasksChangeListener onTasksChangeListener;
    boolean isFirstTimeIntoPage = true;
    @BindView(R.id.new_task_cardview)
    CardView newTaskCardview;
    @BindView(R.id.new_task_count_tv)
    TextView newTaskCountTv;
    @BindView(R.id.next_task_close_iv)
    ImageView nextTaskCloseIv;
    @BindView(R.id.next_task_tv)
    TextView nextTaskTv;
    @BindView(R.id.next_task_layout)
    LinearLayout nextTaskLayout;
    @BindView(R.id.next_task_cardview)
    CardView nextTaskCardview;

    Handler handler = new Handler();
    boolean isAwayScroll = false; //切换时是否滚动，在'已完成和已删除'状态下，点击新任务提醒。

    public static TaskListFragment newInstance(int type) {
        TaskListFragment projectTaskFragment = new TaskListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        projectTaskFragment.setArguments(bundle);
        return projectTaskFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_project_mine, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnTasksChangeListener) {
            onTasksChangeListener = (OnTasksChangeListener) getParentFragment();
        } else {
            try {
                onTasksChangeListener = (OnTasksChangeListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        type = getArguments().getInt("type");
        refreshLayout.setNoticeEmpty(R.mipmap.bg_no_task, R.string.task_list_null_text);
        refreshLayout.setMoveForHorizontal(true);
        recyclerView.setLayoutManager(linearLayoutManager = new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(ItemDecorationUtils.getCommTrans5Divider(getContext(), true));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        if (stateType == 0) {
            headerFooterAdapter = new HeaderFooterAdapter<>(taskAdapter = new TaskAdapter());
            View headerView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.header_search_comm, recyclerView);
            View rl_comm_search = headerView.findViewById(R.id.rl_comm_search);
            registerClick(rl_comm_search);
            headerFooterAdapter.addHeader(headerView);
            taskAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, taskAdapter));
            recyclerView.setAdapter(headerFooterAdapter);
            taskAdapter.setDeleteTask(true);
            taskAdapter.setEditTask(true);
            taskAdapter.setAddTime(true);
            taskAdapter.setOnShowFragmenDialogListener(this);
        } else if (stateType == 1 || stateType == 3) {
            headerFooterItemAdapter = new HeaderFooterAdapter<>(taskItemAdapter = new TaskItemAdapter());

            View headerView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.header_search_comm, recyclerView);
            View rl_comm_search = headerView.findViewById(R.id.rl_comm_search);
            registerClick(rl_comm_search);
            headerFooterItemAdapter.addHeader(headerView);
            taskItemAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, taskItemAdapter));
            recyclerView.setAdapter(headerFooterItemAdapter);
        }


        refreshLayout.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                super.onRefresh(isPullDown);
                getData(true);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                super.onLoadMore(isSilence);
            }
        });
        if (type == TYPE_ALL)
            refreshLayout.startRefresh();

        allTaskEntities = new ArrayList<>();
        todayTaskEntities = new ArrayList<>();
        beAboutToTaskEntities = new ArrayList<>();
        futureTaskEntities = new ArrayList<>();
        noDueTaskEntities = new ArrayList<>();
        newTaskEntities = new ArrayList<>();
        datedTaskEntities = new ArrayList<>();
    }

    @OnClick({R.id.new_task_cardview,
            R.id.next_task_close_iv,
            R.id.next_task_cardview})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_comm_search:
                SearchProjectActivity.launchTask(getContext(), getLoginUserId(), type, SearchProjectActivity.SEARCH_TASK);
                break;
            case R.id.new_task_cardview:
                if (getParentFragment() instanceof TaskAllFragment) {
                    if (getParentFragment().getParentFragment() instanceof TabTaskFragment) {
                        if (select_position != 0) {
                            ((TabTaskFragment) (getParentFragment().getParentFragment())).setFirstTabText("未完成", 0);
                            stateType = 0;
                            isAwayScroll = true;
                            getData(true);
                        } else {
                            if (newTaskEntities != null) {
                                if (newTaskEntities.size() > 1) {
                                    nextTaskLayout.setVisibility(View.VISIBLE);
                                    updateNextTaskState();
                                    v.setClickable(false);
                                } else if (newTaskEntities.size() == 1) {
                                    if (newTaskEntities.get(0) != null) {
                                        updateNextTaskState();
                                        scrollToByPosition(newTaskEntities.get(0).id);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.next_task_cardview://下一个
                updateNextTaskState();
                break;
            case R.id.next_task_close_iv://关闭'下一个'弹框,全部修改为已读
                if (newTaskEntities != null) {
                    showLoadingDialog(null);
                    List<String> ids = new ArrayList<>();
                    for (TaskEntity.TaskItemEntity newTaskEntity : newTaskEntities) {
                        ids.add(newTaskEntity.id);
                    }
                    onCheckNewTask(ids);
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 下一个
     */
    private void updateNextTaskState() {
        if (newTaskEntities != null) {
            if (newTaskEntities.size() > 0) {
                if (newTaskEntities.get(0) != null) {
                    scrollToByPosition(newTaskEntities.get(0).id);
                    List<String> ids = new ArrayList<>();
                    ids.add(newTaskEntities.get(0).id);
                    onCheckNewTask(ids);
                    isAwayScroll = false;
                }
            }
        }
    }

    View childItemView;
    boolean isUpdate = true;

    /**
     * 滚动到指定位置
     *
     * @param taskId
     */
    private void scrollToByPosition(final String taskId) {
        isUpdate = true;
        int parentPosition = getParentPositon(taskId) + headerFooterAdapter.getHeaderCount();
        final int childPosition = getChildPositon(taskId);
        int offset = childPosition * 130 + 46;
        linearLayoutManager.scrollToPositionWithOffset(parentPosition, DensityUtil.dip2px(getContext(), offset) * -1);
        linearLayoutManager.setStackFromEnd(true);

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUpdate)
                    updateItemViewBackgrond(taskId, childPosition);
            }
        }, 300);
    }

    /**
     * 改变itemview背景颜色
     *
     * @param taskId
     * @param childPosition
     */
    private void updateItemViewBackgrond(String taskId, int childPosition) {
        RecyclerView childRecyclerview = getChildRecyclerView(taskId);
        if (childRecyclerview != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) childRecyclerview.getLayoutManager();
            if (layoutManager != null) {
                BaseRecyclerAdapter.ViewHolder viewHolder = (BaseRecyclerAdapter.ViewHolder) childRecyclerview.findViewHolderForAdapterPosition(childPosition);
                if (viewHolder != null) {
                    childItemView = viewHolder.itemView;
                    if (childItemView != null) {
                        isUpdate = false;
                        startViewAnim(childItemView);
                    }
                }
            }

        }
    }

    /**
     * itemview渐变动画
     *
     * @param view
     */
    private void startViewAnim(View view) {
        CardView cardView = null;
        if (view instanceof CardView) {
            cardView = (CardView) view;
        }
        if (cardView != null) {
            ValueAnimator colorAnim = ObjectAnimator.ofInt(cardView, "CardBackgroundColor", 0xFFFCCEA7, 0xFFFFF6E9, 0xFFFFFFFF);
            colorAnim.setDuration(3000);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.addListener(animatorListener);
            colorAnim.start();
        }
    }

    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (childItemView != null) {
                if (childItemView instanceof CardView) {
                    CardView cardView = (CardView) childItemView;
                    cardView.setCardBackgroundColor(0xFFFFFFFF);
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    @Override
    public void notifyFragmentUpdate(Fragment targetFrgament, int type, Bundle bundle) {
        super.notifyFragmentUpdate(targetFrgament, type, bundle);
        if (targetFrgament != this) return;
        if (bundle != null) {
            stateType = bundle.getInt("stateType");
        }
        //刷新
        if (targetFrgament == this && (type == 100 || type == TYPE_MY_ATTENTION)
                && recyclerView != null) {
            getData(true);
        }
    }

    @Override
    protected void getData(boolean isRefresh) {
        clearLists();
        int attentionType = 0;
        if (type == TYPE_ALL) {
            attentionType = 0;
        } else if (type == TYPE_MY_ATTENTION) {
            attentionType = 1;
        }
        getApi().taskListQuery(0,
                getLoginUserId(),
                stateType,
                attentionType,
                "dueTime",
                1,
                -1,
                0).enqueue(new SimpleCallBack<TaskEntity>() {
            @Override
            public void onSuccess(Call<ResEntity<TaskEntity>> call, Response<ResEntity<TaskEntity>> response) {
                stopRefresh();
                getTaskGroupData(response.body().result);
                if (response.body().result != null) {
                    if (type == TYPE_ALL && onTasksChangeListener != null) {
                        onTasksChangeListener.onTasksChanged(response.body().result.items);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResEntity<TaskEntity>> call, Throwable t) {
                super.onFailure(call, t);
                stopRefresh();
            }
        });

    }

    /**
     * 对接口返回数据进行分组(今天、即将到期、未来、未指定日期)
     *
     * @param taskEntity
     */
    private void getTaskGroupData(final TaskEntity taskEntity) {
        if (taskEntity == null) return;
        if (taskEntity.items == null) return;
        if (stateType == 0) {
            Observable.create(new ObservableOnSubscribe<List<TaskEntity>>() {
                @Override
                public void subscribe(ObservableEmitter<List<TaskEntity>> e) throws Exception {
                    if (e.isDisposed()) return;
                    groupingByTasks(taskEntity.items);
                    addDataToAllTask();
                    e.onNext(allTaskEntities);
                    e.onComplete();
                }
            }).compose(this.<List<TaskEntity>>bindToLifecycle())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<TaskEntity>>() {
                        @Override
                        public void accept(List<TaskEntity> searchPolymerizationEntities) throws Exception {
                            if (headerFooterAdapter != null) {
                                recyclerView.setAdapter(headerFooterAdapter);
                            }
                            taskAdapter.bindData(true, allTaskEntities);
                            if (isAwayScroll) {
                                updateNextTaskState();

                            }
                            if (linearLayoutManager.getStackFromEnd())
                                linearLayoutManager.setStackFromEnd(false);
                            if (newTaskEntities.size() > 0) {
                                newTaskCardview.setVisibility(View.VISIBLE);
                                newTaskCountTv.setText(String.valueOf(newTaskEntities.size()));
                            }
                            //第一次进入 隐藏搜索框
                            if (isFirstTimeIntoPage) {
                                linearLayoutManager.scrollToPositionWithOffset(headerFooterAdapter.getHeaderCount(), 0);
                                isFirstTimeIntoPage = false;
                            }
                        }
                    });
        } else if (stateType == 1 || stateType == 3) {
            if (headerFooterItemAdapter == null) {
                headerFooterItemAdapter = new HeaderFooterAdapter<>(taskItemAdapter = new TaskItemAdapter());

                View headerView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.header_search_comm, recyclerView);
                View rl_comm_search = headerView.findViewById(R.id.rl_comm_search);
                registerClick(rl_comm_search);
                headerFooterItemAdapter.addHeader(headerView);
                taskItemAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, taskItemAdapter));
            }
            recyclerView.setAdapter(headerFooterItemAdapter);
            taskItemAdapter.bindData(true, taskEntity.items);
            getNewTasksCount();
            if (linearLayoutManager.getStackFromEnd())
                linearLayoutManager.setStackFromEnd(false);
        }
    }

    /**
     * 分组
     *
     * @param taskItemEntities
     */
    private void groupingByTasks(List<TaskEntity.TaskItemEntity> taskItemEntities) {
        for (TaskEntity.TaskItemEntity taskItemEntity : taskItemEntities) {
            if (taskItemEntity.dueTime > 0) {
                if (TextUtils.equals(DateUtils.getTimeDateFormatYear(taskItemEntity.dueTime), DateUtils.getTimeDateFormatYear(DateUtils.millis())) || DateUtils.getDayDiff(DateUtils.millis(), taskItemEntity.dueTime) < 0) {
                    todayTaskEntities.add(taskItemEntity);
                } else if (DateUtils.getDayDiff(DateUtils.millis(), taskItemEntity.dueTime) <= 3 && DateUtils.getDayDiff(DateUtils.millis(), taskItemEntity.dueTime) > 0) {
                    beAboutToTaskEntities.add(taskItemEntity);
                } else if (DateUtils.getDayDiff(DateUtils.millis(), taskItemEntity.dueTime) > 3) {
                    futureTaskEntities.add(taskItemEntity);
                } else {
                    datedTaskEntities.add(taskItemEntity);
                }
            } else {
                noDueTaskEntities.add(taskItemEntity);
            }
            if (DateUtils.millis() - taskItemEntity.assignTime <= TimeUnit.DAYS.toMillis(1) && !TextUtils.isEmpty(getLoginUserId())) {
                if (taskItemEntity.createUser != null) {
                    if (!TextUtils.equals(taskItemEntity.createUser.userId, getLoginUserId())) {
                        if (!TextUtils.isEmpty(taskItemEntity.readUserIds)) {
                            if (!taskItemEntity.readUserIds.contains(getLoginUserId())) {
                                newTaskEntities.add(taskItemEntity);
                            }
                        } else {
                            newTaskEntities.add(taskItemEntity);
                        }
                    }
                }
            }
        }
    }

    /**
     * 分组内容添加到allTaskEntities
     */
    private void addDataToAllTask() {
        if (datedTaskEntities.size() > 0) {
            TaskEntity todayTask = new TaskEntity();
            todayTask.items = datedTaskEntities;
            todayTask.groupName = "已到期";
            todayTask.groupTaskCount = datedTaskEntities.size();
            allTaskEntities.add(todayTask);
        }
        if (todayTaskEntities.size() > 0) {
            TaskEntity todayTask = new TaskEntity();
            todayTask.items = todayTaskEntities;
            todayTask.groupName = "今天到期";
            todayTask.groupTaskCount = todayTaskEntities.size();
            allTaskEntities.add(todayTask);
        }

        if (beAboutToTaskEntities.size() > 0) {
            TaskEntity task = new TaskEntity();
            task.items = beAboutToTaskEntities;
            task.groupName = "即将到期";
            task.groupTaskCount = beAboutToTaskEntities.size();
            allTaskEntities.add(task);
        }

        if (futureTaskEntities.size() > 0) {
            TaskEntity task = new TaskEntity();
            task.items = futureTaskEntities;
            task.groupName = "未来";
            task.groupTaskCount = futureTaskEntities.size();
            allTaskEntities.add(task);
        }

        if (noDueTaskEntities.size() > 0) {
            TaskEntity task = new TaskEntity();
            task.items = noDueTaskEntities;
            task.groupName = "未指定到期日";
            task.groupTaskCount = noDueTaskEntities.size();
            allTaskEntities.add(task);
        }
    }

    private void clearLists() {
        if (allTaskEntities != null)
            allTaskEntities.clear();
        if (datedTaskEntities != null)
            datedTaskEntities.clear();
        if (todayTaskEntities != null)
            todayTaskEntities.clear();
        if (beAboutToTaskEntities != null)
            beAboutToTaskEntities.clear();
        if (futureTaskEntities != null)
            futureTaskEntities.clear();
        if (noDueTaskEntities != null)
            noDueTaskEntities.clear();
        if (newTaskEntities != null)
            newTaskEntities.clear();
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.stopRefresh();
            refreshLayout.stopLoadMore();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteTaskEvent(TaskActionEvent event) {
        if (event == null) return;
        if (event.action == TaskActionEvent.TASK_REFRESG_ACTION) {
            refreshLayout.startRefresh();
        }
    }

    /**
     * 计时事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimingEvent event) {
        if (event == null) return;
        switch (event.action) {
            case TimingEvent.TIMING_ADD:
                TimeEntity.ItemEntity updateItem = TimerManager.getInstance().getTimer();
                if (updateItem != null) {
                    updateChildTimeing(updateItem.taskPkId, true);
                }
                break;
            case TimingEvent.TIMING_UPDATE_PROGRESS:

                break;
            case TimingEvent.TIMING_STOP:
                if (lastEntity != null) {
                    lastEntity.isTiming = false;
                    taskAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 获取item所在父容器position
     *
     * @param taskId
     * @return
     */
    private int getParentPositon(String taskId) {
        if (taskAdapter.getData() != null) {
            for (int i = 0; i < taskAdapter.getData().size(); i++) {
                TaskEntity task = taskAdapter.getData().get(i);
                if (task != null && task.items != null) {
                    for (int j = 0; j < task.items.size(); j++) {
                        TaskEntity.TaskItemEntity item = task.items.get(j);
                        if (item != null) {
                            if (TextUtils.equals(item.id, taskId)) {
                                return i;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 获取item所在子容器position
     *
     * @param taskId
     * @return
     */
    private int getChildPositon(String taskId) {
        if (taskAdapter.getData() != null) {
            for (int i = 0; i < taskAdapter.getData().size(); i++) {
                TaskEntity task = taskAdapter.getData().get(i);
                if (task != null && task.items != null) {
                    for (int j = 0; j < task.items.size(); j++) {
                        TaskEntity.TaskItemEntity item = task.items.get(j);
                        if (item != null) {
                            if (TextUtils.equals(item.id, taskId)) {
                                return j;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 获取子view中的RecyclerView
     *
     * @param taskId
     * @return
     */
    private RecyclerView getChildRecyclerView(String taskId) {
        int parentPos = getParentPositon(taskId) + headerFooterAdapter.getHeaderCount();
        if (parentPos > 0) {
            int childPos = getChildPositon(taskId);
            if (childPos >= 0) {
                BaseArrayRecyclerAdapter.ViewHolder viewHolderForAdapterPosition = (BaseArrayRecyclerAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(parentPos);
                if (viewHolderForAdapterPosition != null) {
                    RecyclerView recyclerview = viewHolderForAdapterPosition.obtainView(R.id.parent_item_task_recyclerview);
                    return recyclerview;
                }
            }
        }
        return null;
    }

    TaskEntity.TaskItemEntity lastEntity;

    /**
     * 更新item
     *
     * @param taskId
     */
    private void updateChildTimeing(String taskId, boolean isTiming) {
        int parentPos = getParentPositon(taskId) + headerFooterAdapter.getHeaderCount();
        if (parentPos > 0) {
            int childPos = getChildPositon(taskId);
            if (childPos >= 0) {
                BaseArrayRecyclerAdapter.ViewHolder viewHolderForAdapterPosition = (BaseArrayRecyclerAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(parentPos);
                if (viewHolderForAdapterPosition != null) {
                    RecyclerView recyclerview = viewHolderForAdapterPosition.obtainView(R.id.parent_item_task_recyclerview);
                    if (recyclerview != null) {
                        TaskItemAdapter itemAdapter = (TaskItemAdapter) recyclerview.getAdapter();
                        if (itemAdapter != null) {
                            TaskEntity.TaskItemEntity entity = itemAdapter.getItem(childPos);
                            if (entity != null) {
                                if (lastEntity != null)
                                    if (!TextUtils.equals(entity.id, lastEntity.id)) {
                                        lastEntity.isTiming = false;
                                        taskAdapter.notifyDataSetChanged();
                                    }
                                if (entity.isTiming != isTiming) {
                                    entity.isTiming = isTiming;
                                    itemAdapter.updateItem(entity);
                                    lastEntity = entity;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            taskAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 展示选择负责人对话框
     */
    public void showTaskAllotSelectDialogFragment(String projectId, List<TaskEntity.TaskItemEntity.AttendeeUserEntity> attendeeUsers) {
        String tag = TaskAllotSelectDialogFragment.class.getSimpleName();
        FragmentTransaction mFragTransaction = getChildFragmentManager().beginTransaction();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }

        TaskAllotSelectDialogFragment.newInstance(projectId, attendeeUsers)
                .show(mFragTransaction, tag);
    }

    /**
     * 展示选择项目对话框
     */
    public void showProjectSelectDialogFragment() {
        String tag = ProjectSelectDialogFragment.class.getSimpleName();
        FragmentTransaction mFragTransaction = getChildFragmentManager().beginTransaction();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }

        ProjectSelectDialogFragment.newInstance()
                .show(mFragTransaction, tag);
    }

    /**
     * 展示选择到期时间对话框
     */
    private void showDateSelectDialogFragment(long dueTime, String taskId) {
        String tag = DateSelectDialogFragment.class.getSimpleName();
        FragmentTransaction mFragTransaction = getChildFragmentManager().beginTransaction();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        Calendar calendar = Calendar.getInstance();
        if (dueTime <= 0) {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
        } else {
            calendar.setTimeInMillis(dueTime);
        }
        DateSelectDialogFragment.newInstance(calendar, null, taskId)
                .show(mFragTransaction, tag);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showUserSelectDialog(String projectId, TaskEntity.TaskItemEntity taskItemEntity) {
        updateTaskItemEntity = taskItemEntity;
        showTaskAllotSelectDialogFragment(projectId, taskItemEntity.attendeeUsers);
    }

    @Override
    public void showDateSelectDialog(TaskEntity.TaskItemEntity taskItemEntity) {
        updateTaskItemEntity = taskItemEntity;
        if (taskItemEntity != null)
            showDateSelectDialogFragment(taskItemEntity.dueTime, taskItemEntity.id);
    }

    @Override
    public void showProjectSelectDialog(TaskEntity.TaskItemEntity taskItemEntity) {
        updateTaskItemEntity = taskItemEntity;
        showProjectSelectDialogFragment();
    }

    @Override
    public void onFragmentCallBack(Fragment fragment, int type, Bundle params) {
        if (params != null) {
            if (fragment instanceof TaskAllotSelectDialogFragment) {
                List<TaskEntity.TaskItemEntity.AttendeeUserEntity> attusers = (List<TaskEntity.TaskItemEntity.AttendeeUserEntity>) params.getSerializable("list");
                if (updateTaskItemEntity.attendeeUsers != null) {
                    updateTaskItemEntity.attendeeUsers.clear();
                    updateTaskItemEntity.attendeeUsers.addAll(attusers);
                    updateTask(updateTaskItemEntity, null, null);
                }
            } else if (fragment instanceof DateSelectDialogFragment) {
                long millis = params.getLong(KEY_FRAGMENT_RESULT);
                updateTaskItemEntity.dueTime = millis;
                updateTask(updateTaskItemEntity, null, null);

                TaskReminderEntity taskReminderEntity = (TaskReminderEntity) params.getSerializable("taskReminder");
                addReminders(updateTaskItemEntity, taskReminderEntity);
            }
        }
    }


    @Override
    public void onProjectTaskGroupSelect(ProjectEntity projectEntity, TaskGroupEntity taskGroupEntity) {

        updateTask2(updateTaskItemEntity, projectEntity, taskGroupEntity);
    }

    /**
     * 获取新任务数量
     */
    private void getNewTasksCount() {
        getApi().newTasksCountQuery().enqueue(new SimpleCallBack<List<String>>() {
            @Override
            public void onSuccess(Call<ResEntity<List<String>>> call, Response<ResEntity<List<String>>> response) {
                if (response.body().result != null) {
                    int totalCount = response.body().result.size();
                    if (totalCount > 0) {
                        newTaskCardview.setVisibility(View.VISIBLE);
                        newTaskCountTv.setText(String.valueOf(totalCount));
                    }
                }
            }
        });
    }

    /**
     * 修改任务
     *
     * @param itemEntity
     */
    private void updateTask2(TaskEntity.TaskItemEntity itemEntity, ProjectEntity projectEntity, TaskGroupEntity taskGroupEntity) {
        showLoadingDialog(null);
        getApi().taskUpdate(RequestUtils.createJsonBody(getTaskJson2(itemEntity, projectEntity, taskGroupEntity))).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                refreshLayout.startRefresh();
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
            }
        });
    }

    /**
     * 修改任务
     *
     * @param itemEntity
     */
    private void updateTask(TaskEntity.TaskItemEntity itemEntity, ProjectEntity projectEntity, TaskGroupEntity taskGroupEntity) {
        showLoadingDialog(null);
        getApi().taskUpdate(RequestUtils.createJsonBody(getTaskJson(itemEntity, projectEntity, taskGroupEntity))).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                refreshLayout.startRefresh();
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
            }
        });
    }

    /**
     * 获取任务json
     *
     * @param itemEntity
     * @return
     */
    private String getTaskJson(TaskEntity.TaskItemEntity itemEntity, ProjectEntity projectEntity, TaskGroupEntity taskGroupEntity) {
        if (itemEntity == null) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", itemEntity.id);
        jsonObject.addProperty("state", itemEntity.state);
        jsonObject.addProperty("valid", true);
        jsonObject.addProperty("name", itemEntity.name);
        jsonObject.addProperty("parentId", itemEntity.parentId);
        jsonObject.addProperty("dueTime", itemEntity.dueTime);
        jsonObject.addProperty("updateTime", DateUtils.millis());
        if (projectEntity != null) {
            jsonObject.addProperty("matterId", projectEntity.pkId);
        }
        if (taskGroupEntity != null) {
            jsonObject.addProperty("parentId", taskGroupEntity.id);
        }
        JsonArray jsonarr = new JsonArray();
        if (itemEntity.attendeeUsers != null) {
            for (TaskEntity.TaskItemEntity.AttendeeUserEntity attendeeUser : itemEntity.attendeeUsers) {
                jsonarr.add(attendeeUser.userId);
            }
        }
        jsonObject.add("attendees", jsonarr);
        return jsonObject.toString();
    }

    /**
     * 获取任务json
     *
     * @param itemEntity
     * @return
     */
    private String getTaskJson2(TaskEntity.TaskItemEntity itemEntity, ProjectEntity projectEntity, TaskGroupEntity taskGroupEntity) {
        if (itemEntity == null) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", itemEntity.id);
        jsonObject.addProperty("state", itemEntity.state);
        jsonObject.addProperty("valid", true);
        jsonObject.addProperty("name", itemEntity.name);
        jsonObject.addProperty("parentId", itemEntity.parentId);
        jsonObject.addProperty("dueTime", itemEntity.dueTime);
        jsonObject.addProperty("updateTime", DateUtils.millis());
        JsonArray jsonarr = new JsonArray();
        if (projectEntity != null) {
            jsonObject.addProperty("matterId", projectEntity.pkId);
            jsonarr.add(getLoginUserId());
        } else {
            if (itemEntity.attendeeUsers != null) {
                for (TaskEntity.TaskItemEntity.AttendeeUserEntity attendeeUser : itemEntity.attendeeUsers) {
                    jsonarr.add(attendeeUser.userId);
                }
            }
        }
        jsonObject.add("attendees", jsonarr);
        if (taskGroupEntity != null) {
            jsonObject.addProperty("parentId", taskGroupEntity.id);
        }
        return jsonObject.toString();
    }

    /**
     * 添加任务提醒
     *
     * @param taskItemEntity
     * @param taskReminderEntity
     */
    private void addReminders(TaskEntity.TaskItemEntity taskItemEntity, final TaskReminderEntity taskReminderEntity) {
        if (taskReminderEntity == null) return;
        if (taskItemEntity == null) return;
        String json = getReminderJson(taskReminderEntity);
        if (TextUtils.isEmpty(json)) return;
        getApi().taskReminderAdd(taskItemEntity.id, RequestUtils.createJsonBody(json)).enqueue(new SimpleCallBack<TaskReminderEntity>() {
            @Override
            public void onSuccess(Call<ResEntity<TaskReminderEntity>> call, Response<ResEntity<TaskReminderEntity>> response) {

            }

            @Override
            public void onFailure(Call<ResEntity<TaskReminderEntity>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 获取提醒json
     *
     * @param taskReminderEntity
     * @return
     */
    private String getReminderJson(TaskReminderEntity taskReminderEntity) {
        try {
            if (taskReminderEntity == null) return null;
            return JsonUtils.getGson().toJson(taskReminderEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 我知道了
     */
    public void onCheckNewTask(final List<String> ids) {
        if (newTaskEntities == null) return;
        getApi().checkAllNewTask(ids).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                if (ids != null) {
                    if (ids.size() == 1) {
                        newTaskEntities.remove(0);
                        if (newTaskEntities.size() > 1) {
                            nextTaskLayout.setVisibility(View.VISIBLE);
                        }
                        newTaskCountTv.setText(String.valueOf(newTaskEntities.size()));
                        nextTaskTv.setText("下一个 (" + String.valueOf(newTaskEntities.size()) + ")");
                    } else {
                        newTaskEntities.clear();
                    }
                    if (newTaskEntities.size() == 0) {
                        newTaskCardview.setVisibility(View.GONE);
                        nextTaskLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
