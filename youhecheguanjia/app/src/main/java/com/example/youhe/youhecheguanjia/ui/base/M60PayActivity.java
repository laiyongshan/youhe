package com.example.youhe.youhecheguanjia.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dynamicode.p27.companyyh.inter.DCSwiper;
import com.dynamicode.p27.companyyh.util.DCCharUtils;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.adapter.DeviceListAdapter;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.entity.base.SerMap;
import com.example.youhe.youhecheguanjia.utils.ParamSign;
import com.example.youhe.youhecheguanjia.utils.StringUtils;
import com.example.youhe.youhecheguanjia.utils.SystemBarUtil;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.jhl.jhlblueconn.BlueStateListenerCallback;
import com.jhl.jhlblueconn.BluetoothCommmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/7/25.
 * 使用M60机器支付的界面
 */

public class M60PayActivity extends AppCompatActivity implements View.OnClickListener,BlueStateListenerCallback {

    private static final String TAG = "NewJoinpayActivity";

    private String price="",theSerialNumber="",totalDegree="",ordercode="",sendSecretKey="";
    private int orderstyle;//订单类型
    private String info;//收款方
    private TextView shop_info_tv,zongjine_tv,info_tv;

    private LinearLayout item_pay_choose_device,item_pay_swiping_card,item_pay_enter_psw;

    private ProgressBar scan_progress;

    private ImageButton new_pay_back_ib,research_ib;

    private RelativeLayout main_pro_linear;
    private static String m_deviceAddress = "";

    private ListView devicemanager_listv;

    public static final String[] DEVICE_ADDRESS_FILETER = null;//new String[]{"liu","a60"}; //null;//;new String[]{""};搜索设备的时候的一个过滤器，为null时表示搜索所有类型
    BluetoothCommmanager BluetoothComm = null;
    private static final long WAIT_TIMEOUT = 4000; //超时时间  单位 毫秒

    private Handler mMainMessageHandler;

    private boolean bOpenDevice = false;

    private static final String DEBUG_TAG = "M60PayActivity";

