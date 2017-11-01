package com.icourt.alpha.adapter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.icourt.alpha.R;
import com.icourt.alpha.adapter.baseadapter.BaseArrayRecyclerAdapter;
import com.icourt.alpha.adapter.baseadapter.BaseRecyclerAdapter;
import com.icourt.alpha.entity.bean.TimeEntity;
import com.icourt.alpha.utils.DateUtils;
import com.icourt.alpha.utils.GlideUtils;
import com.icourt.alpha.utils.LoginInfoUtils;
import com.icourt.alpha.utils.SystemUtils;
import com.icourt.alpha.utils.UMMobClickAgent;
import com.icourt.alpha.view.recyclerviewDivider.ITimeDividerInterface;
import com.icourt.alpha.widget.manager.TimerManager;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.umeng.socialize.utils.ContextUtil.getContext;

/**
 * Description 计时
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/4
 * version 2.0.0
 */

public class TimeAdapter extends BaseArrayRecyclerAdapter<TimeEntity.ItemEntity> implements ITimeDividerInterface, BaseRecyclerAdapter.OnItemChildClickListener {

    private static final int TIME_TOP_TYPE = 0;
    private static final int TIME_OTHER_TYPE = 1;
    private static final int TIME_SIMPLE_TITLE = 2;
    private HashMap<Integer, Long> timeShowArray = new HashMap<>();//时间分割线
    private long sumTime;

    private boolean useSimpleTitle;

    @Override
    public boolean bindData(boolean isRefresh, List<TimeEntity.ItemEntity> datas) {
        if (isRefresh) {
            timeShowArray.clear();
            //分组  避免上拉加载从最后初始化
            if (datas != null && !datas.isEmpty()) {
                for (int i = 0; i < datas.size(); i++) {
                    TimeEntity.ItemEntity itemEntity = datas.get(i);
                    addTimeDividerArray(itemEntity, i);
                }
            }
        }
        return super.bindData(isRefresh, datas);
    }

    public TimeAdapter(boolean useSimpleTitle) {
        this.useSimpleTitle = useSimpleTitle;
        this.setOnItemChildClickListener(this);
    }

    public TimeAdapter() {
        this.setOnItemChildClickListener(this);
    }

    public void setSumTime(long sumTime) {
        this.sumTime = sumTime;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (useSimpleTitle) {
            return TIME_SIMPLE_TITLE;
        }
        if (position == 0) {
            return TIME_TOP_TYPE;
        }
        return TIME_OTHER_TYPE;
    }

    @Override
    public int bindView(int viewtype) {
        switch (viewtype) {
            case TIME_TOP_TYPE:
                return R.layout.adapter_item_time_top;
            case TIME_OTHER_TYPE:
                return R.layout.adapter_item_time;
            case TIME_SIMPLE_TITLE:
                return R.layout.adapter_item_timing_simple;
            default:
                break;
        }
        return R.layout.adapter_item_time;
    }

    @Override
    public void onBindHoder(ViewHolder holder, TimeEntity.ItemEntity timeEntity, int position) {
        switch (holder.getItemViewType()) {
            case TIME_TOP_TYPE:
                setTypeTopData(holder, timeEntity);
                break;
            case TIME_OTHER_TYPE:
                setTypeOtherData(holder, timeEntity, position);
                break;
            case TIME_SIMPLE_TITLE:
                setTypeSimpleTitle(holder, timeEntity, position);
                break;
            default:
                break;
        }
    }

