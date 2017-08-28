package com.example.youhe.youhecheguanjia.ui.base;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.bean.P92PayBen;

/**
 * 交易失败界面
 */
public class FailurePayActivity extends AppCompatActivity {

    private Button repay_btn;

    private TextView text1,text2;

    private String error_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failure_pay);

        in();
    }


    private void in() {
        text1 = (TextView) findViewById(R.id.text);
        text2 = (TextView) findViewById(R.id.textdoll);
        repay_btn = (Button) findViewById(R.id.repay_btn);
        Intent intent = getIntent();
        String error_msg=intent.getStringExtra("show_msg");
        text2.setText(error_msg+"");
//         String biaoshi = intent.getStringExtra("biaoshi");
//        if (biaoshi.equals("processingiscomplete")){
//            P92PayBen p92PayBen = (P92PayBen) intent.getSerializableExtra("P92PayBen");
//            text2.setText(p92PayBen.getData().getRes_msg());//显示错误原因
//            text2.setTextSize(30);
//            text1.setVisibility(View.GONE);
//        }else if (biaoshi.equals("todealwithfailure")){
//              String P92PayBen= intent.getStringExtra("P92PayBen");
//              text2.setText("错误码:"+P92PayBen);
//        }

        repay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 返回键点击事件
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
            Intent intent=new Intent(FailurePayActivity.this,OrderStyleActivity.class);
            startActivity(intent);
            finish();
            return false;
        }
        return false;
    }
}
