package com.icourt.alpha.fragment;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonParseException;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.IMContactAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.HeaderFooterAdapter;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.db.convertor.IConvertModel;
import com.icourt.alpha.db.convertor.ListConvertor;
import com.icourt.alpha.db.dbmodel.ContactDbModel;
import com.icourt.alpha.db.dbservice.ContactDbService;
import com.icourt.alpha.entity.bean.AlphaUserInfo;
import com.icourt.alpha.entity.bean.GroupContactBean;
import com.icourt.alpha.entity.bean.IMMessageCustomBody;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.interfaces.OnPageFragmentCallBack;
import com.icourt.alpha.utils.FileUtils;
import com.icourt.alpha.utils.JsonUtils;
import com.icourt.alpha.utils.StringUtils;
import com.icourt.alpha.utils.SystemUtils;
import com.icourt.alpha.utils.UriUtils;
import com.icourt.alpha.view.ClearEditText;
import com.icourt.alpha.widget.filter.ListFilter;
import com.icourt.api.RequestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import io.realm.RealmResults;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


/**
 * Description
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/5/20
 * version 1.0.0
 */
public class FileImportContactFragment extends BaseFragment implements BaseRecyclerAdapter.OnItemClickListener {
    private static final String KEY_PATH = "path";
    public static final int TYPE_UPLOAD = 1001;
    private static final String KEY_IS_FILTER_MYSELF = "isFilterMyself";
    private static final String KEY_DESC = "desc";
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    Unbinder unbinder;
    IMContactAdapter imContactAdapter;
    HeaderFooterAdapter<IMContactAdapter> headerFooterAdapter;
    @BindView(R.id.header_comm_search_input_et)
    ClearEditText headerCommSearchInputEt;
    @BindView(R.id.header_comm_search_cancel_tv)
    TextView headerCommSearchCancelTv;
    @BindView(R.id.header_comm_search_input_ll)
    LinearLayout headerCommSearchInputLl;