    private ProgressDialog pd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_joinpay);

        // 4.4及以上版本开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarUtil.setTranslucentStatus(true,M60PayActivity.this);
        }
        SystemBarUtil.useSystemBarTint(M60PayActivity.this);


        Intent extraIntent=getIntent();

        price=extraIntent.getStringExtra("price");//支付金额
        theSerialNumber=extraIntent.getStringExtra("theSerialNumber");//机器序列号
        totalDegree=extraIntent.getStringExtra("totalDegree")+"";//总扣分
        ordercode=extraIntent.getStringExtra("ordercode")+"";
        orderstyle=extraIntent.getIntExtra("orderstyle",orderstyle);
        info=extraIntent.getStringExtra("info");
        sendSecretKey=extraIntent.getStringExtra("sendSecretKey")!=null?extraIntent.getStringExtra("sendSecretKey"):"";//工作密钥

        findViews();
        initView();


        //设置当前窗体回调((这个的时候自动启动服务 已经不需要启动了 ))
        BluetoothComm = BluetoothCommmanager.getInstance(this, this);


        mMainMessageHandler = new MessageHandler(Looper.myLooper());
        //回调搜索方式
        if (!bOpenDevice) {
            BluetoothComm.ScanDevice(DEVICE_ADDRESS_FILETER, 5, 0);
            if (!scan_progress.isShown()) {
                scan_progress.setVisibility(View.VISIBLE);
            }
        } else {
            showLogMessage("请先断开连接");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        item_pay_choose_device.setVisibility(View.VISIBLE);
        item_pay_enter_psw.setVisibility(View.GONE);
        item_pay_swiping_card.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭释放相关蓝牙资源
        BluetoothComm.closeResource();
    }


    /**
     * 通过ID查找所有控件
     */
    private void findViews(){

        pd=new ProgressDialog(M60PayActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("蓝牙连接中...");

        main_pro_linear = (RelativeLayout) findViewById(R.id.main_pro_linear);
        devicemanager_listv= (ListView) findViewById(R.id.device_lv);

        new_pay_back_ib= (ImageButton) findViewById(R.id.new_pay_back_ib);
        new_pay_back_ib.setOnClickListener(this);

        item_pay_choose_device= (LinearLayout) findViewById(R.id.item_pay_choose_device);
        item_pay_swiping_card= (LinearLayout) findViewById(R.id.item_pay_swiping_card);
        item_pay_enter_psw= (LinearLayout) findViewById(R.id.item_pay_enter_psw);

        research_ib= (ImageButton) findViewById(R.id.research_ib);
        research_ib.setOnClickListener(this);

        scan_progress = (ProgressBar) findViewById(R.id.scan_progress);

        shop_info_tv= (TextView) findViewById(R.id.shop_info_tv);
        zongjine_tv= (TextView) findViewById(R.id.zongjine_tv);

        info_tv=(TextView)findViewById(R.id.info_tv);
    }

    /**
     * 初始化所有控件
     * */
    private void initView(){

        shop_info_tv.setText(totalDegree);
        zongjine_tv.setText("￥"+price);
        zongjine_tv.setTextColor(Color.RED);

        info_tv.setText(info+"");

        scan_progress.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.new_pay_back_ib:
                finish();
                break;

            case R.id.research_ib:
                //回调搜索方式
                if (!bOpenDevice) {
                    BluetoothComm.ScanDevice(DEVICE_ADDRESS_FILETER, 4, 0);

                    if (!scan_progress.isShown()) {
                        scan_progress.setVisibility(View.VISIBLE);
                    }

                } else {
                    showLogMessage("请先断开连接");
                    if (scan_progress.isShown()) {
                        scan_progress.setVisibility(View.GONE);
                    }
                }
                break;
        }

    }


    @Override
    public void onDeviceInfo(Map<String, String> info) {

//        showLogMessage("设备信息:");
//
//        Set<?> set = info.entrySet();
//        Iterator<?> iterator = set.iterator();
//        while (iterator.hasNext()) {
//            @SuppressWarnings("unchecked")
//            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) iterator.next();
//            showLogMessage(entry1.getKey() + "==" + entry1.getValue());
//            Log.i(DEBUG_TAG, entry1.getKey() + "==" + entry1.getValue());
//        }

        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.what=0x93;
        updateMessage.sendToTarget();

        /********************************************************************
         函 数 名：SwipeCard
         功能描述：蓝牙设备上输提 刷卡      无输入金额  无密码（例如信用卡预授权完成等交易）
         入口参数：
         long 	timeout 		--刷卡交易超时时间(毫秒)
         long   lAmount         ---交易金额,如果需要自己传入金额,赋值进入即可(如需要MPOS填写默认0即可)
         返回说明：
         **********************************************************/
        BluetoothComm.SwipeCard(WAIT_TIMEOUT, (long) (Float.valueOf(price)*100));//开始刷卡
        Log.i(DEBUG_TAG,"BluetoothComm.SwipeCard(WAIT_TIMEOUT, 10000);//开始刷卡");

    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onError(int nResult, String MsgData) {
        // TODO Auto-generated method stub
        String nReusl = String.format("%02x", nResult);
        showLogMessage("错误提示:" + nReusl + ":" + MsgData);

        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.obj = "";
        updateMessage.what = 0x94;
        updateMessage.sendToTarget();
    }


    HashMap map=new HashMap();
    private HashMap getParamter(){
        map.put("token", TokenSQLUtils.check());//Token值
        map.put("poscode",theSerialNumber);//违章机序列号
        map.put("ordercode",ordercode);//订单编号
        map.put("paymoney",price);//支付金额（）
        if(orderstyle==3){//年检订单传
            map.put("is_annual_inspection", 1);
        }
        return map;
    }

    private Map<String, String> mapcard = new HashMap<String, String>();
    @Override
    public void onReadCardData(Map hashcard) {
        // TODO Auto-generated method stub
        showLogMessage("加密信息：");
        mapcard = (Map<String, String>) hashcard;
        Set<?> set = hashcard.entrySet();
        Iterator<?> iterator = set.iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) iterator.next();

            if(entry1.getKey().equals("PAN")){//卡账号
                map.put("mainaccount",entry1.getValue());
            }else if(entry1.getKey().equals("ExpireDate")){//卡有效期
                map.put("cardvalidity",entry1.getValue().toString());
            }else if(entry1.getKey().equals("CardType")){//卡类型 ==0磁条卡   其他:IC卡
                if(entry1.getValue().equals("0")){//磁条卡
                    map.put("cardtype","2");
                }else if(entry1.getValue().equals("1")){//ic卡
                    map.put("cardtype","1");
                }else if(entry1.getValue().equals("2")){//非接
                    map.put("cardtype","3");
                }
            }else if(entry1.getKey().equals("Track55")){//55域
                map.put("iccardinfo",entry1.getValue());
            }else if(entry1.getKey().equals("Encrytrack2")){//2磁道
                map.put("trackInfo2",entry1.getValue());
            }else if(entry1.getKey().equals("Encrytrack3")&&entry1.getValue()!=null&&(!entry1.getValue().trim().toString().equals(""))){//3磁道
                map.put("trackInfo3",entry1.getValue().toString());
            }else if(entry1.getKey().equals("Pinblock")){
                map.put("passwordpin", ParamSign.getUserPassword(entry1.getValue()));
            }else if(entry1.getKey().equals("PanSeqNo")){//卡序列号
                map.put("cardserialnumber",entry1.getValue().toString());
            }

            showLogMessage(entry1.getKey() + "==" + entry1.getValue());
            Log.e(DEBUG_TAG, entry1.getKey() + "==" + entry1.getValue());
        }

        Intent intent=new Intent(M60PayActivity.this,SignInActivity.class);
        //让hashmap实现可序列化,定义一个实现可序列化的类。
        SerMap serMap=new SerMap();
        serMap.setMap(getParamter());
        //创建Bundle对象，存放实现可序列化的SerMap
        Bundle bundle=new Bundle();
        bundle.putSerializable("serMap",serMap);
        //意图放置bundle变量
        intent.putExtras(bundle);
        startActivity(intent);
        M60PayActivity.this.finish();

    }

    @Override
    public void onDeviceFound(ArrayList<BluetoothIBridgeDevice> mDevices) {
        if (Build.VERSION.SDK_INT > 10) {
            if (!isDestroyed()) {
                ArrayList<BluetoothIBridgeDevice> ListName = new ArrayList<BluetoothIBridgeDevice>();
                if (mDevices.size() == 0) {
                    Message updateMessage = mMainMessageHandler.obtainMessage();
                    updateMessage.what = 0x97;
                    updateMessage.sendToTarget();
                    return;
                }

                synchronized (mDevices) {
                    for (BluetoothIBridgeDevice device : mDevices) {
                        String map;
                        if (device.getDeviceName() != null) {
                            map = device.getDeviceName() + "=" + device.getDeviceAddress();
//                            etTheserialNumber.setText(device.getDeviceAddress()+"");
                        } else {
                            map = "unknown" + "=" + device.getDeviceAddress();
                        }
                        System.out.println(map);
                        ListName.add(device);
                    }

                    //弹出选择对话框
                    if (mDevices.size() == 0) {
                        Message updateMessage = mMainMessageHandler.obtainMessage();
                        updateMessage.what = 0x97;
                        updateMessage.sendToTarget();
                        return;
                    }

                    Message updateMessage = mMainMessageHandler.obtainMessage();
                    updateMessage.obj = ListName;
                    updateMessage.what = 0x99;
                    updateMessage.sendToTarget();
                }
            }
        } else {
            Log.d("onDeviceFound", "is Destroyed");
        }
    }

    @Override
    public void onLoadMasterKeySucc(Boolean aBoolean) {

    }

    @Override
    public void onLoadWorkKeySucc(Boolean aBoolean) {

    }

    @Override
    public void getMacSucess(String s) {

    }

    @Override
    public void getBatSucess(Boolean aBoolean, String s) {

    }

    @Override
    public void swipCardSucess(String cardNumber) {
        // TODO Auto-generated method stub
        showLogMessage("PAN:" + cardNumber.toString());
        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.obj = "";
        updateMessage.what = 0x98;
        updateMessage.sendToTarget();
    }

    @Override
    public void onBluetoothIng() {

    }

    @Override
    public void onBluetoothConected() {
//        Message updateMessage = mMainMessageHandler.obtainMessage();
//        updateMessage.obj = "连接蓝牙成功,正在获取SN号...";
//        updateMessage.what=0x95;
//        updateMessage.sendToTarget();

        showLogMessage("连接蓝牙成功,正在获取SN号...");
        bOpenDevice = true;
        byte[] sendBuf = StringUtils.hexStr2Bytes(sendSecretKey);
        int len = sendSecretKey.length();
        Log.i("TAG","sendSecretKey.length() is:"+len);
        int b=BluetoothComm.WriteWorkKey(sendBuf);
        Log.i("TAG","BluetoothComm.WriteWorkKey(sendBuf) is:"+b);
        if(b==0) {
            BluetoothComm.GetDeviceInfo();
        }else{

        }
    }

    @Override
    public void onBluetoothConectedFail() {
        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.obj = "连接蓝牙设备失败...";
        updateMessage.what=0x96;
        updateMessage.sendToTarget();
        showLogMessage("连接蓝牙设备失败...");
    }

    @Override
    public void onBluetoothDisconnected() {
        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.obj = "蓝牙已断开,请重新连接...";
        updateMessage.what=0x96;
        updateMessage.sendToTarget();
        showLogMessage("蓝牙已断开,请重新连接...");
        bOpenDevice = false;
    }

    @Override
    public void onScanTimeout() {
        showLogMessage("搜索超时.");
    }

    @Override
    public void onBluetoothPowerOff() {
        showLogMessage("蓝牙关闭.");
    }

    @Override
    public void onBluetoothPowerOn() {
        showLogMessage("蓝牙开启.");
    }

    @Override
    public void onWaitingForCardSwipe() {
        showLogMessage("请刷卡/插卡/挥卡...");

        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.what=0x95;
        updateMessage.sendToTarget();
    }

    @Override
    public void onDetectIC() {
        showLogMessage("IC卡插入...");
    }


    class MessageHandler extends Handler {
        private long mLogCount = 0;

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 0x99:
                    final ArrayList<BluetoothIBridgeDevice> mDevices = (ArrayList<BluetoothIBridgeDevice>) msg.obj;
                    final String[] items = new String[mDevices.size()];

                    for (int i = 0; i < mDevices.size(); i++) {
                        items[i] = mDevices.get(i).getDeviceName();
                    }
                    new AlertDialog.Builder(M60PayActivity.this)
                            .setTitle("请选择蓝牙设备")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(items, 0,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            //时间
                                            long end = System.currentTimeMillis();
                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss:SSS");
                                            String hms1 = formatter.format(end);
                                            //显示
//                                            showLogMessage("T1== " + hms1);
//                                            showLogMessage("正在连接:" + mDevices.get(which).getDeviceName() + "==" + mDevices.get(which).getDeviceAddress());

                                            if(pd!=null){
                                                pd.show();
                                            }

                                            Log.i("TAG",mDevices.get(which).getDeviceName());
                                            if(mDevices.get(which).getDeviceName().trim().equals(theSerialNumber.trim())) {
                                                BluetoothComm.ConnectDevice(mDevices.get(which).getDeviceAddress());
                                            }else{
                                                Toast.makeText(M60PayActivity.this, "请使用用户账号注册时所绑定的机器进行支付！", Toast.LENGTH_LONG).show();
                                                if(pd!=null&&pd.isShowing()){
                                                    pd.dismiss();
                                                }
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton("取消",  /*null*/new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    dialog.cancel();
                                    //回调搜索方式
                                    if (!bOpenDevice) {
                                        BluetoothComm.ScanDevice(DEVICE_ADDRESS_FILETER, 5, 1);
                                        if (!scan_progress.isShown()) {
                                            scan_progress.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        showLogMessage("请先断开连接");
                                    }
                                }
                            })
                            .create()
                            .show();//AlertDialog.Builder.create().show()相当于 Dialog.show()
                    break;

                case 0x98:
                    item_pay_swiping_card.setVisibility(View.GONE);
                    item_pay_enter_psw.setVisibility(View.VISIBLE);
                    break;

                case 0x97:
                    if (scan_progress.isShown()) {
                        scan_progress.setVisibility(View.GONE);
                    }
                    Toast.makeText(M60PayActivity.this, "查找无设备，请确认设备是否打开蓝牙，然后重试", Toast.LENGTH_SHORT).show();
                    break;

                case 0x96:
                    if(pd.isShowing()){
                        pd.dismiss();
                    }
                    Log.i(DEBUG_TAG,msg.obj.toString()+"");
                    break;

                case 0x95:
                    if(pd.isShowing()){
                        pd.dismiss();
                    }
                    item_pay_choose_device.setVisibility(View.GONE);
                    item_pay_swiping_card.setVisibility(View.VISIBLE);
                    item_pay_enter_psw.setVisibility(View.GONE);
                    if (scan_progress.isShown()) {
                        scan_progress.setVisibility(View.GONE);
                    }
                    break;

                case 0x94:
                    M60PayActivity.this.finish();
                    break;

                case 0x93:
                    if(pd!=null) {
                        pd.show();
                    }
                    break;
            }

        }
    }

    public void showLogMessage(String msg) {
        Message updateMessage = mMainMessageHandler.obtainMessage();
        updateMessage.obj = msg;
        updateMessage.what=0x96;
        updateMessage.sendToTarget();
    }


}
