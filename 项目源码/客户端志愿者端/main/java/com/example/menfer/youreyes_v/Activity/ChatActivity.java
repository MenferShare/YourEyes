package com.example.menfer.youreyes_v.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.menfer.youreyes_v.R;
import com.hyphenate.easeui.ui.EaseChatFragment;

/**
 * Created by Menfer on 2017/10/29.
 */
public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EaseChatFragment chatFragment = new EaseChatFragment();
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container,chatFragment).commit();

    }
}
