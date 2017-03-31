package com.icourt.alpha.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.icourt.alpha.R;
import com.icourt.alpha.adapter.DemoAdapter;
import com.icourt.alpha.base.BaseActivity;
import com.icourt.alpha.entity.bean.DemoEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Description
 * Company Beijing guokeyuzhou
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：17/3/30
 * version
 */

public class DemoActivity extends BaseActivity {

    public static void launch(@NonNull Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DemoActivity.class);
        context.startActivity(intent);
    }

    RecyclerView recyclerView;
    DemoAdapter demoAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(demoAdapter = new DemoAdapter());

        //模拟数据
        List<DemoEntity> demoEntityList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            demoEntityList.add(new DemoEntity("name_" + i, new Random().nextInt(50) + 10));
        }
        demoAdapter.bindData(true, demoEntityList);
    }
}
