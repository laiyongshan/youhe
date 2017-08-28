package com.example.youhe.youhecheguanjia.ui.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.biz.ThePosPay;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.https.URLs;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;
import com.example.youhe.youhecheguanjia.logic.YeoheActivity;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.HttpUtil;
import com.example.youhe.youhecheguanjia.utils.PayUtil;
import com.example.youhe.youhecheguanjia.utils.QrCodeUtil;
import com.example.youhe.youhecheguanjia.utils.UIHelper;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/5/12 0012.
 */

public class ScanQrPayActivity extends YeoheActivity implements View.OnClickListener{

    private int width,height;
    private ImageView pay_qr_code_img;
    private TextView pay_money_tv;
    private TextView type_tips_tv;
    private TextView expired_time_tv;

    private ImageView weixin_pay_img,ali_pay_img;

    private String ordercode;
    private String paymoney;
    private int mjOpenType;
    private int orderstyle;//订单类型

    private VolleyUtil volleyUtil;

    public static Timer timer;
    private  TimerTask task;

    private int payInt=1;

    private String token= TokenSQLUtils.check();//token值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_pay);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        ordercode=getIntent().getStringExtra("ordercode");
        paymoney=getIntent().getStringExtra("paymoney");
        mjOpenType=getIntent().getIntExtra("mjOpenType",0);
        orderstyle=getIntent().getIntExtra("orderstyle",0);


        volleyUtil= VolleyUtil.getVolleyUtil(this);

        initViews();
//        payType(1);

      task=new TimerTask() {
          @Override
          public void run() {
              Message msg=new Message();
              msg.what=1;
              handler.sendMessage(msg);
          }
      };

      timer=new Timer();
      timer.schedule(task,3000,3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        UIHelper.dismissPd();
    }

    /*
        * 初始化控件
        * */
    private void initViews(){

        weixin_pay_img= (ImageView) findViewById(R.id.weixin_pay_img);
        weixin_pay_img.setOnClickListener(this);
        ali_pay_img= (ImageView) findViewById(R.id.ali_pay_img);
        ali_pay_img.setOnClickListener(this);

        type_tips_tv= (TextView) findViewById(R.id.type_tips_tv);

        expired_time_tv= (TextView) findViewById(R.id.expired_time_tv);

        pay_qr_code_img= (ImageView) findViewById(R.id.pay_qr_code_img);
        ViewGroup.LayoutParams  lp;
        lp=pay_qr_code_img.getLayoutParams();
        lp.width=width*4/7;
        lp.height=width*4/7;
        pay_qr_code_img.setLayoutParams(lp);

        pay_money_tv= (TextView) findViewById(R.id.pay_money_tv);
        pay_money_tv.setText(paymoney+"");

        if(mjOpenType==1){
            weixin_pay_img.setClickable(false);
            ali_pay_img.setClickable(false);
            payType(1);

        }else if(mjOpenType==2){
            weixin_pay_img.setClickable(false);
            ali_pay_img.setClickable(true);
            payType(2);
        }else if(mjOpenType==3){
            weixin_pay_img.setClickable(true);
            ali_pay_img.setClickable(true);
            payType(1);

        }else if(mjOpenType==0){
            weixin_pay_img.setClickable(false);
            ali_pay_img.setClickable(false);
            expired_time_tv.setText("获取支付二维码有误，请退出重试");
        }else{
            weixin_pay_img.setClickable(false);
            ali_pay_img.setClickable(false);
            expired_time_tv.setText("获取支付二维码有误，请退出重试");
        }

    }


    private void payType(int type){
        if(type==1){
            weixin_pay_img.setImageResource(R.drawable.weixinzhifu);
            ali_pay_img.setImageResource(R.drawable.zhifubao2);
            type_tips_tv.setText("微信扫一扫，向我付款");

            getQrCodeUrl(getQrcodeUrlParam(1));

            payInt=1;

        }else if(type==2){
            weixin_pay_img.setImageResource(R.drawable.weixinzhifu2);
            ali_pay_img.setImageResource(R.drawable.zhifubao);
            type_tips_tv.setText("支付宝扫一扫，向我付款");

            getQrCodeUrl(getQrcodeUrlParam(2));

            payInt=2;
        }
    }


