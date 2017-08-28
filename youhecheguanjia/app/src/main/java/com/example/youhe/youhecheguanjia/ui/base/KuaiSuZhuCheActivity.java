package com.example.youhe.youhecheguanjia.ui.base;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.app.AppContext;
import com.example.youhe.youhecheguanjia.app.CommentSetting;
import com.example.youhe.youhecheguanjia.bean.FirstEvent;
import com.example.youhe.youhecheguanjia.biz.GetMasterKey;
import com.example.youhe.youhecheguanjia.biz.SaveNameDao;
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
import com.example.youhe.youhecheguanjia.utils.ParamSign;
import com.example.youhe.youhecheguanjia.utils.SystemBarUtil;
import com.example.youhe.youhecheguanjia.utils.TripleDES;
import com.example.youhe.youhecheguanjia.utils.UIHelper;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;
import com.example.youhe.youhecheguanjia.widget.ClearEditText;
import com.example.youhe.youhecheguanjia.widget.TimeButton;
import com.example.youhe.youhecheguanjia.widget.ToastUtil;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.jhl.jhlblueconn.BlueStateListenerCallback;
import com.jhl.jhlblueconn.BluetoothCommmanager;
import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.pinpad.AidlPinpad;
import com.lkl.cloudpos.aidl.system.AidlSystem;
import com.lkl.cloudpos.data.PinpadConstant;
import com.thepos.biz.ui.base.GainSerialNumberActivity2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 手机注册登录面
 */
public class KuaiSuZhuCheActivity extends AppCompatActivity implements View.OnClickListener,BlueStateListenerCallback {
    private TimeButton timeButton;//获取验证码
    private ClearEditText etTheserialPassword1,etTheserialPassword2,etTheserialMobile,id_car_num_et,name_et2;
    private EditText etTheserialVerification;

    private Button bt_theserialnumber;//获取机器序列号
    private TextView register_tv;//注册按钮

    private TextView version_code_tv;

    private String poscode,mobile,password,password2,verifycode,IdCard,name;
    private String sheng,shi;
    private SaveNameDao saveNameDao;
    private VolleyUtil volleyUtil;//数据请求
    private StatusSQLUtils statusSQLUtils;//登录状态
    private TokenSQLUtils tsu;//Token
    private UIDialog uidialog;
    private TextView etTheserialNumber;
    public  String LKL_SERVICE_ACTION = "lkl_cloudpos_device_service";//服务地址
    private AidlSystem systemInf = null;
    private GetMasterKey getMasterKey;
    private AidlPinpad pinpad = null; // 密码键盘接口,用来灌入主密钥

    public static boolean isGetverifycode=true;//是否获取验证码

    public PosCodeBrocast posCodeBrocast;//mpos设备序列号广播接受者
    private final String POSCODE_ACTION="POSCODE_ACTION_";
    private IntentFilter intentFilter;

    private TextView to_login_tv;//去登录

