package com.daijun.luffy_asm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mmc.lamandys.liba_datapick.core.AutoTrackUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button jumpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jumpButton = findViewById(R.id.jumpButton);
        jumpButton.setOnClickListener(this);
        findViewById(R.id.tabButton).setOnClickListener(this);
        findViewById(R.id.toolbarBotton).setOnClickListener(this);
        findViewById(R.id.radioBotton).setOnClickListener(this);
        findViewById(R.id.drawerBotton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.jumpButton) {
            startActivity(new Intent(this, SecondActivity.class));
            System.out.println("自动埋点：" + AutoTrackUtil.traverseViewOnly(jumpButton));
            return;
        }
        if (id == R.id.tabButton) {
            startActivity(new Intent(this, TabActivity.class));
            return;
        }
        if (id == R.id.toolbarBotton) {
            startActivity(new Intent(this, ToolBarActivity.class));
            return;
        }
        if (id == R.id.radioBotton) {
            startActivity(new Intent(this, RadioActivity.class));
            return;
        }
        if (id == R.id.drawerBotton) {
            startActivity(new Intent(this, DrawerActivity.class));
        }
    }
}
