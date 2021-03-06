package com.example.youhe.youhecheguanjia.ui.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.app.CommentSetting;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.https.URLs;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.ParamSign;
import com.example.youhe.youhecheguanjia.utils.SystemBarUtil;
import com.example.youhe.youhecheguanjia.utils.TripleDES;
import com.example.youhe.youhecheguanjia.utils.UIHelper;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;
import com.example.youhe.youhecheguanjia.widget.ClearEditText;
import com.example.youhe.youhecheguanjia.widget.TimeButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/3/28 0028.
 */

public class AddMyBankCarActivity extends Activity implements View.OnClickListener{

    private ClearEditText bankcar_owner_phonenum_et,bankcar_address_et,bankcard_code_et,bank_name_et,et_auth_code;
    private ImageButton add_my_bankcard_back_ib;
    private Button add_bankcard_btn;
    private TimeButton timeButton;//获取验证码

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_my_bankcard);

        // 4.4及以上版本开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarUtil.setTranslucentStatus(true, AddMyBankCarActivity.this);
        }
        SystemBarUtil.useSystemBarTint(AddMyBankCarActivity.this);

        initView();

        timeButton(savedInstanceState);

        pd=new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCanceledOnTouchOutside(false);
    }

    /**
     * 获取验证码
     * @param savedInstanceState
     */
    private void timeButton(Bundle savedInstanceState) {

        //获取验证码键
        timeButton= (TimeButton) findViewById(R.id.auth_code_btn);
        timeButton.onCreate(savedInstanceState);
        timeButton.setTextAfter("秒后重新获取").setTextBefore("点击获取验证码").setLenght(30 * 1000);
        timeButton.setOnClickListener(this);

    }

    private void initView(){
        bankcar_owner_phonenum_et= (ClearEditText) findViewById(R.id.bankcar_owner_phonenum_et);
//        bankcard_idcard_num_et= (ClearEditText) findViewById(R.id.bankcard_idcard_num_et);
        bankcar_address_et= (ClearEditText) findViewById(R.id.bankcar_address_et);
        bankcard_code_et= (ClearEditText) findViewById(R.id.bankcard_code_et);
        bank_name_et= (ClearEditText) findViewById(R.id.bank_name_et);
        et_auth_code= (ClearEditText) findViewById(R.id.et_auth_code);

        add_my_bankcard_back_ib= (ImageButton) findViewById(R.id.add_my_bankcard_back_ib);
        add_my_bankcard_back_ib.setOnClickListener(this);

        add_bankcard_btn= (Button) findViewById(R.id.add_bankcard_btn);
        add_bankcard_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.add_my_bankcard_back_ib:
                finish();
                break;

            case R.id.add_bankcard_btn:
                if(bankcar_owner_phonenum_et.getText().toString().trim().equals("")){
                    Toast.makeText(AddMyBankCarActivity.this,"银行卡预留手机号码不能为空",Toast.LENGTH_SHORT).show();
                }
                else if(bankcar_address_et.getText().toString().trim().equals("")){
                    Toast.makeText(AddMyBankCarActivity.this,"开户支行不能为空！",Toast.LENGTH_SHORT).show();
                }else if(bankcard_code_et.getText().toString().trim().equals("")){
                    Toast.makeText(AddMyBankCarActivity.this,"银行卡号不能为空！",Toast.LENGTH_SHORT).show();
                }else if(bank_name_et.getText().toString().trim().equals("")){
                    Toast.makeText(AddMyBankCarActivity.this,"银行名称不能为空！",Toast.LENGTH_SHORT).show();
                }else if(et_auth_code.getText().toString().trim().equals("")){
                    Toast.makeText(AddMyBankCarActivity.this,"请输入手机验证码！",Toast.LENGTH_SHORT).show();
                }else{
                    pd.show();
                    addBankCard();//添加银行卡
                }
                break;

            case R.id.auth_code_btn:
                getetVerifycode();//获取验证码
                break;
        }
    }


    HashMap phonePams;
    public HashMap  getVerifycodeParams(){
        String phoneNumber = bankcar_owner_phonenum_et.getText().toString();
        phonePams= new HashMap<>();
        String token = TokenSQLUtils.check();
        if(token!=null) {
            phonePams.put("token", token);
        }
        phonePams.put("bank_mobile", phoneNumber);

        return phonePams;
    }

    /**
     * 获取验证码
     * */

    public void getetVerifycode(){
        if (MainActivity.getHtts()==false){
            Toast.makeText(this,"网络连接失败，请检测设置",Toast.LENGTH_LONG).show();
            return;
        }

        VolleyUtil.getVolleyUtil(AddMyBankCarActivity.this).StringRequestPostVolley(URLs.SEND_BANK_VERIFY_CODE, EncryptUtil.encrypt(getVerifycodeParams()), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                EncryptUtil.decryptJson(jsonObject.toString(),AddMyBankCarActivity.this);
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                Toast.makeText(AddMyBankCarActivity.this, "网络连接失败",Toast.LENGTH_LONG).show();
            }
        });
    }


    //添加银行卡
    public void addBankCard(){

        VolleyUtil.getVolleyUtil(AddMyBankCarActivity.this).StringRequestPostVolley(URLs.ADD_BANK_CARD, EncryptUtil.encrypt(getAddBankParams()), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                parseJson(EncryptUtil.decryptJson(jsonObject.toString(),AddMyBankCarActivity.this));//解析解密之后的数据
                pd.dismiss();
            }
            @Override
            public void ResponError(VolleyError volleyError) {
                pd.dismiss();
            }
        });
    }


    HashMap<String, Object> map;
    //获取添加银行参数
    public HashMap getAddBankParams(){
        map = new HashMap<String, Object>();
        String token = TokenSQLUtils.check();
        if(token!=null) {
            map.put("token", token);
        }

        map.put("bank_name",bank_name_et.getText().toString().trim());
        map.put("bank_code",bankcard_code_et.getText().toString().trim());
        map.put("bank_address",bankcar_address_et.getText().toString().trim());
        map.put("bank_mobile",bankcar_owner_phonenum_et.getText().toString().trim());
        map.put("verifycode",et_auth_code.getText().toString().trim());

        return map;
    }


    //解析添加银行卡信息
    private void parseJson(String json){
        try {
            JSONObject obj=new JSONObject(json);
            String status=obj.getString("status");
            if(obj.has("code")){
                int code=obj.getInt("code");
                UIHelper.showErrTips(code,AddMyBankCarActivity.this);
            }
            if(status.equals("ok")){
                Toast.makeText(AddMyBankCarActivity.this,"添加银行卡成功",Toast.LENGTH_LONG).show();
                finish();
            }else{
                Toast.makeText(AddMyBankCarActivity.this,"添加银行卡失败，请重试",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
