package com.example.youhe.youhecheguanjia.ui.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.app.AppContext;
import com.example.youhe.youhecheguanjia.app.CommentSetting;
import com.example.youhe.youhecheguanjia.bean.FirstEvent;
import com.example.youhe.youhecheguanjia.biz.TimeButtoDao;
import com.example.youhe.youhecheguanjia.db.biz.StatusSQLUtils;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.dialog.UIDialog;
import com.example.youhe.youhecheguanjia.https.URLs;
import com.example.youhe.youhecheguanjia.logic.MainService;
import com.example.youhe.youhecheguanjia.logic.Task;
import com.example.youhe.youhecheguanjia.logic.TaskType;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;
import com.example.youhe.youhecheguanjia.utils.ClickUtils;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.Misidentification;
import com.example.youhe.youhecheguanjia.utils.SystemBarUtil;
import com.example.youhe.youhecheguanjia.utils.UIHelper;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;
import com.example.youhe.youhecheguanjia.widget.ClearEditText;
import com.example.youhe.youhecheguanjia.widget.TimeButton;
import com.example.youhe.youhecheguanjia.widget.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 手机验证登录
 */
public class ShouJiDengLuActivity extends AppCompatActivity implements View.OnClickListener {
    private TimeButton timeButton;
    private ClearEditText clearEditText;
    private EditText clearEditText2;
    private VolleyUtil volleyUtil;
    private StatusSQLUtils statusSQLUtils;
    private RelativeLayout button;
    private TimeButtoDao timeButtoDao;//获取验证码DAO
    private TokenSQLUtils tsu;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private UIDialog uidialog;

    private TextView contact_service_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kuai_su_zhu_ce);

        timeButton(savedInstanceState);
        in();
    }

    private void timeButton(Bundle savedInstanceState) {
        //获取验证码键
        timeButton = (TimeButton) findViewById(R.id.button1);
        timeButton.onCreate(savedInstanceState);
        timeButton.setTextAfter("秒后重新获取").setTextBefore("获取验证码").setLenght(60 * 1000);
        timeButton.setOnClickListener(this);
    }
    private void in() {
        EventBus.getDefault().register(this);
        statusSQLUtils = new StatusSQLUtils(this);
        clearEditText = (ClearEditText) findViewById(R.id.et_shouji);//手机号码
        clearEditText2 = (EditText) findViewById(R.id.et_yanzhenmima);//
        button = (RelativeLayout) findViewById(R.id.denglijian);//登录
        uidialog = new UIDialog(this,"正在登录.......");
        button.setOnClickListener(this);
        volleyUtil = VolleyUtil.getVolleyUtil(this);//上网请求
        timeButtoDao = new TimeButtoDao(timeButton,this,clearEditText,volleyUtil);//初始化获取验证码DAO
        sp = getSharedPreferences("judges", Context.MODE_PRIVATE);
        editor = sp.edit();
        tsu = new TokenSQLUtils(this);

        contact_service_tv= (TextView) findViewById(R.id.contact_service_tv);
        contact_service_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                UIHelper.contactService(ShouJiDengLuActivity.this, CommentSetting.SERVICE_NUM);
            }
        });

    }


    /**
     * 返回键点击事件
     *
     */
    public void fanhui(View view){
        finish();
    }

    @Override
    public void onClick(View view) {
        if (ClickUtils.isFastDoubleClick()){
            return;
        }
        if (MainActivity.getHtts()==false){
            ToastUtil.getLongToastByString(this,"网络连接失败，请检测设置");
            return;
        }
        switch (view.getId()){
            case R.id.button1:
                if(clearEditText.getText().toString().trim().length()<11){
                    Toast.makeText(ShouJiDengLuActivity.this,"请输入完整手机号码",Toast.LENGTH_SHORT).show();
                }else{
                    timeButtoDao.phonePams();//手机验证码
                }

                break;
            case R.id.denglijian://登录

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }

                if(clearEditText.getText().toString().trim().length()<11){
                    Toast.makeText(ShouJiDengLuActivity.this,"请输入完整手机号码",Toast.LENGTH_SHORT).show();
                }else if(clearEditText2.getText().toString().equals("")){
                    Toast.makeText(ShouJiDengLuActivity.this,"请输入手机验证码",Toast.LENGTH_SHORT).show();
                }else {
                    delu();
                }
                break;
        }

    }

    /**
     * 登录
     */
    public void delu(){
        String mobile = clearEditText.getText().toString();//得到手机号码
        String verifycode = clearEditText2.getText().toString();//得到验证码
        HashMap pams = new HashMap<>();
        pams.put("mobile",mobile);
        pams.put("verifycode",verifycode);
        uidialog.show();
        volleyUtil.StringRequestPostVolley(URLs.VERIFY_THE_LOGIN, EncryptUtil.encrypt(pams), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                Log.i("WU","jsonObject=====>"+jsonObject.toString());
                uidialog.hide();
                json(EncryptUtil.decryptJson(jsonObject.toString(),ShouJiDengLuActivity.this));
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                ToastUtil.getShortToastByString(ShouJiDengLuActivity.this,"网络连接失败,无法发送请求");
                uidialog.hide();
            }
        });
    }

    /**
     * 解析JSOn
     * @param json
     */
    public void json(String json){

        try {
            JSONObject jsonObJect = new JSONObject(json);
             String status = jsonObJect.getString("status");
            if(status.equals("ok")){
                deluDao(jsonObJect);//登录成功后的逻辑

                HashMap params=new HashMap();
                Task ts=new Task(TaskType.TS_REFLUSH_HOME_PAGE,params);//登录成功后更新首页数据
                MainService.newTask(ts);
            }else {
               if(jsonObJect.has("code")){
                   int code=jsonObJect.getInt("code");
                   UIHelper.showErrTips(code,ShouJiDengLuActivity.this);
               }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void deluDao(JSONObject jsonObJect){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
//        saveTheLoginStatus();//保存已登录状态
        statusSQLUtils.addDate("yes");//保存已登录状态
        try {
            JSONObject data =  jsonObJect.getJSONObject("data");
            String token = data.getString("token");
            editor.putBoolean("judge",true);
            editor.commit();

            tsu.addDate(token);//保存token到数据库
            EventBus.getDefault().post(new FirstEvent("ok"));//登录成功后通知刷新


        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            AppContext.isLogin=true;
            finish();
        }
    }

    @Subscribe
    public void onEventMainThread(FirstEvent event) {

    }
}