    public static FileImportContactFragment newInstance(String path, String desc, boolean isFilterMyself) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PATH, path);
        bundle.putString(KEY_DESC, desc);
        bundle.putBoolean(KEY_IS_FILTER_MYSELF, isFilterMyself);
        FileImportContactFragment importFilePathFragment = new FileImportContactFragment();
        importFilePathFragment.setArguments(bundle);
        return importFilePathFragment;
    }

    OnPageFragmentCallBack onPageFragmentCallBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onPageFragmentCallBack = (OnPageFragmentCallBack) context;
        } catch (ClassCastException e) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_file_import_contact, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        headerFooterAdapter = new HeaderFooterAdapter<>(imContactAdapter = new IMContactAdapter());
        imContactAdapter.setSelectable(true);
        View headerView = HeaderFooterAdapter.inflaterView(getContext(), R.layout.header_contact_share_search, recyclerView);
        headerFooterAdapter.addHeader(headerView);
        registerClick(headerView.findViewById(R.id.header_comm_search_ll));
        registerClick(headerView.findViewById(R.id.header_group_item_ll));
        headerCommSearchInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    getData(true);
                } else {
                    serachGroupMember(s.toString());
                }
            }
        });
        recyclerView.setAdapter(headerFooterAdapter);
        imContactAdapter.setOnItemClickListener(this);
        getData(true);
        headerCommSearchInputLl.setVisibility(View.GONE);
    }

    /**
     * 过滤调自己
     *
     * @param data
     * @return
     */
    private List<GroupContactBean> filterMySelf(List<GroupContactBean> data) {
        GroupContactBean groupContactBean = new GroupContactBean();
        groupContactBean.accid = StringUtils.toLowerCase(getLoginUserId());
        new ListFilter<GroupContactBean>().filter(data, groupContactBean);
        return data;
    }

    @Override
    protected void getData(boolean isRefresh) {
        super.getData(isRefresh);
        Observable.create(new ObservableOnSubscribe<List<GroupContactBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GroupContactBean>> e) throws Exception {
                if (e.isDisposed()) return;
                ContactDbService contactDbService = new ContactDbService(getLoginUserId());
                RealmResults<ContactDbModel> contactDbModels = contactDbService.queryAll();
                if (contactDbModels != null) {
                    List<GroupContactBean> contactBeen = ListConvertor.convertList(new ArrayList<IConvertModel<GroupContactBean>>(contactDbModels));
                    filterRobot(contactBeen);
                    e.onNext(contactBeen);
                }
                e.onComplete();
            }
        }).compose(this.<List<GroupContactBean>>bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GroupContactBean>>() {
                    @Override
                    public void accept(List<GroupContactBean> groupContactBeen) throws Exception {
                        if (groupContactBeen != null && isFilterMyself()) {
                            filterMySelf(groupContactBeen);
                        }
                        imContactAdapter.bindData(true, groupContactBeen);
                    }
                });
    }

    private boolean isFilterMyself() {
        return getArguments() != null
                && getArguments().getBoolean(KEY_IS_FILTER_MYSELF);
    }

    @Override
    public void notifyFragmentUpdate(Fragment targetFrgament, int type, Bundle bundle) {
        super.notifyFragmentUpdate(targetFrgament, type, bundle);
        if (targetFrgament == this) {
            if (type == TYPE_UPLOAD) {
                String path = getPath();
                if (!TextUtils.isEmpty(path) && path.startsWith("http")) {
                    shareUrl2Contact(path, getArguments().getString(KEY_DESC, ""));
                } else {
                    shareFile2Contact(path);
                }
            }
        }
    }


    private String getPath() {
        return getArguments().getString(KEY_PATH);
    }

    private void shareUrl2Contact(String path, String desc) {
        if (TextUtils.isEmpty(path)) return;
        final ArrayList<GroupContactBean> selectedData = imContactAdapter.getSelectedData();
        if (selectedData.isEmpty()) return;
        AlphaUserInfo loginUserInfo = getLoginUserInfo();
        String uid = StringUtils.toLowerCase(loginUserInfo != null ? loginUserInfo.getUserId() : "");
        if (selectedData.size() > 0) {
            showLoadingDialog(null);
            for (int i = 0; i < selectedData.size(); i++) {
                final IMMessageCustomBody msgPostEntity = IMMessageCustomBody.createLinkMsg(Const.CHAT_TYPE_P2P,
                        loginUserInfo == null ? "" : loginUserInfo.getName(),
                        uid,
                        selectedData.get(i).accid,
                        path,
                        desc,
                        null,
                        null);
                String jsonBody = null;
                try {
                    jsonBody = JsonUtils.Gson2String(msgPostEntity);
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }

                final int finalI = i;
                getChatApi().msgAdd(RequestUtils.createJsonBody(jsonBody))
                        .enqueue(new SimpleCallBack<IMMessageCustomBody>() {
                            @Override
                            public void onSuccess(Call<ResEntity<IMMessageCustomBody>> call, Response<ResEntity<IMMessageCustomBody>> response) {
                                if (finalI == selectedData.size() - 1) {
                                    dismissLoadingDialog();
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResEntity<IMMessageCustomBody>> call, Throwable t) {
                                super.onFailure(call, t);
                                if (finalI == selectedData.size() - 1) {
                                    dismissLoadingDialog();
                                }
                            }
                        });
            }
        }
    }

    /**
     * 分享文件到享聊
     */

    private void shareFile2Contact(String path) {
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            reqPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "我们需要文件读写权限!", 1001);
            return;
        }
        if (TextUtils.isEmpty(path)) return;
        //content://com.icourt.alpha.provider/external_storage_root/alpha_download/dshgghsdhg.pdf
        ParcelFileDescriptor n_fileDescriptor = null;
        File file = null;
        Uri fileUri = null;
        if (path.startsWith("content://")) {
            try {
                fileUri = Uri.parse(path);
                n_fileDescriptor = UriUtils.get_N_FileDescriptor(getContext(), fileUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n_fileDescriptor == null) {
                showTopSnackBar("共享文件不存在啦");
                return;
            }
        } else {
            file = new File(path);
            if (!file.exists()) {
                showTopSnackBar("文件不存在啦");
                return;
            }
        }
        final ArrayList<GroupContactBean> selectedData = imContactAdapter.getSelectedData();
        if (selectedData.isEmpty()) return;
        AlphaUserInfo loginUserInfo = getLoginUserInfo();
        String uid = StringUtils.toLowerCase(loginUserInfo.getUserId());
        if (selectedData.size() > 0) {
            showLoadingDialog(null);
            for (int i = 0; i < selectedData.size(); i++) {
                final IMMessageCustomBody msgPostEntity = IMMessageCustomBody.createFileMsg(
                        Const.CHAT_TYPE_P2P,
                        loginUserInfo == null ? "" : loginUserInfo.getName(),
                        uid,
                        selectedData.get(i).accid,
                        path);
                Map<String, RequestBody> params = new HashMap<>();
                params.put("platform", RequestUtils.createTextBody(msgPostEntity.platform));
                params.put("to", RequestUtils.createTextBody(msgPostEntity.to));
                params.put("from", RequestUtils.createTextBody(msgPostEntity.from));
                params.put("ope", RequestUtils.createTextBody(String.valueOf(msgPostEntity.ope)));
                params.put("name", RequestUtils.createTextBody(msgPostEntity.name));
                params.put("magic_id", RequestUtils.createTextBody(msgPostEntity.magic_id));
                if (file != null) {
                    params.put(RequestUtils.createStreamKey(file), RequestUtils.createStreamBody(file));
                } else {
                    String lastPathSegment = fileUri.getLastPathSegment();
                    if (TextUtils.isEmpty(lastPathSegment)) {
                        lastPathSegment = UUID.randomUUID().toString();
                    }
                    byte[] bytes = FileUtils.fileDescriptor2Byte(n_fileDescriptor);
                    if (n_fileDescriptor != null) {
                        try {
                            n_fileDescriptor.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    params.put(RequestUtils.createStreamKey(lastPathSegment), RequestUtils.createStreamBody(bytes));
                }

                final int finalI = i;
                getChatApi().msgImageAdd(params)
                        .enqueue(new SimpleCallBack<IMMessageCustomBody>() {
                            @Override
                            public void onSuccess(Call<ResEntity<IMMessageCustomBody>> call, Response<ResEntity<IMMessageCustomBody>> response) {
                                if (finalI == selectedData.size() - 1) {
                                    dismissLoadingDialog();
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResEntity<IMMessageCustomBody>> call, Throwable t) {
                                super.onFailure(call, t);
                                if (finalI == selectedData.size() - 1) {
                                    dismissLoadingDialog();
                                }
                            }
                        });
            }
        }
    }

    /**
     * 过滤掉 机器人（robot == 100）
     *
     * @param data
     * @return
     */
    private List<GroupContactBean> filterRobot(List<GroupContactBean> data) {
        return new ListFilter<GroupContactBean>().filter(data, GroupContactBean.TYPE_ROBOT);
    }

    /**
     * 搜索用户
     *
     * @param name
     */
    private void serachGroupMember(String name) {
        try {
            ContactDbService contactDbService = new ContactDbService(getLoginUserId());
            RealmResults<ContactDbModel> result = contactDbService.contains("name", name);
            if (result == null) {
                imContactAdapter.clearData();
                return;
            }
            List<GroupContactBean> contactBeen = ListConvertor.convertList(new ArrayList<IConvertModel<GroupContactBean>>(result));
            filterRobot(contactBeen);
            if (isFilterMyself()) {
                filterMySelf(contactBeen);
            }
            imContactAdapter.clearSelected();
            imContactAdapter.bindData(true, contactBeen);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.header_comm_search_cancel_tv})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_comm_search_ll:
                headerCommSearchInputLl.setVisibility(View.VISIBLE);
                SystemUtils.showSoftKeyBoard(getActivity(), headerCommSearchInputEt);
                break;
            case R.id.header_comm_search_cancel_tv:
                headerCommSearchInputEt.setText("");
                SystemUtils.hideSoftKeyBoard(getActivity(), headerCommSearchInputEt, true);
                headerCommSearchInputLl.setVisibility(View.GONE);
                break;
            case R.id.header_group_item_ll:
                if (onPageFragmentCallBack != null) {
                    onPageFragmentCallBack.onRequest2NextPage(this, 0, null);
                }
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

    @Override
    public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        imContactAdapter.toggleSelected(position);
    }
}