    private AppCompatSpinner spinner_role;
    private ArrayAdapter spinnerAdapter = null;
    private ArrayList<String> spinnerData = new ArrayList<>();
    private int postype=1;//设备型号 1：P8 && 2：M60_A
    private Handler mMainMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shou_jji_den_lu);

        // 4.4及以上版本开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            SystemBarUtil.setTranslucentStatus(true,KuaiSuZhuCheActivity.this);
        }
        SystemBarUtil.useSystemBarTint(KuaiSuZhuCheActivity.this);
        EventBus.getDefault().register(this);
        posCodeBrocast=new PosCodeBrocast();
        intentFilter= new IntentFilter();
        intentFilter.addAction(POSCODE_ACTION);

        timeButton(savedInstanceState);

        in();

        //设置当前窗体回调((这个的时候自动启动服务 已经不需要启动了 ))
        BluetoothComm = BluetoothCommmanager.getInstance(this, this);
        mMainMessageHandler = new MessageHandler(Looper.myLooper());

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(posCodeBrocast,intentFilter);
    }


    private void in() {

        tsu = new TokenSQLUtils(this);
        statusSQLUtils = new StatusSQLUtils(this);
        saveNameDao = new SaveNameDao(this);

        etTheserialNumber = (TextView) findViewById(R.id.et_theserialnumber);//设备序列号
        etTheserialVerification = (EditText) findViewById(R.id.et_yanzhenmima);//验证码
        etTheserialPassword1 = (ClearEditText) findViewById(R.id.et_mima);//密码
        etTheserialPassword2= (ClearEditText) findViewById(R.id.onceagain_et);//第二次密码
        etTheserialMobile= (ClearEditText) findViewById(R.id.et_shouji);//手机号码
        id_car_num_et = (ClearEditText) findViewById(R.id.id_car_num_et);//获取身分证
        name_et2 = (ClearEditText) findViewById(R.id.name_et2);//获取姓名
        volleyUtil = VolleyUtil.getVolleyUtil(this);//上网请求
        param = new HashMap<>();
        uidialog = new UIDialog(this,"正在登录.......");

        register_tv= (TextView) findViewById(R.id.register_tv);//注册
        register_tv.setOnClickListener(this);

        bt_theserialnumber= (Button) findViewById(R.id.bt_theserialnumber);
        bt_theserialnumber.setOnClickListener(this);

        to_login_tv= (TextView) findViewById(R.id.to_login_tv);
        to_login_tv.setOnClickListener(this);

        spinner_role= (AppCompatSpinner) findViewById(R.id.spinner_role);
        spinnerData.add("选择设备类型");
        spinnerData.add("P8");
        spinnerData.add("P92");
        spinnerData.add("M60-A");
        spinnerAdapter=new ArrayAdapter<String>(KuaiSuZhuCheActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerData);
        spinner_role.setAdapter(spinnerAdapter);

        initLBS();//定位
        event();
    }


    private void event() {
        spinner_role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                postype=i;//1:P8   2:M60
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    /**
     * 获取mpost的序列号
     * @param event
     */
    @Subscribe
    public void onEventMainThread(FirstEvent event) {
        TextView textView = (TextView) findViewById(R.id.et_theserialnumber);
        if(event.getMsg().startsWith("YH")) {
            textView.setText(event.getMsg());
            param.put("poscode", event.getMsg());//mpos设备序列号
        }
    }

    /**
     * 获取验证码
     * @param savedInstanceState
     */
    private void timeButton(Bundle savedInstanceState) {

        //获取验证码键
        timeButton = (TimeButton) findViewById(R.id.time_btn);
        timeButton.onCreate(savedInstanceState);
        timeButton.setTextAfter("秒后重新获取").setTextBefore("点击获取验证码").setLenght(60 * 1000);
        timeButton.setOnClickListener(this);

    }

    /**
     * 返回键
     *
     */
    public void fanhui(View view){
        finish();
    }


    /**
     * @param view
     */
    private HashMap<String,String> phonePams;
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.time_btn:
                getetVerifycode();
                break;

            case R.id.bt_theserialnumber:
                if(postype!=0) {
                    if(postype==1) {//p8
                        theserialNumber();
                    }else if(postype==2){//p92

                    }else if(postype==3){//m60-A
                        searchDevice();
                    }
                }else{
                    Toast.makeText(KuaiSuZhuCheActivity.this,"请先选择设备类型",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.register_tv:

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    //隐藏键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }

                register();
                break;

            case R.id.to_login_tv:
                Intent i=new Intent(KuaiSuZhuCheActivity.this,DenLuActivity.class);
                startActivity(i);
                finish();
                break;
        }

    }

    /**
     * 获取验证码
     * */
    public void getetVerifycode(){
        if (MainActivity.getHtts()==false){
            Toast.makeText(this,"网络连接失败，请检测设置",Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = etTheserialMobile.getText().toString();

        isGetverifycode=false;
        if(etTheserialNumber.getText().toString().equals("")||etTheserialNumber.getText().toString()==null){
            Toast.makeText(this,"请先获取机器序列号",Toast.LENGTH_LONG).show();
            isGetverifycode=false;
        }else {
            if (phoneNumber.length()<11) {
                Toast.makeText(this, "手机号码不能低于11位",Toast.LENGTH_LONG).show();
                isGetverifycode=false;
            } else {

                isGetverifycode=true;

                phonePams = new HashMap<>();
                phonePams.put("mobile", phoneNumber);
                phonePams.put("poscode", etTheserialNumber.getText().toString());
                volleyUtil.StringRequestPostVolley(URLs.PHONEVERIFICATIONCODE, EncryptUtil.encrypt(phonePams), new VolleyInterface() {
                    @Override
                    public void ResponseResult(Object jsonObject) {
                        Log.i("TAG","获取注册验证码："+jsonObject.toString());
                        EncryptUtil.decryptJson(jsonObject.toString(),KuaiSuZhuCheActivity.this);
                    }

                    @Override
                    public void ResponError(VolleyError volleyError) {
                        Toast.makeText(KuaiSuZhuCheActivity.this, "获取验证码失败，请重试！",Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    /**
     * 注册请求
     * */
    public void register(){
        if (MainActivity.getHtts()==false){
            Toast.makeText(KuaiSuZhuCheActivity.this,"网络连接失败，请检测设置",Toast.LENGTH_LONG).show();
            return;
        }

        String  judgeMachineType = android.os.Build.MODEL;

        //判断机器机型
        if(judgeMachineType.equals("P92")){//智能POS支付
            poscode = etTheserialNumber.getText().toString();//智能post设备序列号
            param.put("poscode",poscode);

        }

        verifycode = etTheserialVerification.getText().toString();//验证码
        password = etTheserialPassword1.getText().toString();//第一次密码
        password2 = etTheserialPassword2.getText().toString();//第二次密码
        mobile = etTheserialMobile.getText().toString();//手机号码
        IdCard  = id_car_num_et.getText().toString();//获取身份证
        name = name_et2.getText().toString();//得到名字

        if(password.length()>=6&&password2.length()>=6){
            if(verifycode.equals("")||password.equals("")||password2.equals("")||mobile.equals("")||IdCard.equals("")||name.equals("")) {
                Toast.makeText(KuaiSuZhuCheActivity.this,"请填写完整信息",Toast.LENGTH_LONG).show();
            }else if(!password.equals(password2)){
                Toast.makeText(KuaiSuZhuCheActivity.this,"两次密码输入不一致",Toast.LENGTH_LONG).show();
            }else{
                if(etTheserialNumber.getText().toString().equals("")||etTheserialNumber.getText().toString()==null){
                    Toast.makeText(KuaiSuZhuCheActivity.this,"请先获取机器序列号",Toast.LENGTH_LONG).show();
                }else {
                    if(!password.equals(password2)){
                        Toast.makeText(KuaiSuZhuCheActivity.this,"两次密码输入不一致",Toast.LENGTH_SHORT).show();
                    }else{
                        requestString();
                    }
                }
            }
        }else {
            Toast.makeText(KuaiSuZhuCheActivity.this,"密码长度不能少于6",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 跳转到获取设备序列号界面
     */
    public void theserialNumber(){
        Intent intent = new Intent(this,GainSerialNumberActivity2.class);
        startActivityForResult(intent,1000);
        this.overridePendingTransition(R.anim.in_from_right,
                R.anim.out_from_left);
    }

    /**
     * 搜索M60设备
     * */
    private boolean bOpenDevice = false;
    public static final String[] DEVICE_ADDRESS_FILETER = null;
    BluetoothCommmanager BluetoothComm = null;
    public void searchDevice(){
        UIHelper.showPd(KuaiSuZhuCheActivity.this);
        if (!bOpenDevice) {
            BluetoothComm.ScanDevice(DEVICE_ADDRESS_FILETER, 5, 0);
        }else {
            Toast.makeText(KuaiSuZhuCheActivity.this,"请先断开连接",Toast.LENGTH_LONG).show();
        }
    }


    private static final String TAG="WU";
    /**
     * 设别服务连接桥
     */
    private ServiceConnection conn = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            Log.i(TAG, "aidlService服务连接成功");
            if(serviceBinder != null){	//绑定成功
                AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
                try {
                    systemInf = AidlSystem.Stub.asInterface(serviceManager
                            .getSystemService());
                    try {
                        String terminalSn = systemInf.getSerialNo();//获取终端序列号
                        etTheserialNumber.setText(terminalSn);
                        //初始化密码键盘
                        pinpad = AidlPinpad.Stub.asInterface(serviceManager.getPinPad(PinpadConstant.PinpadId.BUILTIN));
                        getMasterKey = new GetMasterKey(KuaiSuZhuCheActivity.this,pinpad,terminalSn);
                        getMasterKey.httpSign();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "AidlService服务断开了");
        }
    };


    /**
     * 绑定服务
     */
    public void indService(){
        Intent intent = new Intent();
        intent.setAction(LKL_SERVICE_ACTION);
        boolean flag = this.bindService(intent, conn, this.BIND_AUTO_CREATE);//绑定服务
        if (flag) {
            Log.i(TAG, "服务绑定成功");
        } else {
            Log.i(TAG, "服务绑定失败");
        }
    }

    /**
     * 重新进去界面时
     * 绑定服务
     */
    @Override
    protected void onResume() {
        super.onResume();
//        String  judgeMachineType = android.os.Build.MODEL;
//        if (judgeMachineType.equals("P92")){
//            findViewById(R.id.bt_theserialnumber).setVisibility(View.GONE);
//            indService();//绑定服务
//        }else {
//
//        }
    }

    /**
     * 暂停时
     * 解绑服务
     */
    @Override
    protected void onPause() {
        super.onPause();

//        String  judgeMachineType = android.os.Build.MODEL;
//        if (judgeMachineType.equals("P92")){
//            if(conn != null){
//                this.unbindService(conn);
//            }
//        }else {
//        }
    }

    //获取参数
    private HashMap getParams(){

        param=new HashMap();

        param.put("postype",postype);//设备类型
        param.put("poscode",etTheserialNumber.getText().toString());//设备序列号
        param.put("mobile", mobile);//手机号码
        param.put("verifycode", verifycode);
        param.put("password", ParamSign.getUserPassword(password));
        param.put("password2", ParamSign.getUserPassword(password2));
        param.put("identitycard",IdCard);//身份证
        param.put("clientname",name);//用户名
        param.put("province",sheng+"");//省
        param.put("city",shi+"");//城市


        return param;

    }


    private HashMap param;
    //注册请求
    public void requestString() {
        uidialog.show();

        if (MainActivity.getHtts()==false){
            Toast.makeText(this,"网络连接失败，请检测设置",Toast.LENGTH_LONG).show();
            return;
        }

        volleyUtil.StringRequestPostVolley(URLs.REGISTER_URL, EncryptUtil.encrypt(getParams()), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                Log.i("TAG","注册返回的数据："+jsonObject.toString());
                uidialog.hide();

                String josn = EncryptUtil.decryptJson(jsonObject.toString(),KuaiSuZhuCheActivity.this);
                String  judgeMachineType = android.os.Build.MODEL;
                //如果是P92就需要判断主密钥导入是否成功
                if (judgeMachineType.equals("P92")  ){
                    if (getMasterKey.getIntoTheState()){
                        estimate(josn);
                    }else {
                        ToastUtil.getLongToastByString(KuaiSuZhuCheActivity.this,"机器资源准备失败，请按左侧下方重置按钮！");
                    }

                }else {
                    estimate(josn);;//解析解密之后的数据
                }
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                ToastUtil.getShortToastByString(KuaiSuZhuCheActivity.this,"连接错误");
                uidialog.hide();
            }
        });
    }

    /**
     * 注册判断
     * @param
     */
    public void estimate(String josn) {
        String  status = "";
        JSONObject jsonObject =null;
        try {
                jsonObject = new JSONObject(josn);
                status = jsonObject.getString("status");
                if(status.equals("ok")) {//如果是请求正确
                    JSONObject jsonObject2 = null;

                    Toast.makeText(this, "注册成功",Toast.LENGTH_LONG).show();

                    HashMap params = new HashMap();
                    Task ts = new Task(TaskType.TS_REFLUSH_HOME_PAGE, params);//登录成功后更新首页数据
                    MainService.newTask(ts);
                    EventBus.getDefault().post(new FirstEvent("ok"));
                    jsonObject2 = jsonObject.getJSONObject("data");
                    String token = jsonObject2.getString("token");
                    volleyUtil.cancelAllQueue(this);
                    saveNameDao.writeTxtToFile("", "poscode.txt");
                    statusSQLUtils.addDate("yes");//注册成功后设置为登录状态
                    tsu.addDate(token);//注册成功保存token
                    saveNameDao.writeTxtToFile(mobile, "phonenumbe.txt");

                    Toast.makeText(KuaiSuZhuCheActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    finish();

                } else if(status.equals("fail")){//如果请求错误
//                    Misidentification.misidentification1(this,status,jsonObject);
                    if(jsonObject.has("code")){
                        int code=jsonObject.getInt("code");
                        UIHelper.showErrTips(code,KuaiSuZhuCheActivity.this);
                    }

                    if(jsonObject.has("show_msg")){
                       Toast.makeText(KuaiSuZhuCheActivity.this,"注册失败："+jsonObject.optString("show_msg"),Toast.LENGTH_LONG).show();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    /**
     * 跳转到免责声明界面
     * @param view
     */
    public void click(View view){
        if (MainActivity.getHtts()==false){
            ToastUtil.getLongToastByString(this,"网络连接失败，请检测设置");
            return;
        }
        Intent intent = new Intent(this,DisclaimerActivity.class);
        intent.putExtra("mianzhe",URLs.THE_MANUAL);
        intent.putExtra("title","服务使用协议");
        startActivity(intent);
    }


    private LocationClient mClient;
    private LocationClientOption mOption;
    private String mLBSAddress;
    private boolean mLBSIsReceiver;
    private AppContext application;
    //初始化地位服务
    private void initLBS() {
        application = new AppContext();
        mOption = new LocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setOpenGps(true);
        mOption.setCoorType("bd09ll");
        mOption.setAddrType("all");
        mOption.setScanSpan(1000);
        mOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mClient = new LocationClient(getApplicationContext(), mOption);
        mClient.setLocOption(mOption);
        mClient.start();
        mLBSIsReceiver = true;
        mClient.requestLocation();
        mClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation arg0) {
                mLBSAddress = arg0.getAddrStr();
                application.mLocation = arg0.getAddrStr();
                if(arg0.getCity()!=null) {
                    sheng =arg0.getProvince().replace("省", "");
                    shi = arg0.getCity().replace("市", "");
                    Log.i("WU",sheng+shi);

                }
                Log.i("TAG","当前位置为："+application.getLocalCity()+arg0.getAddrStr());
                Log.i("TAG","当前位置城市为："+application.getLocalCity());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOption.setOpenGps(false);
        mClient.stop();
        unregisterReceiver(posCodeBrocast);
        //关闭释放相关蓝牙资源
        BluetoothComm.closeResource();
    }




    //mpost设备序列号广播接收者
    class PosCodeBrocast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null) {
                String poscode = intent.getStringExtra("poscode");
                if(poscode!=null) {
                    etTheserialNumber.setText(poscode);
                    param.put("poscode", poscode);//mpost设备序列号
                }
            }
        }
    }


    @Override
    public void onDeviceInfo(Map<String, String> info) {
        Set<?> set = info.entrySet();
        Iterator<?> iterator = set.iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) iterator.next();
            Log.i("TAG", entry1.getKey() + "==" + entry1.getValue());
        }
    }

    @Override
    public void onTimeout() {
        Toast.makeText(KuaiSuZhuCheActivity.this,"连接超时" +
                "" +
                "" +
                "，请重试",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(int i, String s) {

    }

    @Override
    public void onReadCardData(Map map) {

    }

    @Override
    public void onDeviceFound(ArrayList<BluetoothIBridgeDevice> mDevices) {
        UIHelper.dismissPd();
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
                                Message updateMessage = mMainMessageHandler.obtainMessage();
                                updateMessage.obj = device.getDeviceName();
                                updateMessage.what = 0x98;
                                updateMessage.sendToTarget();
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
//                        Toast.makeText(KuaiSuZhuCheActivity.this, "查找无设备，请重试", Toast.LENGTH_SHORT).show();
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
    public void swipCardSucess(String s) {

    }

    @Override
    public void onBluetoothIng() {

    }

    @Override
    public void onBluetoothConected() {
        bOpenDevice = true;
//        BluetoothComm.GetDeviceInfo();
        Log.i("TAG","连接成功！！！！");
    }

    @Override
    public void onBluetoothConectedFail() {
        Log.i("TAG","连接失败！！！！");
    }

    @Override
    public void onBluetoothDisconnected() {
        bOpenDevice = false;
        Log.i("TAG","连接断开连接！！！！");
    }

    @Override
    public void onScanTimeout() {
        Toast.makeText(KuaiSuZhuCheActivity.this,"查找无设备,请确认设备是否打开，然后重试",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBluetoothPowerOff() {

    }

    @Override
    public void onBluetoothPowerOn() {

    }

    @Override
    public void onWaitingForCardSwipe() {

    }

    @Override
    public void onDetectIC() {

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

                case 0x97:
                    Toast.makeText(KuaiSuZhuCheActivity.this,"查找无设备,请确认设备是否打开，然后重试",Toast.LENGTH_SHORT).show();
                    break;

                case 0x98:
                    UIHelper.dismissPd();
                    etTheserialNumber.setText(msg.obj.toString()+"");
                    BluetoothComm.DisConnectBlueDevice();
                    break;

                case 0x99:
                    final ArrayList<BluetoothIBridgeDevice> mDevices = (ArrayList<BluetoothIBridgeDevice>) msg.obj;
                    final String[] items = new String[mDevices.size()];

                    for (int i = 0; i < mDevices.size(); i++) {
                        items[i] = mDevices.get(i).getDeviceName();
                    }
                    new AlertDialog.Builder(KuaiSuZhuCheActivity.this)
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
//                                            showLogMessage("正在连接:" + mDevices.get(which).getDeviceName() + "==" + mDevices.get(which).getDeviceAddress());
                                            Log.i("TAG",mDevices.get(which).getDeviceName()+":mDevices.get(which).getDeviceAddress()");
                                            etTheserialNumber.setText(mDevices.get(which).getDeviceName());
//                                            BluetoothComm.ConnectDevice(mDevices.get(which).getDeviceAddress());

                                        }
                                    }
                            )
                            .setNegativeButton("取消",  /*null*/new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    dialog.cancel();

                                }
                            })
                            .create()
                            .show();//AlertDialog.Builder.create().show()相当于 Dialog.show()


                    break;

            }

        }
    }

}
