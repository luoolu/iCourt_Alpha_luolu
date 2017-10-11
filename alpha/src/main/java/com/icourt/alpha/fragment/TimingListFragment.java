package com.icourt.alpha.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andview.refreshview.XRefreshView;
import com.icourt.alpha.R;
import com.icourt.alpha.adapter.TimeAdapter;
import com.icourt.alpha.adapter.baseadapter.adapterObserver.RefreshViewEmptyObserver;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.constants.TimingConfig;
import com.icourt.alpha.entity.bean.TimeEntity;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.view.xrefreshlayout.RefreshLayout;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Description  计时列表
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/10/9
 * version 2.1.0
 */
public class TimingListFragment extends BaseFragment {

    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_QUERY_TYPE = "queryType";
    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;
    Unbinder unbinder;

    /**
     * @param queryType
     * @param startTimeMillis 毫秒 开始时间 1:日的开始时间 2:周的开始时间 3:月的开始时间 4:年的开始时间
     * @return
     */
    public static TimingListFragment newInstance(@TimingConfig.TIMINGQUERYTYPE int queryType, long startTimeMillis) {
        TimingListFragment fragment = new TimingListFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_START_TIME, startTimeMillis);
        args.putInt(KEY_QUERY_TYPE, queryType);
        fragment.setArguments(args);
        return fragment;
    }

    @TimingConfig.TIMINGQUERYTYPE
    int queryType;
    long startTimeMillis;
    long endTimeMillis;
    TimeAdapter timeAdapter;
    int pageIndex = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.layout_refresh_recyclerview, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initView() {
        queryType = TimingConfig.convert2timingQueryType(getArguments().getInt(KEY_QUERY_TYPE));
        startTimeMillis = getArguments().getLong(KEY_START_TIME);
        if (queryType == TimingConfig.TIMING_QUERY_BY_DAY) {//日
            endTimeMillis = startTimeMillis + TimeUnit.DAYS.toMillis(1) - 1;
        } else if (queryType == TimingConfig.TIMING_QUERY_BY_WEEK) {//周
            endTimeMillis = startTimeMillis + TimeUnit.DAYS.toMillis(7) - 1;
        } else if (queryType == TimingConfig.TIMING_QUERY_BY_MONTH) {//月
            endTimeMillis = DateUtils.getMonthLastDay(startTimeMillis);
        } else if (queryType == TimingConfig.TIMING_QUERY_BY_YEAR) {//年

        }
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(timeAdapter = new TimeAdapter(true));
        refreshLayout.setNoticeEmpty(R.mipmap.icon_placeholder_timing, R.string.timing_empty);
        refreshLayout.setMoveForHorizontal(true);
        timeAdapter.registerAdapterDataObserver(new RefreshViewEmptyObserver(refreshLayout, timeAdapter));
        refreshLayout.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean isSilence) {
                super.onLoadMore(isSilence);
                getData(false);
            }
        });
        refreshLayout.setPullRefreshEnable(false);
        boolean canLoadMore = (queryType != TimingConfig.TIMING_QUERY_BY_DAY && queryType != TimingConfig.TIMING_QUERY_BY_WEEK); //年月可以上拉加载
        canLoadMore = true;
        refreshLayout.setPullLoadEnable(canLoadMore);
        getData(true);
    }

    @Override
    protected void getData(boolean isRefresh) {
        super.getData(isRefresh);
        long dividerTime = (pageIndex * TimeUnit.DAYS.toMillis(7));
        long weekStartTimeMillSecond = startTimeMillis - dividerTime;
        long weekEndTimeMillSecond = weekStartTimeMillSecond + TimeUnit.DAYS.toMillis(7);

        String weekStartTime = DateUtils.getyyyy_MM_dd(weekStartTimeMillSecond);
        String weekEndTime = DateUtils.getyyyy_MM_dd(weekEndTimeMillSecond);

        timingListQueryByTime(weekStartTime, weekEndTime);
    }

    /**
     * 获取某周的计时项
     *
     * @param weekStartTime
     * @param weekEndTime
     */
    private void timingListQueryByTime(String weekStartTime, String weekEndTime) {
        callEnqueue(
                getApi().timingListQueryByTime(getLoginUserId(), weekStartTime, weekEndTime, 0, Integer.MAX_VALUE),
                new SimpleCallBack<TimeEntity>() {
                    @Override
                    public void onSuccess(Call<ResEntity<TimeEntity>> call, Response<ResEntity<TimeEntity>> response) {
                        if (response.body().result != null) {
                            timeAdapter.bindData(true, response.body().result.items);
                        }
                        stopRefresh();
                    }

                    @Override
                    public void onFailure(Call<ResEntity<TimeEntity>> call, Throwable t) {
                        super.onFailure(call, t);
                        stopRefresh();
                    }
                }
        );
    }

    private void stopRefresh() {
        if (refreshLayout != null) {
            refreshLayout.stopRefresh();
            refreshLayout.stopLoadMore();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
