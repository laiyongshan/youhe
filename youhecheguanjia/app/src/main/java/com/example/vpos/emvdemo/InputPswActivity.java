package com.example.vpos.emvdemo;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.entity.base.SerMap;
import com.example.youhe.youhecheguanjia.ui.base.M60PayActivity;
import com.example.youhe.youhecheguanjia.ui.base.SignInActivity;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.NiftyUtil;
import com.example.youhe.youhecheguanjia.utils.ParamSign;
import com.example.youhe.youhecheguanjia.widget.OnPasswordInputFinish;
import com.example.youhe.youhecheguanjia.widget.PasswordView;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/3.
 */

public class InputPswActivity extends Activity {

    private EditText vpos_input_psw_et;
    private HashMap map=new HashMap();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PasswordView pwdView = new PasswordView(this);
        setContentView(pwdView);


        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        if(bundle.get("map")!=null) {
            map = (HashMap) bundle.get("map");
        }else{
        }

        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish() {
                //输入完成后我们简单显示一下输入的密码
//                Toast.makeText(InputPswActivity.this, pwdView.getStrPassword(), Toast.LENGTH_SHORT).show();

//                map.put("passwordpin",pwdView.getStrPassword());//输入的密码

                Message msg=new Message();
                msg.what=0;
                msg.obj=pwdView.getStrPassword()+"";
                handler.sendMessage(msg);

            }
        });

        /**
         *  可以用自定义控件中暴露出来的cancelImageView方法，重新提供相应
         *  如果写了，会覆盖我们在自定义控件中提供的响应
         *  可以看到这里toast显示 "Biu Biu Biu"而不是"Cancel"*/
        pwdView.getCancelImageView().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(InputPswActivity.this, "取消交易！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 返回键点击事件
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    Intent intent=new Intent(InputPswActivity.this,SignInActivity.class);
                    //让hashmap实现可序列化,定义一个实现可序列化的类。
//                创建Bundle对象，存放实现可序列化的SerMap
                    Bundle bundle=new Bundle();
                    SerMap serMap=new SerMap();
                    map.put("passwordpin",ParamSign.getUserPassword((String)msg.obj));//输入的密码
//                    Toast.makeText(InputPswActivity.this,">>>>>>>>"+msg.obj,Toast.LENGTH_LONG).show();
                    serMap.setMap(map);
                    bundle.putSerializable("serMap",serMap);
                    //意图放置bundle变量
                    intent.putExtras(bundle);
                    startActivity(intent);
                    InputPswActivity.this.finish();
                    break;
            }
        }

    };
}
