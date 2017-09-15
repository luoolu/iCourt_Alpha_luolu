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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.icourt.alpha.R;
import com.icourt.alpha.activity.CustomerCompanyDetailActivity;
import com.icourt.alpha.activity.CustomerPersonDetailActivity;
import com.icourt.alpha.activity.ProjectBasicTextInfoActivity;
import com.icourt.alpha.activity.ProjectJudgeActivity;
import com.icourt.alpha.activity.ProjectMembersActivity;
import com.icourt.alpha.adapter.ProjectBasicInfoAdapter;
import com.icourt.alpha.adapter.ProjectClientAdapter;
import com.icourt.alpha.adapter.ProjectMembersAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseFragmentAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.RefreshViewEmptyObserver;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.db.dbmodel.CustomerDbModel;
import com.icourt.alpha.db.dbservice.CustomerDbService;
import com.icourt.alpha.entity.bean.CustomerEntity;
import com.icourt.alpha.entity.bean.ProjectBasicItemEntity;
import com.icourt.alpha.entity.bean.ProjectDetailEntity;
import com.icourt.alpha.entity.bean.ProjectProcessesEntity;
import com.icourt.alpha.entity.event.ProjectActionEvent;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.ItemDecorationUtils;
import com.icourt.alpha.utils.LoginInfoUtils;
import com.icourt.alpha.utils.SpUtils;
import com.icourt.alpha.utils.UMMobClickAgent;
import com.icourt.alpha.view.WrapContentHeightViewPager;
import com.icourt.alpha.view.xrefreshlayout.RefreshLayout;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Response;

import static com.icourt.alpha.activity.MainActivity.KEY_CUSTOMER_PERMISSION;
import static com.icourt.alpha.activity.ProjectDetailActivity.KEY_PROJECT_ID;
import static com.icourt.alpha.activity.ProjectDetailActivity.KEY_PROJECT_MYSTAR;
import static com.icourt.alpha.activity.ProjectDetailActivity.KEY_PROJECT_PROCESSES;

/**
 * Description 项目概览
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/3
 * version 2.0.0
 */

public class ProjectDetailFragment extends BaseFragment implements BaseRecyclerAdapter.OnItemClickListener {

    Unbinder unbinder;
    @BindView(R.id.project_member_recyclerview)
    RecyclerView projectMemberRecyclerview;
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;
    @BindView(R.id.project_member_count)
    TextView projectMemberCount;
    @BindView(R.id.project_add_routine)
    ImageView projectAddRoutine;
    @BindView(R.id.project_viewpager)
    WrapContentHeightViewPager projectViewpager;
    @BindView(R.id.basic_top_recyclerview)
    RecyclerView basicTopRecyclerview;
    @BindView(R.id.project_member_layout)
    LinearLayout projectMemberLayout;
    @BindView(R.id.procedure_layout)
    LinearLayout procedureLayout;

    private String projectId;
    ProjectMembersAdapter projectMemberAdapter;
    ProjectBasicInfoAdapter projectBasicInfoAdapter;
    BaseFragmentAdapter baseFragmentAdapter;
    OnFragmentCallBackListener onFragmentCallBackListener;
    ProjectDetailEntity projectDetailBean;
    private CustomerDbService customerDbService = null;
    ProjectRangeFragment projectRangeFragment;

