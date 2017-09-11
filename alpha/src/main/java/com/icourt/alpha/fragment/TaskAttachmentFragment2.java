package com.icourt.alpha.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.TaskAttachmentAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.HeaderFooterAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.DataChangeAdapterObserver;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.entity.bean.TaskAttachmentEntity;
import com.icourt.alpha.entity.event.TaskActionEvent;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.http.observer.BaseObserver;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.interfaces.OnUpdateTaskListener;
import com.icourt.alpha.widget.dialog.BottomActionDialog;
import com.icourt.api.RequestUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Description  任务下的附件
 * 权限动态获取
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/9/11
 * version 2.1.0
 */
public class TaskAttachmentFragment2 extends BaseFragment
        implements BaseRecyclerAdapter.OnItemClickListener,
        BaseRecyclerAdapter.OnItemLongClickListener {
    private static final String KEY_TASK_ID = "key_task_id";
    private static final String KEY_TASK_LOOK_ATTACHMENT_PERMISSION = "key_task_look_attachment_permission";
    private static final String KEY_TASK_ADD_ATTACHMENT_PERMISSION = "key_task_add_attachment_permission";
    private static final String KEY_TASK_DELETE_ATTACHMENT_PERMISSION = "key_task_delete_attachment_permission";

    /**
     * hasLookAttachmentPermission>hasAddAttachmentPermission
     * hasLookAttachmentPermission>hasDeleteAttachmentPermission
     *
     * @param taskId
     * @param hasLookAttachmentPermission   浏览附件的权限
     * @param hasAddAttachmentPermission    添加附件的权限
     * @param hasDeleteAttachmentPermission 删除附件的权限
     * @return
     */
    public static TaskAttachmentFragment2 newInstance(@NonNull String taskId,
                                                      boolean hasLookAttachmentPermission,
                                                      boolean hasAddAttachmentPermission,
                                                      boolean hasDeleteAttachmentPermission) {
        TaskAttachmentFragment2 taskAttachmentFragment = new TaskAttachmentFragment2();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TASK_ID, taskId);
        bundle.putBoolean(KEY_TASK_LOOK_ATTACHMENT_PERMISSION, hasLookAttachmentPermission);
        bundle.putBoolean(KEY_TASK_ADD_ATTACHMENT_PERMISSION, hasAddAttachmentPermission);
        bundle.putBoolean(KEY_TASK_DELETE_ATTACHMENT_PERMISSION, hasDeleteAttachmentPermission);
        taskAttachmentFragment.setArguments(bundle);
        return taskAttachmentFragment;
    }

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    Unbinder unbinder;
    TaskAttachmentAdapter taskAttachmentAdapter;
    HeaderFooterAdapter<TaskAttachmentAdapter> headerFooterAdapter;
    boolean hasLookAttachmentPermission;
    boolean hasAddAttachmentPermission;
    boolean hasDeleteAttachmentPermission;
    String taskId;
    TextView footerNoticeView;
    View footerAddView;
    OnUpdateTaskListener updateTaskListener;
    GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int requestCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                List<String> paths = new ArrayList<>();
                for (int i = 0; i < resultList.size(); i++) {
                    PhotoInfo photoInfo = resultList.get(i);
                    if (photoInfo == null) continue;
                    if (!TextUtils.isEmpty(photoInfo.getPhotoPath())) {
                        paths.add(resultList.get(i).getPhotoPath());
                    }
                }
                uploadFiles(paths);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String s) {

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnFragmentCallBackListener) {
            updateTaskListener = (OnUpdateTaskListener) getParentFragment();
        } else {
            try {
                updateTaskListener = (OnUpdateTaskListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_task_attachment_layout2, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initView() {
        taskId = getArguments().getString(KEY_TASK_ID, "");
        hasLookAttachmentPermission = getArguments().getBoolean(KEY_TASK_LOOK_ATTACHMENT_PERMISSION);
        hasAddAttachmentPermission = getArguments().getBoolean(KEY_TASK_ADD_ATTACHMENT_PERMISSION);
        hasDeleteAttachmentPermission = getArguments().getBoolean(KEY_TASK_DELETE_ATTACHMENT_PERMISSION);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        headerFooterAdapter = new HeaderFooterAdapter<>(taskAttachmentAdapter = new TaskAttachmentAdapter());
        taskAttachmentAdapter.registerAdapterDataObserver(new DataChangeAdapterObserver() {
            @Override
            protected void updateUI() {
                if (updateTaskListener != null) {
                    updateTaskListener.onUpdateDocument(String.valueOf(taskAttachmentAdapter.getItemCount()));
                }
            }
        });
        taskAttachmentAdapter.setOnItemClickListener(this);
        taskAttachmentAdapter.setOnItemLongClickListener(this);
        footerAddView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.footer_add_attachment, recyclerView);
        TextView attachmentTv = footerAddView.findViewById(R.id.add_attachment_view);
        if (attachmentTv != null) {
            attachmentTv.setText("添加附件");
        }
        registerClick(attachmentTv);
        headerFooterAdapter.addFooter(footerAddView);

        footerNoticeView = (TextView) HeaderFooterAdapter.inflaterView(getContext(), R.layout.footer_folder_document_num, recyclerView);
        footerNoticeView.setText("");
        headerFooterAdapter.addFooter(footerNoticeView);


        recyclerView.setAdapter(headerFooterAdapter);
        //有浏览权限 再调数据获取接口
        if (hasLookAttachmentPermission) {
            getData(true);
        } else {
            footerAddView.setVisibility(View.GONE);
            footerNoticeView.setVisibility(View.VISIBLE);
            footerNoticeView.setText("暂无权限查看");
        }
    }

    @Override
    protected void getData(boolean isRefresh) {
        super.getData(isRefresh);
        callEnqueue(getApi().taskAttachMentListQuery(taskId),
                new SimpleCallBack<List<TaskAttachmentEntity>>() {
                    @Override
                    public void onSuccess(Call<ResEntity<List<TaskAttachmentEntity>>> call, Response<ResEntity<List<TaskAttachmentEntity>>> response) {
                        taskAttachmentAdapter.bindData(true, response.body().result);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_attachment_view:
                showBottomMenu();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 显示底部添加菜单
     */
    private void showBottomMenu() {
        new BottomActionDialog(getContext(),
                null,
                Arrays.asList("拍照", "从手机相册选择"),
                new BottomActionDialog.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(BottomActionDialog dialog, BottomActionDialog.ActionItemAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                checkAndSelectFromCamera(mOnHanlderResultCallback);
                                break;
                            case 1:
                                checkAndSelectMutiPhotos(mOnHanlderResultCallback);
                                break;
                        }
                    }
                }).show();
    }

    /**
     * 批量上传文件
     *
     * @param filePaths 文件路径
     */
    private void uploadFiles(@NonNull final List<String> filePaths) {
        Observable.just(filePaths)
                .filter(new Predicate<List<String>>() {
                    @Override
                    public boolean test(@io.reactivex.annotations.NonNull List<String> strings) throws Exception {
                        return !strings.isEmpty();
                    }
                })
                .flatMap(new Function<List<String>, ObservableSource<JsonElement>>() {

                    @Override
                    public ObservableSource<JsonElement> apply(@io.reactivex.annotations.NonNull List<String> strings) throws Exception {
                        List<Observable<JsonElement>> observables = new ArrayList<Observable<JsonElement>>();
                        for (int i = 0; i < strings.size(); i++) {
                            String filePath = strings.get(i);
                            if (TextUtils.isEmpty(filePath)) {
                                continue;
                            }
                            File file = new File(filePath);
                            if (!file.exists()) {
                                continue;
                            }
                            Map<String, RequestBody> params = new HashMap<>();
                            params.put(RequestUtils.createStreamKey(file), RequestUtils.createImgBody(file));
                            observables.add(getApi().taskAttachmentUploadObservable(taskId, params));
                        }
                        return Observable.concat(observables);
                    }
                })
                .compose(this.<JsonElement>bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<JsonElement>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable disposable) {
                        super.onSubscribe(disposable);
                        showLoadingDialog(R.string.str_uploading);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JsonElement jsonElement) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable throwable) {
                        super.onError(throwable);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissLoadingDialog();
                        getData(true);
                        broadTaskUpdate();
                    }
                });
    }

    /**
     * 是否需要?
     */
    protected void broadTaskUpdate() {
        EventBus.getDefault().post(new TaskActionEvent(TaskActionEvent.TASK_REFRESG_ACTION));
    }

    @Override
    public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {

    }

    @Override
    public boolean onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        showDeleteConfirmDialog(taskAttachmentAdapter.getItem(position));
        return true;
    }

    /**
     * 删除菜单
     *
     * @param entity
     */
    private void showDeleteConfirmDialog(final TaskAttachmentEntity entity) {
        if (entity == null) return;
        if (entity.pathInfoVo == null) return;
        new BottomActionDialog(getContext(),
                null,
                Arrays.asList(getString(R.string.str_delete)),
                new BottomActionDialog.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(BottomActionDialog dialog, BottomActionDialog.ActionItemAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                deleteAttachment(entity);
                                break;
                        }
                    }
                }).show();
    }

    /**
     * 删除附件
     *
     * @param entity
     */
    private void deleteAttachment(final TaskAttachmentEntity entity) {
        if (entity == null) return;
        if (entity.pathInfoVo == null) return;
        showLoadingDialog(R.string.str_deleting);
        callEnqueue(getApi().taskDocumentDelete(taskId, entity.pathInfoVo.filePath),
                new SimpleCallBack<JsonElement>() {
                    @Override
                    public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                        dismissLoadingDialog();
                        if (taskAttachmentAdapter != null) {
                            taskAttachmentAdapter.removeItem(entity);
                            broadTaskUpdate();
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
