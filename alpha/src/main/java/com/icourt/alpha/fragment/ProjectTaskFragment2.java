package com.icourt.alpha.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.andview.refreshview.XRefreshView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icourt.alpha.R;
import com.icourt.alpha.activity.SearchProjectActivity;
import com.icourt.alpha.activity.TaskDetailActivity;
import com.icourt.alpha.activity.TimerDetailActivity;
import com.icourt.alpha.activity.TimerTimingActivity;
import com.icourt.alpha.adapter.TaskAdapter;
import com.icourt.alpha.adapter.TaskItemAdapter;
import com.icourt.alpha.adapter.TaskItemAdapter2;
import com.icourt.alpha.adapter.baseadapter.BaseArrayRecyclerAdapter;
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
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.ItemDecorationUtils;
import com.icourt.alpha.utils.UMMobClickAgent;
import com.icourt.alpha.view.xrefreshlayout.RefreshLayout;
import com.icourt.alpha.widget.manager.TimerManager;
import com.icourt.api.RequestUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.umeng.socialize.utils.ContextUtil.getContext;

/**
 * Description 项目下任务列表
 * Company Beijing icourt
 * author  zhaodanyang  E-mail:zhaodanyang@icourt.cc
 * date createTime：17/9/5
 * version 2.0.0
 */

public class ProjectTaskFragment2 extends BaseTaskFragment implements BaseQuickAdapter.OnItemLongClickListener, BaseQuickAdapter.OnItemChildClickListener, BaseQuickAdapter.OnItemClickListener {

    private static final String KEY_PROJECT_ID = "key_project_id";

    Unbinder unbinder;
    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;

    private boolean isFirstTimeIntoPage = true;//用来判断是不是第一次进入该界面，如果是，滚动到一条，隐藏搜索栏。

    TaskItemAdapter2 taskAdapter;
    TaskEntity.TaskItemEntity lastEntity;
    String projectId;
    int startType, finishType;

    private LinearLayoutManager mLinearLayoutManager;

    public static ProjectTaskFragment2 newInstance(@NonNull String projectId) {
        ProjectTaskFragment2 projectTaskFragment = new ProjectTaskFragment2();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PROJECT_ID, projectId);
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
    protected void initView() {
        super.initView();
        projectId = getArguments().getString(KEY_PROJECT_ID);
        refreshLayout.setNoticeEmpty(R.mipmap.bg_no_task, R.string.task_list_null_text);
        refreshLayout.setMoveForHorizontal(true);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setHasFixedSize(true);

        View headerView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.header_search_comm, recyclerView);
        View rl_comm_search = headerView.findViewById(R.id.rl_comm_search);
        registerClick(rl_comm_search);

