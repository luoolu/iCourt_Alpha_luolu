package com.icourt.alpha.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.asange.recyclerviewadapter.BaseViewHolder;
import com.asange.recyclerviewadapter.OnItemClickListener;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.RepoAdapter;
import com.icourt.alpha.constants.SFileConfig;
import com.icourt.alpha.entity.bean.RepoEntity;
import com.icourt.alpha.http.callback.SFileCallBack;
import com.icourt.alpha.http.callback.SimpleCallBack2;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.http.observer.BaseObserver;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.alpha.view.smartrefreshlayout.EmptyRecyclerView;
import com.icourt.alpha.widget.filter.ListFilter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

import static com.icourt.alpha.constants.SFileConfig.PERMISSION_R;
import static com.icourt.alpha.constants.SFileConfig.PERMISSION_RW;
import static com.icourt.alpha.constants.SFileConfig.REPO_LAWFIRM;
import static com.icourt.alpha.constants.SFileConfig.REPO_MINE;
import static com.icourt.alpha.constants.SFileConfig.REPO_PROJECT;
import static com.icourt.alpha.constants.SFileConfig.REPO_SHARED_ME;

/**
 * Description
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/8/19
 * version 2.1.0
 */
public class RepoSelectListFragment extends RepoBaseFragment implements OnItemClickListener {
    public static final String KEY_REPO_TYPE = "repoType";
    public static final String KEY_REPO_FILTER_ONLY_READ = "filter_only_read";

    @BindView(R.id.recyclerView)
    EmptyRecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    Unbinder unbinder;
    RepoAdapter repoAdapter;
    OnFragmentCallBackListener onFragmentCallBackListener;
    int repoType;
    boolean filterOnlyReadRepo;

