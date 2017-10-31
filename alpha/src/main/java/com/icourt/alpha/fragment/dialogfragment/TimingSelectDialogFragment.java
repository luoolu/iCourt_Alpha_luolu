package com.icourt.alpha.fragment.dialogfragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.icourt.alpha.R;
import com.icourt.alpha.adapter.baseadapter.BaseFragmentAdapter;
import com.icourt.alpha.base.BaseDialogFragment;
import com.icourt.alpha.base.BaseFragment;
import com.icourt.alpha.constants.TimingConfig;
import com.icourt.alpha.entity.bean.TimingSelectEntity;
import com.icourt.alpha.fragment.TimingSelectDayFragment;
import com.icourt.alpha.fragment.TimingSelectMonthFragment;
import com.icourt.alpha.fragment.TimingSelectWeekFragment;
import com.icourt.alpha.fragment.TimingSelectYearFragment;
import com.icourt.alpha.interfaces.OnDateSelectedListener;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;
import com.icourt.alpha.utils.DensityUtil;
import com.icourt.alpha.view.NoScrollViewPager;
import com.icourt.alpha.widget.manager.TimerDateManager;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Description 计时列表时间筛选 - 日、周、月、年时间选择窗
 * Company Beijing icourt
 * author  youxuan  E-mail:zhaodanyang@icourt.cc
 * date createTime：2017/9/16
 * version 1.0.0
 */

public class TimingSelectDialogFragment extends BaseDialogFragment implements OnDateSelectedListener {

    private static final String KEY_SELECTED_TYPE = "keySelectedType";
    private static final String KEY_START_TIME = "keyStartTime";

    private Unbinder bind;

    @BindView(R.id.tab_day_ctv)
    CheckedTextView tabDayCtv;
    @BindView(R.id.tab_week_ctv)
    CheckedTextView tabWeekCtv;
    @BindView(R.id.tab_month_ctv)
    CheckedTextView tabMonthCtv;
    @BindView(R.id.tab_year_ctv)
    CheckedTextView tabYearCtv;
    @BindView(R.id.viewpager)
    NoScrollViewPager viewpager;
    @BindView(R.id.cancel_tv)
    TextView cancelTv;
    @BindView(R.id.complete_tv)
    TextView completeTv;

    private BaseFragmentAdapter baseFragmentAdapter;
    OnFragmentCallBackListener onFragmentCallBackListener;

    @TimingConfig.TIMINGQUERYTYPE
    int selectedType;//选中的类型
    long startTime;//开始时间

