package com.example.youhe.youhecheguanjia.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.https.URLs;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;
import com.example.youhe.youhecheguanjia.ui.base.AccountQueryActivity;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.UIHelper;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/7/19.
 */

public class DeleteAcountDialog extends Dialog implements View.OnClickListener{

    private TextView delete_tips_tv;
    private TextView delete_cancel_tv,delete_sure_tv;

    private AccountQueryActivity accountQueryActivity;
    private Context context;

    private String account;

    public DeleteAcountDialog(@NonNull Context context, @StyleRes int themeResId,String account,AccountQueryActivity accountQueryActivity) {
        super(context, themeResId);
        this.account=account;
        this.accountQueryActivity=accountQueryActivity;
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_account);

        initView();
    }

    private void initView(){

        delete_tips_tv= (TextView) findViewById(R.id.delete_tips_tv);
        delete_tips_tv.setText("是否确定删除账号："+account);
        delete_cancel_tv= (TextView) findViewById(R.id.delete_cancel_tv);
        delete_cancel_tv.setOnClickListener(this);
        delete_sure_tv= (TextView) findViewById(R.id.delete_sure_tv);
        delete_sure_tv.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_cancel_tv:
                dismiss();
                break;

            case R.id.delete_sure_tv:
//
                deleteAccount(account);
                break;
        }
    }


    private void deleteAccount(String vehicleAccount){

//        UIHelper.showPd(context);

        HashMap map=new HashMap();
        map.put("token", TokenSQLUtils.check());
        map.put("vehicleAccount",vehicleAccount);
        VolleyUtil.getVolleyUtil(context).StringRequestPostVolley(URLs.DELETE_ACCOUNT_122, EncryptUtil.encrypt(map), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                Log.i("TAG","删除账号返回的数据是："+jsonObject.toString());
                try {
                    JSONObject obj=new JSONObject(EncryptUtil.decryptJson(jsonObject.toString(),context));
                    String status=obj.optString("status");
                    if(status.equals("ok")){
                        //删除成功
                        accountQueryActivity.getAccountList();
                        dismiss();
                        Toast.makeText(context,"删除成功！",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context,"删除失败！",Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
//                    UIHelper.dismissPd();
                }
            }

            @Override
            public void ResponError(VolleyError volleyError) {

            }
        });
    }

}