    /**
     * 处理简单标题布局
     * R.layout.adapter_item_timing_simple
     *
     * @param holder
     * @param timeEntity
     * @param position
     */
    private void setTypeSimpleTitle(ViewHolder holder, TimeEntity.ItemEntity timeEntity, int position) {
        if (holder == null || timeEntity == null) {
            return;
        }
        ImageView timer_icon = holder.obtainView(R.id.timer_icon);
        TextView timer_count_tv = holder.obtainView(R.id.timer_count_tv);
        TextView timer_title_tv = holder.obtainView(R.id.timer_title_tv);
        View divider_ll = holder.obtainView(R.id.divider_ll);
        TextView divider_time = holder.obtainView(R.id.divider_time);
        TextView divider_time_count = holder.obtainView(R.id.divider_time_count);
        timer_title_tv.setText(TextUtils.isEmpty(timeEntity.name) ? "未录入工作描述" : timeEntity.name);
        if (timeEntity.state == TimeEntity.ItemEntity.TIMER_STATE_START) {
            //说明是正在计时
            long useTime = TimerManager.getInstance().getTimingSeconds();
            if (useTime < 0) {
                useTime = 0;
            }
            timer_count_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            timer_count_tv.setTextColor(SystemUtils.getColor(timer_count_tv.getContext(), R.color.colorPrimary));
            timer_count_tv.setText(DateUtils.getTimingStr(useTime));
            timer_icon.setImageResource(R.drawable.orange_side_dot_bg);
        } else {
            //说明没有在计时
            timer_count_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            timer_count_tv.setTextColor(SystemUtils.getColor(timer_count_tv.getContext(), R.color.textColorPrimary));
            try {
                timer_count_tv.setText(DateUtils.getHmIntegral(timeEntity.useTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            timer_icon.setImageResource(R.mipmap.icon_start_20);
        }
        holder.bindChildClick(timer_icon);
        addTimeDividerArray(timeEntity, position);
        if (timeShowArray.containsKey(position)) {
            divider_ll.setVisibility(View.VISIBLE);
            if (DateUtils.isToday(timeEntity.workDate)) {
                divider_time.setText("今天");
            } else if (DateUtils.isYesterday(timeEntity.workDate)) {
                divider_time.setText("昨天");
            } else {
                divider_time.setText(DateUtils.getTimeDate(timeEntity.workDate));
            }
            long dayTimingLength = timeEntity.todayTimingSum;
            divider_time_count.setText(DateUtils.getHmIntegral(dayTimingLength));
        } else {
            divider_ll.setVisibility(View.GONE);
        }
    }

    /**
     * 设置顶部数据
     */
    private void setTypeTopData(ViewHolder holder, TimeEntity.ItemEntity timeEntity) {
        TextView totalView = holder.obtainView(R.id.time_top_total_tv);
        if (sumTime > 0) {
            totalView.setText(DateUtils.getHmIntegral(sumTime) + "'");
        }
    }

    /**
     * 设置列表数据
     */
    public void setTypeOtherData(ViewHolder holder, TimeEntity.ItemEntity timeEntity, int position) {
        addTimeDividerArray(timeEntity, position);
        TextView durationView = holder.obtainView(R.id.time_item_duration_tv);
        TextView quantumView = holder.obtainView(R.id.time_item_quantum_tv);
        ImageView photoView = holder.obtainView(R.id.time_item_user_photo_image);
        TextView descView = holder.obtainView(R.id.time_item_desc_tv);
        TextView userNameView = holder.obtainView(R.id.time_item_user_name_tv);
        TextView typeView = holder.obtainView(R.id.time_item_type_tv);
        ImageView rightArrow = holder.obtainView(R.id.time_item_right_arrow);
        if (TextUtils.equals(timeEntity.createUserId, LoginInfoUtils.getLoginUserId())) {
            rightArrow.setVisibility(View.VISIBLE);
        } else {
            rightArrow.setVisibility(View.INVISIBLE);
        }

        if (timeEntity.state == TimeEntity.ItemEntity.TIMER_STATE_START) {
            long useTime = timeEntity.useTime;
            if (useTime <= 0 && timeEntity.startTime > 0) {
                useTime = DateUtils.millis() - timeEntity.startTime;
            }
            if (useTime < 0) {
                useTime = 0;
            }
            durationView.setText(DateUtils.getTimingStr(useTime / TimeUnit.SECONDS.toMillis(1)));
            quantumView.setText(DateUtils.getFormatDate(timeEntity.startTime, DateUtils.DATE_HHMM_STYLE1) + " - 现在");
        } else {
            try {
                durationView.setText(DateUtils.getHmIntegral(timeEntity.useTime));
                quantumView.setText(DateUtils.getFormatDate(timeEntity.startTime, DateUtils.DATE_HHMM_STYLE1) + " - " + DateUtils.getFormatDate(timeEntity.endTime, DateUtils.DATE_HHMM_STYLE1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GlideUtils.loadUser(holder.itemView.getContext(), timeEntity.userPic, photoView);
        descView.setText(TextUtils.isEmpty(timeEntity.name) ? "未录入工作描述" : timeEntity.name);
        userNameView.setText(timeEntity.username);
        typeView.setText(timeEntity.workTypeName);
    }

    /**
     * 处理时间分割线
     *
     * @param timeEntity
     */
    private void addTimeDividerArray(TimeEntity.ItemEntity timeEntity, int position) {
        if (timeEntity == null) {
            return;
        }
        if (!timeShowArray.containsValue(timeEntity.workDate)) {
            timeShowArray.put(position, timeEntity.workDate);
        }
    }

    /**
     * 是否显示时间
     *
     * @param pos
     * @return
     */
    @Override
    public boolean isShowTimeDivider(int pos) {
        if (pos != 0) {
            TimeEntity.ItemEntity item = getItem(pos);
            return item != null && timeShowArray.get(pos) != null;
        }
        return false;
    }

    /**
     * 显示的时间字符串 isShowTimeDivider=true 不可以返回null
     *
     * @param pos
     * @return
     */
    @NonNull
    @Override
    public String getShowTime(int pos) {
        if (pos != 0) {
            TimeEntity.ItemEntity item = getItem(pos);
            if (item != null) {
                if (DateUtils.isThisYear(item.workDate)) {
                    if (DateUtils.isToday(item.workDate)) {
                        return "今天";
                    } else if (DateUtils.isYesterday(item.workDate)) {
                        return "昨天";
                    } else {
                        DateUtils.getTimeDate(item.workDate);
                    }
                } else {
                    DateUtils.getFormatDate(item.workDate, DateUtils.DATE_YYYYMMDD_STYLE2);
                }
            }
            return item != null ?
                    DateUtils.getTimeDate(item.workDate) : "null";
        }
        return "";
    }

    @Override
    public void onItemChildClick(BaseRecyclerAdapter adapter, ViewHolder holder, View view, int position) {
        TimeEntity.ItemEntity item = getItem(getRealPos(position));
        if (item == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.timer_icon:
                if (item.state == TimeEntity.TIMER_STATE_END_TYPE) {
                    item.state = 0;
                    MobclickAgent.onEvent(getContext(), UMMobClickAgent.start_timer_click_id);
                    TimerManager.getInstance().addTimer(item);
                } else {
                    item.state = 1;
                    MobclickAgent.onEvent(getContext(), UMMobClickAgent.stop_timer_click_id);
                    TimerManager.getInstance().stopTimer();
                }
                notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