    public static RepoSelectListFragment newInstance(@SFileConfig.REPO_TYPE int repoType,
                                                     boolean filterOnlyReadRepo) {
        RepoSelectListFragment fragment = new RepoSelectListFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_REPO_TYPE, repoType);
        args.putBoolean(KEY_REPO_FILTER_ONLY_READ, filterOnlyReadRepo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnFragmentCallBackListener) {
            onFragmentCallBackListener = (OnFragmentCallBackListener) getParentFragment();
        } else {
            try {
                onFragmentCallBackListener = (OnFragmentCallBackListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.layout_refresh_recyclerview, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initView() {
        repoType = SFileConfig.convert2RepoType(getArguments().getInt(KEY_REPO_TYPE));
        filterOnlyReadRepo = getArguments().getBoolean(KEY_REPO_FILTER_ONLY_READ);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(repoAdapter = new RepoAdapter(REPO_MINE) {

            @Override
            public void onBindHolder(BaseViewHolder holder, @Nullable RepoEntity repoEntity, int i) {
                super.onBindHolder(holder, repoEntity, i);
                ImageView document_expand_iv = holder.obtainView(R.id.document_expand_iv);
                ImageView document_detail_iv = holder.obtainView(R.id.document_detail_iv);
                if (document_detail_iv != null) {
                    document_expand_iv.setVisibility(View.GONE);
                }
                if (document_detail_iv != null) {
                    document_detail_iv.setVisibility(View.GONE);
                }
            }
        });
        repoAdapter.setOnItemClickListener(this);
        switch (repoType) {
            case REPO_MINE:
                recyclerView.setEmptyContent(R.string.repo_empty);
                break;
            case REPO_SHARED_ME:
                recyclerView.setEmptyContent(R.string.repo_share_empty);
                break;
            case REPO_LAWFIRM:
                recyclerView.setEmptyContent(R.string.repo_lawfirm_empty);
                break;
            case REPO_PROJECT:
                recyclerView.setEmptyContent(R.string.repo_empty);
                break;
        }
        if (filterOnlyReadRepo) {
            recyclerView.setEmptyContent(R.string.repo_type_no_writable_repo);
        }
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(com.scwang.smartrefresh.layout.api.RefreshLayout refreshlayout) {
                getData(true);
            }
        });
        refreshLayout.autoRefresh();
    }


    @Override
    protected void getData(final boolean isRefresh) {
        super.getData(isRefresh);
        getRepoList();
    }

    private void getRepoList() {
        switch (repoType) {
            case REPO_MINE: {
                getDocumentRoot(null, false);
            }
            break;
            case REPO_SHARED_ME: {
                //获取管理员账号
                callEnqueue(getApi().getOfficeAdmin(getLoginUserId()),
                        new SimpleCallBack2<String>() {
                            @Override
                            public void onSuccess(Call<String> call, Response<String> response) {
                                getDocumentRoot(response.body(), filterOnlyReadRepo);
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                super.onFailure(call, t);
                                stopRefresh();
                            }
                        });
            }
            break;
            case REPO_LAWFIRM: {
                getLawyerRepo();
            }
            break;
            case REPO_PROJECT: {
                //获取管理员账号
                callEnqueue(getApi().getOfficeAdmin(getLoginUserId()),
                        new SimpleCallBack2<String>() {
                            @Override
                            public void onSuccess(Call<String> call, Response<String> response) {
                                getDocumentRoot(response.body(), filterOnlyReadRepo);
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                super.onFailure(call, t);
                                stopRefresh();
                            }
                        });
            }
            break;
        }
    }


    /**
     * 带读写权限的资料库
     */
    private void getLawyerRepo() {
        getApi().getOfficeLibs(filterOnlyReadRepo ? PERMISSION_RW : PERMISSION_R)
                .enqueue(new SFileCallBack<ResEntity<List<RepoEntity>>>() {
                    @Override
                    public void onSuccess(Call<ResEntity<List<RepoEntity>>> call, Response<ResEntity<List<RepoEntity>>> response) {
                        stopRefresh();
                        if (response.body().result == null) {
                            response.body().result = new ArrayList<RepoEntity>();
                        }
                        repoAdapter.bindData(true, response.body().result);
                        recyclerView.enableEmptyView(response.body().result);
                    }

                    @Override
                    public void onFailure(Call<ResEntity<List<RepoEntity>>> call, Throwable t) {
                        super.onFailure(call, t);
                        stopRefresh();
                        recyclerView.enableEmptyView(null);
                    }
                });
    }

    /**
     * 获取资料库
     * 不分页获取所有
     *
     * @param officeAdminId      律所管理员id
     * @param filterOnlyReadRepo 是否过滤只读权限的repo
     */
    private void getDocumentRoot(@Nullable String officeAdminId, final boolean filterOnlyReadRepo) {
        Observable<List<RepoEntity>> listCall = null;
        final int pageSize = Integer.MAX_VALUE;
        switch (repoType) {
            case REPO_MINE:
                listCall = getSFileApi().documentRootQueryObservable(1, pageSize, null, null, null);
                break;
            case REPO_SHARED_ME:
                listCall = getSFileApi().documentRootQueryObservable(1, pageSize, officeAdminId, null, "shared");
                break;
            case REPO_LAWFIRM:
                listCall = getSFileApi().documentRootQueryObservable();
                break;
            case REPO_PROJECT:
                listCall = getSFileApi().documentRootQueryObservable(1, pageSize, null, officeAdminId, "shared");
                break;
        }
        if (listCall == null) return;
        listCall.flatMap(new Function<List<RepoEntity>, ObservableSource<List<RepoEntity>>>() {
            @Override
            public ObservableSource<List<RepoEntity>> apply(@NonNull List<RepoEntity> repoEntities) throws Exception {
                return saveEncryptedData(repoEntities);
            }
        }).compose(this.<List<RepoEntity>>bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<RepoEntity>>() {
                    @Override
                    public void onNext(@NonNull List<RepoEntity> repoEntities) {
                        stopRefresh();
                        if (repoEntities == null) {
                            repoEntities = new ArrayList<RepoEntity>();
                        }
                        if (filterOnlyReadRepo) {
                            filterOnlyReadPermissionRepo(repoEntities);
                        }
                        repoAdapter.bindData(true, repoEntities);
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        super.onError(throwable);
                        stopRefresh();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        stopRefresh();
                        repoAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 过滤只读权限的资料库
     *
     * @param datas
     */
    private void filterOnlyReadPermissionRepo(List<RepoEntity> datas) {
        ListFilter.filterItems(datas, new ListFilter.ObjectFilterListener<RepoEntity>() {
            @Override
            public boolean isFilter(@Nullable RepoEntity repoEntity) {
                return repoEntity != null
                        && TextUtils.equals(repoEntity.permission, PERMISSION_R);
            }
        });
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadmore();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    /**
     * 展示资料库解锁的对话框
     *
     * @param item
     */
    private void showDecryptDialog(final RepoEntity item) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.repo_encrypted)
                .setMessage(R.string.repo_encrypted_unsupport)
                .setPositiveButton(R.string.str_good, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
        //二期开发解密功能
       /* View dialogView = View.inflate(getContext(), R.layout.dialog_repo_decriypt, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        final ClearEditText repoPwdEt = dialogView.findViewById(R.id.repo_pwd_et);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_tv:
                        alertDialog.dismiss();
                        break;
                    case R.id.ok_tv: {
                        if (StringUtils.isEmpty(repoPwdEt.getText())) {
                            showToast("密码不能为空");
                            return;
                        }
                        decryptRepo(alertDialog, item, repoPwdEt.getText().toString());
                    }
                    break;
                }
            }
        };
        dialogView.findViewById(R.id.cancel_tv).setOnClickListener(onClickListener);
        dialogView.findViewById(R.id.ok_tv).setOnClickListener(onClickListener);
        alertDialog.show();*/
    }

    /**
     * 解密资料库
     *
     * @param alertDialog
     * @param item
     * @param pwd
     */
    private void decryptRepo(@Nullable final AlertDialog alertDialog, final RepoEntity item, String pwd) {
        if (item == null) return;
        showLoadingDialog(null);
        callEnqueue(
                getSFileApi().repoDecrypt(item.repo_id, pwd),
                new SFileCallBack<String>() {
                    @Override
                    public void onSuccess(Call<String> call, Response<String> response) {
                        dismissLoadingDialog();
                        if (alertDialog != null) {
                            alertDialog.dismiss();
                        }
                        if (StringUtils.equalsIgnoreCase("success", response.body(), false)) {
                            //1.更新适配器
                            item.decryptMillisecond = DateUtils.millis();
                            repoAdapter.updateItem(item);

                            //2.更新本地
                            updateEncryptedRecord(item.repo_id, item.encrypted, item.decryptMillisecond);
                            showTopSnackBar("解密成功");
                        } else {
                            bugSync("资料库解密返回失败", response.body());
                            showTopSnackBar("解密返回失败:" + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        dismissLoadingDialog();
                        if (t instanceof HttpException &&
                                ((HttpException) t).code() == 400) {
                            showToast("密码错误");
                        } else {
                            super.onFailure(call, t);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(com.asange.recyclerviewadapter.BaseRecyclerAdapter baseRecyclerAdapter, BaseViewHolder baseViewHolder, View view, int i) {
        RepoEntity item = repoAdapter.getItem(i);
        if (item == null) {
            return;
        }
        if (item.isNeedDecrypt()) {
            showDecryptDialog(item);
            return;
        }
        if (onFragmentCallBackListener != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_FRAGMENT_RESULT, item);
            bundle.putInt(KEY_REPO_TYPE, repoType);
            onFragmentCallBackListener.onFragmentCallBack(this, 1, bundle);
        }
    }
}