    HashMap map=new HashMap();
    private HashMap getQrcodeUrlParam(int paytype){
        map=new HashMap();
        if(token!=null) {
            map.put("token", token);
        }else{
            map.put("token","");
        }
        map.put("ordercode",ordercode);
        map.put("paytype",paytype);
        map.put("paymoney",paymoney);
        map.put("is_balance_deductible", ThePosPay.is_balance_deductible);
        if(orderstyle==3){//年检订单传
            map.put("is_annual_inspection",1);
        }
        return map;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.weixin_pay_img://微信扫码支付
                payType(1);
                if (mjOpenType==3) {
                    weixin_pay_img.setClickable(false);
                    ali_pay_img.setClickable(true);
                }
                break;

            case R.id.ali_pay_img://支付宝扫码支付
                payType(2);
                if (mjOpenType==3) {
                    weixin_pay_img.setClickable(true);
                    ali_pay_img.setClickable(false);
                }
                break;
        }
    }

    /**
     * 订单支付(茂捷线上二维码通道)
     * */
    int expiredTime;//二维码过期时间
    String qrcode_url;
    public void getQrCodeUrl(HashMap map){

        UIHelper.showPd(ScanQrPayActivity.this);

        volleyUtil.StringRequestPostVolley(URLs.GET_QR_CODE_URL, EncryptUtil.encrypt(map), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                try {
                    JSONObject object=new JSONObject(EncryptUtil.decryptJson(jsonObject.toString(),ScanQrPayActivity.this));

                    String status=object.optString("status");
                    if(status.equals("ok")) {
                        JSONObject dataObj = object.optJSONObject("data");
                        if (dataObj.has("qrcode_text")) {
                            qrcode_url = dataObj.optString("qrcode_text");//二维码图片URL
                        }
                        if (dataObj.has("expiredTime")) {
                            expiredTime = dataObj.optInt("expiredTime") * 1000;//过期时间 单位：ms
                        }
                            if(qrcode_url!=null) {
                                Bitmap qrCodeBitmap = QrCodeUtil.generateBitmap(qrcode_url,width+10,width+10);
                                if(qrCodeBitmap!=null) {
                                    pay_qr_code_img.setImageBitmap(qrCodeBitmap);
                                }
                            }

                        expired_time_tv.setText("二维码有效期为" + expiredTime / 1000 / 60 + "分钟，过期后请刷新重试");
                    }else{
                        if(object.has("show_msg")){
                            Toast.makeText(ScanQrPayActivity.this,""+object.optString("show_msg"),Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(ScanQrPayActivity.this,"获取支付二维码异常，请稍候重试",Toast.LENGTH_LONG).show();
                        }
                        ScanQrPayActivity.this.finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    UIHelper.dismissPd();
                }
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                UIHelper.dismissPd();
                ScanQrPayActivity.this.finish();
                Toast.makeText(ScanQrPayActivity.this,"获取支付二维码异常，请稍候重试",Toast.LENGTH_LONG).show();
            }
        });
    }


    private HashMap getStatusParams(){
        map=new HashMap();
        if(token!=null) {
            map.put("token", token);
        }else{
            map.put("token", "");
        }
        map.put("ordercode",ordercode);
        return map;
    }


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            // TODO Auto-generated method stub
            // 要做的事情
            switch (msg.what) {
                case 1:
                    PayUtil.getPayStatus(ScanQrPayActivity.this, getStatusParams());
                    break;
            }
            super.handleMessage(msg);
        }
    };



    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... param) {
    }


    /**
     * 第一个参数表示要执行的任务，通常是网络的路径；第二个参数表示进度的刻度，第三个参数表示任务执行的返回结果
     */
    public class  GetQRCodeTask extends AsyncTask<String, Void, Bitmap> {
        /**
         * 表示任务执行之前的操作
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            UIHelper.showPd(ScanQrPayActivity.this);
        }

        /**
         * 主要是完成耗时的操作
         */
        @Override
        protected Bitmap doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            // 使用网络连接类HttpClient类王城对网络数据的提取

            Bitmap bitmap =  HttpUtil.getNetBitmap(arg0[0]);
            return bitmap;
        }

        /**
         * 主要是更新UI的操作
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result!=null) {
                pay_qr_code_img.setImageBitmap(result);
            }
            UIHelper.dismissPd();

            MyCount mc=new MyCount(expiredTime,1000);
            mc.start();
        }
    }


    class MyCount extends CountDownTimer{
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            payType(payInt);//二维码过期，自动刷新二维码？
        }
    }


    //文件存储根目录
    private String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }
}