        taskAdapter = new TaskItemAdapter2();
        taskAdapter.addHeaderView(headerView);
        taskAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, taskAdapter));
        taskAdapter.setOnItemLongClickListener(this);
        taskAdapter.setOnItemChildClickListener(this);
        taskAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(taskAdapter);

        refreshLayout.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                super.onRefresh(isPullDown);
                checkAddTaskAndDocumentPms(projectId);
                getData(true);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                super.onLoadMore(isSilence);
            }
        });
        refreshLayout.startRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (taskAdapter != null)
            taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_comm_search:
                SearchProjectActivity.launchFinishTask(getContext(), "", 0, 0, SearchProjectActivity.SEARCH_TASK, projectId);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 获取权限列表
     */
    private void checkAddTaskAndDocumentPms(String projectId) {
        getApi().permissionQuery(getLoginUserId(), "MAT", projectId).enqueue(new SimpleCallBack<List<String>>() {
            @Override
            public void onSuccess(Call<ResEntity<List<String>>> call, Response<ResEntity<List<String>>> response) {

                if (response.body().result != null) {
                    if (response.body().result.contains("MAT:matter.task:edit")) {
                        isEditTask = true;
                    }
                    if (response.body().result.contains("MAT:matter.task:delete")) {
                        isDeleteTask = true;
                    }
                    if (response.body().result.contains("MAT:matter.timeLog:add")) {
                        isAddTime = true;
                    }
                }
            }
        });
    }

    @Override
    protected void getData(boolean isRefresh) {
        getApi().taskListQueryByMatterId(0, "dueTime", projectId, -1, 1, -1).enqueue(new SimpleCallBack<TaskEntity>() {
            @Override
            public void onSuccess(Call<ResEntity<TaskEntity>> call, Response<ResEntity<TaskEntity>> response) {
                //请求成功之后，要将数据进行分组。
                getTaskGroupDatas(response.body().result);
            }

            @Override
            public void onFailure(Call<ResEntity<TaskEntity>> call, Throwable t) {
                super.onFailure(call, t);
                stopRefresh();
                enableEmptyView(null);
            }
        });
    }

    /**
     * 异步分组
     *
     * @param taskEntity
     */
    private void getTaskGroupDatas(final TaskEntity taskEntity) {
        if (taskEntity != null) {
            enableEmptyView(taskEntity.items);
            if (taskEntity.items != null) {
                Observable.create(new ObservableOnSubscribe<List<TaskEntity.TaskItemEntity>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<TaskEntity.TaskItemEntity>> e) throws Exception {
                        if (e.isDisposed()) return;
                        e.onNext(groupingByTasks(taskEntity.items));
                        e.onComplete();
                    }
                }).compose(this.<List<TaskEntity.TaskItemEntity>>bindToLifecycle())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<TaskEntity.TaskItemEntity>>() {
                            @Override
                            public void accept(List<TaskEntity.TaskItemEntity> searchPolymerizationEntities) throws Exception {
                                stopRefresh();
                                taskAdapter.setAddTime(isAddTime);
                                taskAdapter.setNewData(searchPolymerizationEntities);
                                if (isFirstTimeIntoPage) {
                                    mLinearLayoutManager.scrollToPositionWithOffset(taskAdapter.getHeaderLayoutCount(), 0);
                                    isFirstTimeIntoPage = false;
                                }
                                TimerManager.getInstance().timerQuerySync();
                            }
                        });
            }
        } else {
            enableEmptyView(null);
        }
    }

    /**
     * 任务分组
     *
     * @param taskitems
     */
    private List<TaskEntity.TaskItemEntity> groupingByTasks(List<TaskEntity.TaskItemEntity> taskitems) {
        List<TaskEntity.TaskItemEntity> allTaskEntities = new ArrayList<>();//展示所要用到的列表集合
        List<TaskEntity> taskGroup = new ArrayList<>();//用来存放分组的列表
        List<TaskEntity.TaskItemEntity> noitems = new ArrayList<>();//没有分组的任务列表
        List<TaskEntity.TaskItemEntity> taskEntities = new ArrayList<>();//所有分组了的任务列表
        List<TaskEntity.TaskItemEntity> myStarTaskEntities = new ArrayList<>();//我关注的的任务列表

        TimeEntity.ItemEntity timerEntity = TimerManager.getInstance().getTimer();
        for (TaskEntity.TaskItemEntity taskItemEntity : taskitems) {
            if (TimerManager.getInstance().hasTimer()) {
                if (timerEntity != null) {
                    if (!TextUtils.isEmpty(timerEntity.taskPkId)) {
                        if (TextUtils.equals(timerEntity.taskPkId, taskItemEntity.id)) {
                            taskItemEntity.isTiming = true;
                        }
                    }
                }
            }
            if (taskItemEntity.type == 1) {//1:任务组，将所有任务组单独拿出来
                TaskEntity itemEntity = new TaskEntity();
                itemEntity.groupName = taskItemEntity.name;
                itemEntity.groupId = taskItemEntity.id;
                taskGroup.add(itemEntity);
            } else if (taskItemEntity.type == 0) {//0:任务
                if (TextUtils.isEmpty(taskItemEntity.parentId)) {//根据是否有parentId，判断是否属于哪个任务组
                    noitems.add(taskItemEntity);
                } else {
                    taskEntities.add(taskItemEntity);
                }
                if (taskItemEntity.attentioned == 1) {//我关注的
                    myStarTaskEntities.add(taskItemEntity);
                }
            }
        }
        if (taskGroup.size() > 0) {//遍历所有分组，将有分组的item添加到对应组的列表里面。
            for (TaskEntity allTaskEntity : taskGroup) {
                List<TaskEntity.TaskItemEntity> items = new ArrayList<>();//有分组
                for (TaskEntity.TaskItemEntity entity : taskEntities) {
                    if (TextUtils.equals(allTaskEntity.groupId, entity.parentId)) {
                        items.add(entity);
                    }
                }
                allTaskEntity.items = items;
                allTaskEntity.groupTaskCount = items.size();
            }
        } else {
            if (!taskEntities.isEmpty()) {
                noitems.addAll(taskEntities);
            }
        }
        if (noitems.size() > 0) {
            TaskEntity itemEntity = new TaskEntity();
            itemEntity.groupName = "未分组";
            itemEntity.items = noitems;
            itemEntity.groupTaskCount = noitems.size();
            taskGroup.add(itemEntity);
        }
        if (myStarTaskEntities.size() > 0) {
            TaskEntity itemEntity = new TaskEntity();
            itemEntity.groupName = "我关注的";
            itemEntity.items = myStarTaskEntities;
            itemEntity.groupTaskCount = myStarTaskEntities.size();
            taskGroup.add(0, itemEntity);
        }

        //taskGroup为分组完成的列表，将分组完成的列表转换成我们要显示的数据格式。
        for (TaskEntity taskEntity : taskGroup) {
            TaskEntity.TaskItemEntity itemEntity = new TaskEntity.TaskItemEntity();
            itemEntity.type = 1;//表示是任务组
            itemEntity.groupName = taskEntity.groupName;
            itemEntity.groupTaskCount = taskEntity.groupTaskCount;
            allTaskEntities.add(itemEntity);
            if (taskEntity.items != null) {
                allTaskEntities.addAll(taskEntity.items);
            }
        }
        return allTaskEntities;
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.stopRefresh();
            refreshLayout.stopLoadMore();
        }
    }

    private void enableEmptyView(List result) {
        if (refreshLayout != null) {
            if (result != null) {
                if (result.size() > 0) {
                    refreshLayout.enableEmptyView(false);
                } else {
                    refreshLayout.enableEmptyView(true);
                }
            } else {
                refreshLayout.enableEmptyView(true);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTaskEvent(TaskActionEvent event) {
        if (event == null) return;
        if (event.action == TaskActionEvent.TASK_REFRESG_ACTION) {
            refreshLayout.startRefresh();
        }
    }

    /**
     * 根据任务id，获取任务在Adapter中的位置
     *
     * @param taskId
     * @return
     */
    private int getItemPosition(String taskId) {
        for (int i = 0; i < taskAdapter.getData().size(); i++) {
            TaskEntity.TaskItemEntity taskItemEntity = taskAdapter.getData().get(i);
            if (taskItemEntity.type == 0 && TextUtils.equals(taskItemEntity.id, taskId)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 更新item
     *
     * @param taskId
     */
    private void updateChildTimeing(String taskId, boolean isTiming) {
        int pos = getItemPosition(taskId);
        if (pos >= 0) {
            TaskEntity.TaskItemEntity entity = taskAdapter.getItem(pos);
            if (entity != null) {
                if (lastEntity != null)
                    if (!TextUtils.equals(entity.id, lastEntity.id)) {
                        lastEntity.isTiming = false;
                        taskAdapter.notifyDataSetChanged();
                    }
                if (entity.isTiming != isTiming) {
                    entity.isTiming = isTiming;
                    taskAdapter.updateItem(entity);
                    lastEntity = entity;
                }
            }
        }
    }


    @Override
    protected void startTimingBack(TaskEntity.TaskItemEntity requestEntity, Response<TimeEntity.ItemEntity> response) {
        taskAdapter.updateItem(requestEntity);
        if (response.body() != null) {
            TimerTimingActivity.launch(getActivity(), response.body());
//            TimeEntity.ItemEntity timer = TimerManager.getInstance().getTimer();
//            TimerDetailActivity.launch(getActivity(), timer);
        }
    }

    @Override
    protected void stopTimingBack(TaskEntity.TaskItemEntity requestEntity) {
        taskAdapter.updateItem(requestEntity);
        TimeEntity.ItemEntity timer = TimerManager.getInstance().getTimer();
        TimerDetailActivity.launch(getActivity(), timer);
    }

    @Override
    protected void taskUpdateBack(@ChangeType int actionType, @NonNull TaskEntity.TaskItemEntity itemEntity) {
        if (actionType == CHANGE_DUETIME) {
            getData(true);
        } else {
            taskAdapter.updateItem(itemEntity);
        }
    }

    @Override
    protected void taskTimerUpdateBack(String taskId) {
        if (TextUtils.isEmpty(taskId)) {//停止计时的广播
            if (lastEntity != null) {
                lastEntity.isTiming = false;
            }
            taskAdapter.notifyDataSetChanged();
        } else {//开始计时的广播
            TimeEntity.ItemEntity updateItem = TimerManager.getInstance().getTimer();
            if (updateItem != null) {
                updateChildTimeing(updateItem.taskPkId, true);
            }
        }

    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        TaskEntity.TaskItemEntity item = taskAdapter.getItem(i);
        if (item.type == 0)//说明是任务
            showLongMenu(item);
        return false;
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        TaskEntity.TaskItemEntity itemEntity = taskAdapter.getItem(i);
        switch (view.getId()) {
            case R.id.task_item_start_timming:
                if (!itemEntity.isTiming) {
                    MobclickAgent.onEvent(getContext(), UMMobClickAgent.stop_timer_click_id);
                    startTiming(itemEntity);
                } else {
                    MobclickAgent.onEvent(getContext(), UMMobClickAgent.start_timer_click_id);
                    stopTiming(itemEntity);
                }
                break;
            case R.id.task_item_checkbox:
                if (isEditTask) {
                    if (!itemEntity.state) {//完成任务
                        if (itemEntity.attendeeUsers != null) {
                            if (itemEntity.attendeeUsers.size() > 1) {
                                showFinishDialog(getActivity(), "该任务由多人负责,确定完成?", itemEntity, SHOW_FINISH_DIALOG);
                            } else {
                                updateTaskState(itemEntity, true);
                            }
                        } else {
                            updateTaskState(itemEntity, true);
                        }
                    } else {//取消完成任务
                        updateTaskState(itemEntity, false);
                    }
                } else {
                    showTopSnackBar("您没有编辑任务的权限");
                }
                break;
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        TaskEntity.TaskItemEntity item = taskAdapter.getItem(i);
        if (item.type == 0)//说明是任务
            TaskDetailActivity.launch(view.getContext(), item.id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