    public static ProjectDetailFragment newInstance(@NonNull String projectId) {
        ProjectDetailFragment projectDetailFragment = new ProjectDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PROJECT_ID, projectId);
        projectDetailFragment.setArguments(bundle);
        return projectDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_project_detail_layout, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFragmentCallBackListener = (OnFragmentCallBackListener) context;
    }

    @Override
    protected void initView() {
        projectId = getArguments().getString(KEY_PROJECT_ID);
        customerDbService = new CustomerDbService(LoginInfoUtils.getLoginUserId());
        refreshLayout.setMoveForHorizontal(true);

        basicTopRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        basicTopRecyclerview.addItemDecoration(ItemDecorationUtils.getCommMagin5Divider(getContext(), false));
        basicTopRecyclerview.setAdapter(projectBasicInfoAdapter = new ProjectBasicInfoAdapter());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        projectMemberRecyclerview.setLayoutManager(layoutManager);
        projectMemberRecyclerview.addItemDecoration(ItemDecorationUtils.getCommFull5VerticalDivider(getContext(), true));
        projectMemberRecyclerview.setHasFixedSize(true);
        projectMemberRecyclerview.setAdapter(projectMemberAdapter = new ProjectMembersAdapter());
        projectMemberAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, projectMemberAdapter));
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
        refreshLayout.startRefresh();
    }

    /**
     * 设置数据
     *
     * @param projectDetailBean
     */
    private void setDataToView(ProjectDetailEntity projectDetailBean) {
        if (projectMemberLayout == null) return;
        if (projectDetailBean != null) {
            this.projectDetailBean = projectDetailBean;
            EventBus.getDefault().post(new ProjectActionEvent(ProjectActionEvent.PROJECT_TIMER_ACTION, projectDetailBean.sumTime));
            if (onFragmentCallBackListener != null) {
                Bundle bundle = new Bundle();
                if (TextUtils.isEmpty(projectDetailBean.myStar)) {
                    bundle.putInt(KEY_PROJECT_MYSTAR, 0);
                } else {
                    bundle.putInt(KEY_PROJECT_MYSTAR, Integer.valueOf(projectDetailBean.myStar));
                }
                onFragmentCallBackListener.onFragmentCallBack(this, 0, bundle);
            }

            List<ProjectBasicItemEntity> basicItemEntities = new ArrayList<>();
            setKeyValueData(basicItemEntities, "项目类型", projectDetailBean.matterTypeName, Const.PROJECT_TYPE_TYPE);
            setKeyValueData(basicItemEntities, "项目名称", projectDetailBean.name, Const.PROJECT_NAME_TYPE);
            setKeyValueData(basicItemEntities, "项目编号", projectDetailBean.matterNo, Const.PROJECT_NUMBER_TYPE);
            setClientData(basicItemEntities);//客户
            if (projectDetailBean.matterType != Const.PROJECT_TRANSACTION_TYPE) { //所内事务不显示当事人item
                setLitigantData(basicItemEntities);//当事人
            }
            setGroupsData(basicItemEntities);//负责部门
            setAttorneysData(basicItemEntities);//案源律师
            setKeyValueData(basicItemEntities, "项目备注", projectDetailBean.remark, Const.PROJECT_REMARK_TYPE);

            if (projectDetailBean.beginDate > 0 && projectDetailBean.endDate > 0) {
                setKeyValueData(basicItemEntities, "项目时间", String.format("%s - %s",
                        DateUtils.getTimeDateFormatYearDot(projectDetailBean.beginDate),
                        DateUtils.getTimeDateFormatYearDot(projectDetailBean.endDate)), Const.PROJECT_TIME_TYPE);
            }

            projectBasicInfoAdapter.setClientsBeens(projectDetailBean.clients);
            projectBasicInfoAdapter.bindData(true, basicItemEntities);
            projectBasicInfoAdapter.setOnItemClickListener(this);

            if (projectDetailBean.members != null) {//项目成员
                if (projectDetailBean.members.size() > 0) {
                    projectMemberLayout.setVisibility(View.VISIBLE);
                    projectMemberCount.setText(String.format("项目成员（%s)", projectDetailBean.members.size()));
                    projectMemberAdapter.bindData(true, projectDetailBean.members);
                }
            } else {
                projectMemberLayout.setVisibility(View.GONE);
            }
            projectMemberAdapter.setOnItemClickListener(this);
            if (projectDetailBean.matterType == 0) {//争议解决
                getRangeData(projectDetailBean.pkId);
            } else {
                procedureLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置单一的数据类型：value为string
     *
     * @param basicItemEntities
     * @param key
     * @param value
     * @param type
     */
    private void setKeyValueData(List<ProjectBasicItemEntity> basicItemEntities, String key, String value, int type) {
        if (TextUtils.isEmpty(value)) return;
        ProjectBasicItemEntity itemEntity = new ProjectBasicItemEntity();
        itemEntity.key = key;
        itemEntity.value = value;
        itemEntity.type = type;
        basicItemEntities.add(itemEntity);
    }

    /**
     * 设置客户信息
     *
     * @param basicItemEntities
     */
    private void setClientData(List<ProjectBasicItemEntity> basicItemEntities) {
        if (projectDetailBean.clients == null || projectDetailBean.clients.size() <= 0) return;
        ProjectBasicItemEntity itemEntity = new ProjectBasicItemEntity();
        if (projectDetailBean.clients.size() > 1) {
            itemEntity.key = String.format("客户 (%s)", projectDetailBean.clients.size());
        } else {
            itemEntity.key = "客户";
        }
        StringBuffer buffer = new StringBuffer();
        for (ProjectDetailEntity.ClientsBean client : projectDetailBean.clients) {
            buffer.append(client.contactName).append("、");
        }
        itemEntity.value = buffer.toString();
        if (itemEntity.value.length() > 0) {
            itemEntity.value = itemEntity.value.substring(0, itemEntity.value.length() - 1);
        }
        itemEntity.type = Const.PROJECT_CLIENT_TYPE;
        basicItemEntities.add(itemEntity);
    }

    /**
     * 设置当事人信息
     *
     * @param basicItemEntities
     */
    private void setLitigantData(List<ProjectBasicItemEntity> basicItemEntities) {
        if (projectDetailBean.litigants == null || projectDetailBean.litigants.size() <= 0) return;
        ProjectBasicItemEntity itemEntity = new ProjectBasicItemEntity();
        if (projectDetailBean.litigants.size() > 1) {
            itemEntity.key = String.format("当事人 (%s)", projectDetailBean.litigants.size());
        } else {
            itemEntity.key = "当事人";
        }
        StringBuffer buffer = new StringBuffer();
        for (ProjectDetailEntity.LitigantsBean litigant : projectDetailBean.litigants) {
            buffer.append(litigant.contactName).append(",");
        }
        itemEntity.value = buffer.toString();
        if (itemEntity.value.length() > 0) {
            itemEntity.value = itemEntity.value.substring(0, itemEntity.value.length() - 1);
        }
        itemEntity.type = Const.PROJECT_PERSON_TYPE;
        basicItemEntities.add(itemEntity);
    }

    /**
     * 设置部门信息
     *
     * @param basicItemEntities
     */
    private void setGroupsData(List<ProjectBasicItemEntity> basicItemEntities) {
        if (projectDetailBean.groups == null || projectDetailBean.groups.size() <= 0) return;
        ProjectBasicItemEntity itemEntity = new ProjectBasicItemEntity();
        if (projectDetailBean.groups.size() > 1) {
            itemEntity.key = String.format("负责部门 (%s)", projectDetailBean.groups.size());
        } else {
            itemEntity.key = "负责部门";
        }
        StringBuffer buffer = new StringBuffer();
        for (ProjectDetailEntity.GroupsBean group : projectDetailBean.groups) {
            buffer.append(group.name).append("、");
        }
        itemEntity.value = buffer.toString();
        if (itemEntity.value.length() > 0) {
            itemEntity.value = itemEntity.value.substring(0, itemEntity.value.length() - 1);
        }
        itemEntity.type = Const.PROJECT_DEPARTMENT_TYPE;
        basicItemEntities.add(itemEntity);

    }

    /**
     * 设置案源律师数据
     *
     * @param basicItemEntities
     */
    private void setAttorneysData(List<ProjectBasicItemEntity> basicItemEntities) {
        if (projectDetailBean.attorneys == null || projectDetailBean.attorneys.size() <= 0) return;
        ProjectBasicItemEntity itemEntity = new ProjectBasicItemEntity();
        if (projectDetailBean.attorneys.size() > 1) {
            itemEntity.key = String.format("案源律师 (%s)", projectDetailBean.attorneys.size());
        } else {
            itemEntity.key = "案源律师";
        }
        StringBuffer buffer = new StringBuffer();
        for (ProjectDetailEntity.AttorneysBean attorneysBean : projectDetailBean.attorneys) {
            buffer.append(attorneysBean.attorneyName).append("、");
        }
        itemEntity.value = buffer.toString();
        if (itemEntity.value.length() > 0) {
            itemEntity.value = itemEntity.value.substring(0, itemEntity.value.length() - 1);
        }
        itemEntity.type = Const.PROJECT_ANYUAN_LAWYER_TYPE;
        basicItemEntities.add(itemEntity);

    }

    @Override
    protected void getData(boolean isRefresh) {
        getApi().projectDetail(projectId).enqueue(new SimpleCallBack<List<ProjectDetailEntity>>() {
            @Override
            public void onSuccess(Call<ResEntity<List<ProjectDetailEntity>>> call, Response<ResEntity<List<ProjectDetailEntity>>> response) {
                stopRefresh();
                if (response.body().result != null) {
                    if (response.body().result.size() > 0) {
                        setDataToView(response.body().result.get(0));
                    }
                }
            }
        });
    }

    /**
     * 获取程序信息
     *
     * @param projectId
     */
    private void getRangeData(String projectId) {
        getApi().projectProcessesQuery(projectId).enqueue(new SimpleCallBack<List<ProjectProcessesEntity>>() {
            public void onSuccess(Call<ResEntity<List<ProjectProcessesEntity>>> call, Response<ResEntity<List<ProjectProcessesEntity>>> response) {
                if (procedureLayout != null) {
                    if (response.body().result != null && response.body().result.size() > 0) {
                        procedureLayout.setVisibility(View.VISIBLE);
                        setRangeDataToView(response.body().result.get(0));
                    } else {
                        procedureLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * 设置程序信息
     *
     * @param projectProcessesEntity
     */
    private void setRangeDataToView(ProjectProcessesEntity projectProcessesEntity) {
        if (baseFragmentAdapter == null) {
            baseFragmentAdapter = new BaseFragmentAdapter(getChildFragmentManager());
            projectViewpager.setAdapter(baseFragmentAdapter);
            baseFragmentAdapter.bindData(true,
                    Arrays.asList(
                            projectRangeFragment == null ? projectRangeFragment = ProjectRangeFragment.newInstance(projectProcessesEntity) : projectRangeFragment)
            );
        } else {
            if (projectRangeFragment != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_PROJECT_PROCESSES, projectProcessesEntity);
                projectRangeFragment.notifyFragmentUpdate(projectRangeFragment, 0, bundle);
            }
        }
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.stopRefresh();
            refreshLayout.stopLoadMore();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
    }

    @OnClick({R.id.project_add_routine,
            R.id.project_member_layout,
            R.id.project_member_recyclerview,
            R.id.project_member_childlayout})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.project_add_routine:
                showTopSnackBar("添加程序信息");
                break;
            case R.id.project_member_layout:
            case R.id.project_member_childlayout:
                if (projectDetailBean != null)
                    ProjectMembersActivity.launch(getContext(), projectDetailBean.members, Const.PROJECT_MEMBER_TYPE);
                break;
        }
    }

    @Override
    public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.ViewHolder holder, View view, int position) {
        if (projectDetailBean == null) return;
        if (adapter instanceof ProjectMembersAdapter) {
            ProjectMembersActivity.launch(getContext(), projectDetailBean.members, Const.PROJECT_MEMBER_TYPE);
        } else if (adapter instanceof ProjectBasicInfoAdapter) {
            ProjectBasicItemEntity entity = (ProjectBasicItemEntity) adapter.getItem(position);
            switch (entity.type) {
                case Const.PROJECT_NAME_TYPE:
                case Const.PROJECT_REMARK_TYPE:
                case Const.PROJECT_NUMBER_TYPE:
                    ProjectBasicTextInfoActivity.launch(view.getContext(), entity.key, entity.value, entity.type);
                    break;
                case Const.PROJECT_ANYUAN_LAWYER_TYPE://案源律师
                    ProjectMembersActivity.launch(view.getContext(), projectDetailBean.attorneys, Const.PROJECT_ANYUAN_LAWYER_TYPE);
                    break;
                case Const.PROJECT_DEPARTMENT_TYPE://负责部门
                    ProjectJudgeActivity.launch(getContext(), entity.key, projectDetailBean.groups, entity.type);
                    break;
                case Const.PROJECT_PERSON_TYPE://当事人
                    ProjectJudgeActivity.launch(getContext(), entity.key, projectDetailBean.litigants, entity.type);
                    break;
            }
        } else if (adapter instanceof ProjectClientAdapter) {
            if (!hasCustomerPermission()) return;
            if (customerDbService == null) return;
            ProjectDetailEntity.ClientsBean clientsBean = (ProjectDetailEntity.ClientsBean) adapter.getItem(position);
            CustomerEntity customerEntity = null;
            CustomerDbModel customerDbModel = customerDbService.queryFirst("pkid", clientsBean.contactPkid);
            if (customerDbModel == null) return;
            customerEntity = customerDbModel.convert2Model();
            if (customerEntity == null) return;
            if (!TextUtils.isEmpty(customerEntity.contactType)) {
                MobclickAgent.onEvent(getContext(), UMMobClickAgent.look_client_click_id);
                //公司
                if (TextUtils.equals(customerEntity.contactType.toUpperCase(), "C")) {
                    CustomerCompanyDetailActivity.launch(getContext(), customerEntity.pkid, customerEntity.name, false);
                } else if (TextUtils.equals(customerEntity.contactType.toUpperCase(), "P")) {
                    CustomerPersonDetailActivity.launch(getContext(), customerEntity.pkid, customerEntity.name, false);
                }
            }
        }
    }

    /**
     * 是否有查看联系人权限
     *
     * @return
     */
    private boolean hasCustomerPermission() {
        return SpUtils.getInstance().getBooleanData(KEY_CUSTOMER_PERMISSION, false);
    }
}