    public static TimingSelectDialogFragment newInstance(@TimingConfig.TIMINGQUERYTYPE int type, long startTime) {
        TimingSelectDialogFragment dialogFragment = new TimingSelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_SELECTED_TYPE, type);
        bundle.putLong(KEY_START_TIME, startTime);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.dialog_fragment_timer_select, inflater, container, savedInstanceState);
        bind = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null && context instanceof OnFragmentCallBackListener) {
            onFragmentCallBackListener = (OnFragmentCallBackListener) context;
        }
    }

    @Override
    protected void initView() {
        if (getArguments() != null) {
            selectedType = TimingConfig.convert2timingQueryType(getArguments().getInt(KEY_SELECTED_TYPE, TimingConfig.TIMING_QUERY_BY_DAY));
            startTime = getArguments().getLong(KEY_START_TIME, System.currentTimeMillis());
        }

        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setWindowAnimations(R.style.AppThemeSlideAnimation);
                window.setGravity(Gravity.BOTTOM);
                View decorView = window.getDecorView();
                if (decorView != null) {
                    int dp15 = DensityUtil.dip2px(getContext(), 15);
                    decorView.setPadding(dp15, dp15, dp15, dp15);
                }
            }
        }

        viewpager.setAdapter(baseFragmentAdapter = new BaseFragmentAdapter(getChildFragmentManager()));
        viewpager.setOffscreenPageLimit(5);
        baseFragmentAdapter.bindData(true,
                Arrays.asList(TimingSelectDayFragment.newInstance(selectedType == TimingConfig.TIMING_QUERY_BY_DAY ? startTime : System.currentTimeMillis()),
                        TimingSelectWeekFragment.newInstance(selectedType == TimingConfig.TIMING_QUERY_BY_WEEK ? startTime : System.currentTimeMillis()),
                        TimingSelectMonthFragment.newInstance(selectedType == TimingConfig.TIMING_QUERY_BY_MONTH ? startTime : System.currentTimeMillis()),
                        TimingSelectYearFragment.newInstance(selectedType == TimingConfig.TIMING_QUERY_BY_YEAR ? startTime : System.currentTimeMillis())));

        viewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                selectTabItem(position);
            }

        });

        switch (selectedType) {//根据显示的type，切换到对应type页。
            case TimingConfig.TIMING_QUERY_BY_DAY:
                viewpager.setCurrentItem(0);
                selectTabItem(0);
                break;
            case TimingConfig.TIMING_QUERY_BY_WEEK:
                viewpager.setCurrentItem(1);
                selectTabItem(1);
                break;
            case TimingConfig.TIMING_QUERY_BY_MONTH:
                viewpager.setCurrentItem(2);
                selectTabItem(2);
                break;
            case TimingConfig.TIMING_QUERY_BY_YEAR:
                viewpager.setCurrentItem(3);
                selectTabItem(3);
                break;
            default:
                break;
        }
    }

    /**
     * 选中日、周、月、年的某个tab
     *
     * @param position
     */
    private void selectTabItem(int position) {
        tabDayCtv.setChecked(position == 0);//说明是日
        tabWeekCtv.setChecked(position == 1);//说明是周
        tabMonthCtv.setChecked(position == 2);//说明是月
        tabYearCtv.setChecked(position == 3);//说明是年

        if (position == 0) {//如果是日的选择时间，需要判断选择的时间是否在有效范围内
            Fragment fragment = baseFragmentAdapter.getItem(position);
            if (fragment != null && fragment instanceof TimingSelectDayFragment) {
                TimingSelectDayFragment dayFragment = (TimingSelectDayFragment) fragment;
                onDateSelected(dayFragment.getSelectedTime());
            }
        } else {
            completeTv.setBackgroundResource(R.drawable.bg_round_orange);
            completeTv.setEnabled(true);
        }
    }

    @OnClick({R.id.cancel_tv,
            R.id.complete_tv,
            R.id.tab_day_ctv,
            R.id.tab_week_ctv,
            R.id.tab_month_ctv,
            R.id.tab_year_ctv})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab_day_ctv:
                viewpager.setCurrentItem(0);
                break;
            case R.id.tab_week_ctv:
                viewpager.setCurrentItem(1);
                break;
            case R.id.tab_month_ctv:
                viewpager.setCurrentItem(2);
                break;
            case R.id.tab_year_ctv:
                viewpager.setCurrentItem(3);
                break;
            case R.id.cancel_tv:
                dismiss();
                break;
            case R.id.complete_tv:
                BaseFragment fragment = (BaseFragment) baseFragmentAdapter.getItem(viewpager.getCurrentItem());
                Bundle fragmentData = fragment.getFragmentData(0, null);
                if (fragmentData == null) {
                    return;
                }
                TimingSelectEntity timingSelectEntity = (TimingSelectEntity) fragmentData.getSerializable(KEY_FRAGMENT_RESULT);
                if (timingSelectEntity != null) {
                    int currentItem = viewpager.getCurrentItem();
                    int type = TimingConfig.TIMING_QUERY_BY_DAY;
                    if (currentItem == 0) {//日
                        type = TimingConfig.TIMING_QUERY_BY_DAY;
                    } else if (currentItem == 1) {//周
                        type = TimingConfig.TIMING_QUERY_BY_WEEK;
                    } else if (currentItem == 2) {//月
                        type = TimingConfig.TIMING_QUERY_BY_MONTH;
                    } else if (currentItem == 3) {//年
                        type = TimingConfig.TIMING_QUERY_BY_YEAR;
                    }
                    if (onFragmentCallBackListener != null) {
                        onFragmentCallBackListener.onFragmentCallBack(this, type, fragmentData);
                    }
                    log("开始时间： －－－－  " + timingSelectEntity.startTimeStr);
                    log("结束时间： －－－－  " + timingSelectEntity.endTimeStr);
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSelected(long timeMillis) {
        if (timeMillis < TimerDateManager.getStartDate().getTimeInMillis()) {//如果选中的时间小于2015年1月1日，则完成按钮不可点击
            completeTv.setEnabled(false);
            completeTv.setBackgroundResource(R.drawable.bg_round_gray);
        } else if (timeMillis > System.currentTimeMillis()) {//如果选中的时间大于当前时间，则完成按钮不可点击
            completeTv.setEnabled(false);
            completeTv.setBackgroundResource(R.drawable.bg_round_gray);
        } else {//可点击
            completeTv.setEnabled(true);
            completeTv.setBackgroundResource(R.drawable.bg_round_orange);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }
}
