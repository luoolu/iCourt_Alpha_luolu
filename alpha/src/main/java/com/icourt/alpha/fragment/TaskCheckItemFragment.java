package com.icourt.alpha.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.TaskCheckItemAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.entity.bean.TaskCheckItemEntity;
import com.icourt.alpha.entity.event.TaskActionEvent;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.interfaces.OnUpdateTaskListener;
import com.icourt.alpha.utils.ItemDecorationUtils;
import com.icourt.api.RequestUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Description 任务详情：检查项
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/12
 * version 2.0.0
 */

public class TaskCheckItemFragment extends BaseFragment implements BaseRecyclerAdapter.OnItemChildClickListener {
    private static final String KEY_TASK_ID = "key_task_id";
    private static final String KEY_HAS_PERMISSION = "key_has_permission";
    Unbinder unbinder;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;

    String taskId;
    TaskCheckItemAdapter taskCheckItemAdapter;
    @BindView(R.id.check_item_edit)
    EditText checkItemEdit;
    @BindView(R.id.check_item_add)
    ImageView checkItemAdd;
    OnUpdateTaskListener updateTaskListener;
    boolean hasPermission;
    @BindView(R.id.add_item_layout)
    LinearLayout addItemLayout;
    @BindView(R.id.empty_layout)
    LinearLayout emptyLayout;
    @BindView(R.id.list_layout)
    LinearLayout listLayout;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    public static TaskCheckItemFragment newInstance(@NonNull String taskId, boolean hasPermission) {
        TaskCheckItemFragment taskCheckItemFragment = new TaskCheckItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TASK_ID, taskId);
        bundle.putBoolean(KEY_HAS_PERMISSION, hasPermission);
        taskCheckItemFragment.setArguments(bundle);
        return taskCheckItemFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_task_check_item_layout, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            updateTaskListener = (OnUpdateTaskListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initView() {
        taskId = getArguments().getString(KEY_TASK_ID);
        hasPermission = getArguments().getBoolean(KEY_HAS_PERMISSION);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }
        });
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerview.setAdapter(taskCheckItemAdapter = new TaskCheckItemAdapter());
        recyclerview.addItemDecoration(ItemDecorationUtils.getCommFull05Divider(getContext(), true, R.color.alpha_divider_color));
        getData(false);
        if (hasPermission) {
            addItemLayout.setVisibility(View.VISIBLE);
            taskCheckItemAdapter.setOnItemChildClickListener(this);
            checkItemEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (!TextUtils.isEmpty(checkItemEdit.getText().toString()))
                            addCheckItem();
                        else
                            showTopSnackBar("请输入检查项名称");
                    }
                    return true;
                }
            });
        } else {
            addItemLayout.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.check_item_add})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.check_item_add:
                if (hasPermission) {
                    if (!TextUtils.isEmpty(checkItemEdit.getText().toString()))
                        addCheckItem();
                    else
                        showTopSnackBar("请输入检查项名称");
                }
                break;
        }
    }

    @Override
    protected void getData(boolean isRefresh) {
        showLoadingDialog(null);
        getApi().taskCheckItemQuery(taskId).enqueue(new SimpleCallBack<TaskCheckItemEntity>() {
            @Override
            public void onSuccess(Call<ResEntity<TaskCheckItemEntity>> call, Response<ResEntity<TaskCheckItemEntity>> response) {
                dismissLoadingDialog();
                if (response.body().result.items != null) {
                    taskCheckItemAdapter.bindData(false, response.body().result.items);
                    if (response.body().result.items.size() <= 0) {
                        if (!hasPermission) {
                            listLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            listLayout.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResEntity<TaskCheckItemEntity>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
            }
        });
    }

    private void updateCheckItem() {
        if (getParentFragment() instanceof OnFragmentCallBackListener) {
            updateTaskListener = (OnUpdateTaskListener) getParentFragment();
        }
        if (updateTaskListener != null) {
            if (taskCheckItemAdapter.getSelectedData() != null) {
                updateTaskListener.onUpdateCheckItem(taskCheckItemAdapter.getSelectedData().size() + "/" + taskCheckItemAdapter.getItemCount());
            } else {
                updateTaskListener.onUpdateCheckItem(0 + "/" + taskCheckItemAdapter.getItemCount());
            }
        }
    }

    /**
     * 添加检查项
     */
    private void addCheckItem() {
        final TaskCheckItemEntity.ItemEntity itemEntity = getCheckItem();
        getApi().taskCheckItemCreate(RequestUtils.createJsonBody(new Gson().toJson(itemEntity))).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                if (response.body() != null) {
                    if (response.body().result != null) {
                        String id = response.body().result.getAsString();
                        itemEntity.id = id;
                        taskCheckItemAdapter.addItem(itemEntity);
                        checkItemEdit.setText("");
                        EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
                        updateCheckItem();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                showTopSnackBar("添加检查项失败");
            }
        });
    }

    /**
     * 获取添加检查项的json
     *
     * @return
     */
    private TaskCheckItemEntity.ItemEntity getCheckItem() {

        TaskCheckItemEntity.ItemEntity itemEntity = new TaskCheckItemEntity.ItemEntity();
        itemEntity.state = false;
        itemEntity.taskId = taskId;
        itemEntity.name = checkItemEdit.getText().toString().trim();
        return itemEntity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onItemChildClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        TaskCheckItemEntity.ItemEntity itemEntity = (TaskCheckItemEntity.ItemEntity) adapter.getItem(position);
        showLoadingDialog(null);
        switch (view.getId()) {
            case R.id.check_item_checktext_tv:
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if (checkedTextView.isChecked()) {
                    itemEntity.state = false;
                } else {
                    itemEntity.state = true;
                }
                finisCheckItem(itemEntity, position);
                break;
            case R.id.check_item_delete_image:
                deleteCheckItem(itemEntity);
                break;
        }
    }

    /**
     * 修改检查项
     *
     * @param itemEntity
     */
    private void finisCheckItem(final TaskCheckItemEntity.ItemEntity itemEntity, final int position) {
        getApi().taskCheckItemUpdate(RequestUtils.createJsonBody(new Gson().toJson(itemEntity).toString())).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
                taskCheckItemAdapter.updateItem(itemEntity);
                if (itemEntity.state) {
                    taskCheckItemAdapter.getSelectedArray().put(position, true);
                } else {
                    taskCheckItemAdapter.getSelectedArray().delete(position);
                }
                updateCheckItem();
            }
        });
    }

    /**
     * 删除检查项
     *
     * @param itemEntity
     */
    private void deleteCheckItem(final TaskCheckItemEntity.ItemEntity itemEntity) {
        if (itemEntity == null) return;
        getApi().taskCheckItemDelete(itemEntity.id).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
                taskCheckItemAdapter.removeItem(itemEntity);
                updateCheckItem();
            }
        });
    }
}
