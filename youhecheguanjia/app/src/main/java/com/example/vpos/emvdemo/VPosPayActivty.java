package com.example.vpos.emvdemo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;

import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.entity.base.SerMap;
import com.example.youhe.youhecheguanjia.ui.base.M60PayActivity;
import com.example.youhe.youhecheguanjia.utils.StringUtils;
import com.qsposcom.SerialPortNative;

import vpos.apipackage.APDU_RESP;
import vpos.apipackage.APDU_SEND;
import vpos.apipackage.Icc;
import vpos.apipackage.Mcr;
import vpos.apipackage.Picc;
import vpos.apipackage.Sys;
import vpos.emvkernel.EMVCAPK;
import vpos.emvkernel.EMV_APPLIST;
import vpos.emvkernel.EMV_PARAM;
import vpos.emvkernel.EmvKernel;
import vpos.util.ByteUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VPosPayActivty extends Activity {

    public String tag = "EmvActivity";
    Picc Picc = null;
    Icc Icc = null;
    int iret;

    //	private static final String FILEPATH="/extsd/solomon.com.testcinvakejava/";
    private static final String FILEPATH="/data/data/com.example.youhe.youhecheguanjia/";
    //private static final String FILEPATH="/data/wwan/solomon.com.testcinvakejava/";
    //private static final String FILEPATH="/mnt/sdcard/data/solomon.com.testcinvakejava/";
    //private static final String FILEPATH="/mnt/sdcard/solomon.com.testcinvakejava/";
    private File destDir;

    private ImageButton vpos_back_ib;
    private TextView shop_info_tv,zongjine_tv,info_tv;

    private String price="",theSerialNumber="",totalDegree="",ordercode="";
    private int orderstyle;//订单类型
    private String info;//收款方

    private long Amount;//授权金额

    Check_Thread check_thread=null;//检卡子线程
    Test_Thread Test_Thread = null;//读卡子线程

    ProgressDialog pd;


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv);

        pd=new ProgressDialog(this);
        pd.setMessage("读卡中，请稍候...");

        SerialPortNative.HardWarePowerOff();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SerialPortNative.initHardWare();

        destDir = new File(this.FILEPATH);
        if (!destDir.exists()) {
            if (destDir.mkdirs()) {
                Log.i(tag, "make dir OK");
                destDir.setExecutable(true);
                destDir.setReadable(true);
                destDir.setWritable(true);
            } else {
                Log.i(tag, "make dir failed");
            }
        } else {
            Log.i(tag, "make dir, dir exist");
            destDir.setExecutable(true);
            destDir.setReadable(true);
            destDir.setWritable(true);

            for (File f : destDir.listFiles()) {
                Log.i(tag , f.toString());
                f.delete();
            }
        }



//        Sys.Lib_ReadSN(serialNo);
//        String sn="";
//        try {
//            sn=new String(serialNo,"UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        Log.i(tag,"Sys.Lib_ReadSN():"+Sys.Lib_ReadSN(serialNo));
//        Log.i(tag,"serialNo>>>>>>>>>>>"+sn);
//        for(int i=0;i<serialNo.length;i++){
//            Log.i(tag,serialNo[i]+"");
//        }
//
//        Sys.Lib_Beep();
//        Log.i(tag,"Sys.Lib_Beep():"+Sys.Lib_Beep());

//        ret1=Mcr.Lib_McrOpen();
//        ret1=Mcr.Lib_McrReset();
//        if (0 != ret1){
//            Log.d(tag, "Lib_MsrOpen error!"+ret1);
//        }else {
//            Log.d(tag, "Lib_MsrOpen succeed!"+ret1);
//        }

        Intent extraIntent=getIntent();
        price=extraIntent.getStringExtra("price")+"";//支付金额
        theSerialNumber=extraIntent.getStringExtra("theSerialNumber")!=null?extraIntent.getStringExtra("theSerialNumber"):"";//机器序列号
        totalDegree=extraIntent.getStringExtra("totalDegree")+"";//总扣分
        ordercode=extraIntent.getStringExtra("ordercode")+"";
        orderstyle=extraIntent.getIntExtra("orderstyle",orderstyle);
        info=extraIntent.getStringExtra("info")!=null?extraIntent.getStringExtra("info"):"";

        Amount=Double.valueOf(price).longValue();//授权金额
        Log.i(tag,">>>>>>:"+Amount);

        findViews();
        initView();

        if(Test_Thread != null && !Test_Thread.m_bThreadFinished){
            return;
        }
        check_thread=new Check_Thread();
        check_thread.start();
    }

    byte [] serialNo=new byte[32];
    HashMap map=new HashMap();
    private HashMap getParamter(){
        map.put("token", TokenSQLUtils.check());//Token值
        int no=Sys.Lib_ReadSN(serialNo);
//        map.put("poscode",new String(serialNo).toString());//设备序列号
        map.put("poscode","YH000000001");//设备序列号
        map.put("ordercode",ordercode);//订单编号
        map.put("paymoney",price);//支付金额（）
        if(orderstyle==3){//年检订单传
            map.put("is_annual_inspection", 1);
        }
        return map;
    }

    /**
     * 通过ID查找所有控件
     */
    private void findViews(){

        ImageView animationIV= (ImageView) findViewById(R.id.animationIV);
        AnimationDrawable animationDrawable = (AnimationDrawable) animationIV.getDrawable();
        animationDrawable.start();

        vpos_back_ib= (ImageButton) findViewById(R.id.vpos_back_ib);
        vpos_back_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                check_thread.exit=true;
                finish();
            }
        });

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
    }


    @Override
    protected void onStart(){
        super.onStart();
        Log.i(tag,"onStart....");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(tag,"onStop....");
    }

    int ret1=-1;
    @Override
    protected void onResume() {
        super.onResume();

        Picc = new Picc();
        Icc = new Icc();

        ret1=Mcr.Lib_McrReset();
        ret1=Mcr.Lib_McrOpen();
        if (0 != ret1){
            Log.d(tag, "Lib_MsrOpen error!"+ret1);
        }else {
            Log.d(tag, "Lib_MsrOpen succeed!"+ret1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        int c=Mcr.Lib_McrClose();//关闭刷卡磁头
        if(c!=0){
            Log.i(tag,"Mcr.Lib_McrClose() is err"+"\n"+c);
        }

//        try {
//            check_thread.interrupt();
//            check_thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Log.e(tag,e.toString());
//        }
        SerialPortNative.HardWarePowerOff();
        Log.e(tag, "onDestroy HardWarePowerOff");
    }



    public void SendMsg(String strInfo, int what){
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    byte solt = (byte)0 ;
    public byte dataIn[] = new byte[512];
    public class Check_Thread extends Thread{

        public volatile boolean exit = false;//设置停止标志位

        int checkType;

        public Check_Thread(){

        }

        public void run(){
            byte[] cardType=new byte[2];
            byte[] serialNo=new byte[10];

            byte slot = (byte)0;
            byte vcc_mode = 1;
            byte ATR[] = new byte[40];

            int ret2=-2;
            int ret3=-3;


            byte cmd[] = new byte[4];
            cmd[0] = 0x00;			//0-3 cmd
            cmd[1] = (byte) 0xa4;
            cmd[2] = 0x04;
            cmd[3] = 0x00;
            short lc = 0x0e;
            short le = 256;

            synchronized(this){

                do{
                    if(ret1!=0) {
                        ret1 = Mcr.Lib_McrOpen();//打卡磁头
                    }
                    checkType=Mcr.Lib_McrCheck();	//磁条卡刷卡检卡
                    if(checkType==0){
                        Log.i(tag,"checkType1:"+checkType);
                        new Test_Thread(2,Amount).start();
                        return;
                    }else{
//                        Mcr.Lib_McrClose();
                    }
                    Log.i(tag,"checkType1:"+checkType+"\nMcr.Lib_McrOpen():"+ret1);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ret2 = Icc.Lib_IccOpen(slot, vcc_mode, ATR);
                    APDU_SEND ApduSend = new APDU_SEND(cmd, lc, dataIn, le);
                    APDU_RESP ApduResp = null;
                    byte[] resp = new byte[516];
                    checkType = Icc.Lib_IccCommand(slot, ApduSend.getBytes(), resp);
//						checkType = Icc.Lib_IccCheck(solt);    //IC卡插卡卡检卡
                    Log.i(tag, "checkType2:" + checkType + "\nLib_IccOpen(slot, vcc_mode, ATR):" + ret2);
                    if (checkType == 0) {
                        new Test_Thread(0,Amount).start();
                        return;
                    }else{
                        Log.e(tag, "Lib_IccOpen failed!");
                        Icc.Lib_IccClose(slot);
                    }

                    try {
                        Thread.sleep(500);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    ret3=Picc.Lib_PiccOpen();
                    checkType=Picc.Lib_PiccCheck((byte)'a',cardType,serialNo);	//非接卡检卡
                    Log.i(tag,"checkType3:"+checkType+"\nPicc.Lib_PiccOpen():"+ret3);
                    if(checkType==0){
                        new Test_Thread(1,Amount).start();
                        return;
                    }else{
                        Picc.Lib_PiccClose();
                    }
                }while(!exit);
            }

        }

    }


    class Test_Thread extends Thread {

        int testMode, ret;
        long TransAmount ;//授权金额
        long TransNo = 0;//交易流水号
        boolean m_bThreadFinished = true;
        EMV_PARAM TermParam;
        byte PART_MATCH = 0;

        byte[] AppName = new byte[33];
        byte[] AID = {(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x33};
        byte AidLen = 5;
        byte SelFlag = PART_MATCH;
        byte Priority = 0;
        byte TargetPer = 0;
        byte MaxTargetPer = 0;
        byte FloorLimitCheck = 1;
        byte RandTransSel = 1;
        byte VelocityCheck = 1;
        long FloorLimit = 2000;
        long Threshold = 0;
        byte[] TACDenial = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
        byte[] TACOnline = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
        byte[] TACDefault = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
        byte[] AcquierId = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0x56};
        byte[] dDOL = {(byte)0x03,(byte)0x9F,(byte)0x37,(byte)0x04};
        byte[] tDOL = {(byte)0x0F,(byte)0x9F,(byte)0x02,(byte)0x06,(byte)0x5F,(byte)0x2A,(byte)0x02,(byte)0x9A,(byte)0x03,(byte)0x9C,(byte)0x01,(byte)0x95,(byte)0x05,(byte)0x9F,(byte)0x37,(byte)0x04};
        byte[] Version = {(byte)0x00,(byte)0x30};
        byte[] RiskManData = new byte[10];
        byte EC_bTermLimitCheck = 0;
        long EC_TermLimit = 10000;

        byte CL_bStatusCheck = 1;
        long CL_FloorLimit = 10000;
        long CL_TransLimit = 50000;
        long CL_CVMLimit = 20000;
        byte TermQuali_byte2 = 0;

        int slot;

        public Test_Thread(int slot, long TransAmount ) {
            TermParam = new EMV_PARAM();
            this.slot=slot;

            this.TransAmount=TransAmount;//授权金额
        }

        public void run() {
            Log.e("ScanThread[ run ]", "run() begin");
            m_bThreadFinished = false;
            SendMsg("", 2);

            synchronized (this) {
                do{
                    SendMsg("init...", 0);

                    EmvKernel.EmvLib_SetFilePath(FILEPATH);

                    ret = EmvKernel.EmvLib_Init();
                    if(ret != 0){
                        SendMsg("EmvLib_Init Fail ret = " + ret, 0);
                        break;
                    }

                    SendMsg("Add Capk...", 0);
                    {
                        byte[] RID = {(byte) 0xA0,0x00,0x00,0x00,0x03};
                        byte KeyID = (byte) 0x90;
                        byte HashInd = 0x01;
                        byte ArithInd = 0x01;
                        byte ModulLen = 64;
                        byte[] Modul = {(byte)0xC2,(byte)0x6B,(byte)0x3C,(byte)0xB3,(byte)0x83,(byte)0x3E,(byte)0x42,(byte)0xD8,(byte)0x27,(byte)0x0D,(byte)0xC1,(byte)0x0C,(byte)0x89,(byte)0x99,(byte)0xB2,(byte)0xDA,
                                (byte)0x18,(byte)0x10,(byte)0x68,(byte)0x38,(byte)0x65,(byte)0x0D,(byte)0xA0,(byte)0xDB,(byte)0xF1,(byte)0x54,(byte)0xEF,(byte)0xD5,(byte)0x11,(byte)0x00,(byte)0xAD,(byte)0x14,
                                (byte)0x47,(byte)0x41,(byte)0xB2,(byte)0xA8,(byte)0x7D,(byte)0x68,(byte)0x81,(byte)0xF8,(byte)0x63,(byte)0x0E,(byte)0x33,(byte)0x48,(byte)0xDE,(byte)0xA3,(byte)0xF7,(byte)0x80,
                                (byte)0x38,(byte)0xE9,(byte)0xB2,(byte)0x1A,(byte)0x69,(byte)0x7E,(byte)0xB2,(byte)0xA6,(byte)0x71,(byte)0x6D,(byte)0x32,(byte)0xCB,(byte)0xF2,(byte)0x60,(byte)0x86,(byte)0xF1};

                        byte ExponentLen = 1;
                        byte[] Exponent = {0x03};
                        byte[] ExpDate = {0x15,0x12,0x31};
                        byte[] CheckSum = {(byte) 0xB3,(byte)0xAE,0x2B,(byte)0xC3,(byte)0xCA,(byte)0xFC,0x05,(byte)0xEE,(byte)0xEF,(byte)0xAA,0x46,(byte)0xA2,(byte)0xA4,0x7E,(byte)0xD5,0x1D,
                                (byte)0xE6,0x79,(byte)0xF8,0x23};

                        EMVCAPK capk_visa_t90 = new EMVCAPK(RID, KeyID, HashInd, ArithInd, ModulLen, Modul, ExponentLen, Exponent,
                                ExpDate, CheckSum);
                        ret = EmvKernel.EmvLib_AddCapk(capk_visa_t90);
                        if(ret != 0){
                            SendMsg("EmvLib_AddCapk Fail ret = " + ret, 0);
                            break;
                        }
                    }

                    //CUP_TEST_APP
                    {
                        byte[] AID1 = {(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x33,(byte)0x01,(byte)0x01,(byte)0x02};
                        byte AidLen1 = 8;

                        EMV_APPLIST CUP_TEST_APP = new EMV_APPLIST(AppName, AID1, AidLen1, SelFlag,
                                Priority, TargetPer, MaxTargetPer,
                                FloorLimitCheck, RandTransSel, VelocityCheck,
                                FloorLimit, Threshold, TACDenial,
                                TACOnline, TACDefault, AcquierId, dDOL,
                                tDOL, Version, RiskManData, EC_bTermLimitCheck,
                                EC_TermLimit, CL_bStatusCheck,CL_FloorLimit, CL_TransLimit,
                                CL_CVMLimit, TermQuali_byte2);
                        ret = EmvKernel.EmvLib_AddApp(CUP_TEST_APP);
                        if(ret != 0){
                            SendMsg("EmvLib_AddApp Fail", 0);
                            break;
                        }
                    }

                    //PBOC_TEST_APP
                    {
                        byte[] AID2 = {(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x33,(byte)0x01,(byte)0x1};
                        byte AidLen2 = 7;

                        EMV_APPLIST PBOC_TEST_APP = new EMV_APPLIST(AppName, AID2, AidLen2, SelFlag,
                                Priority, TargetPer, MaxTargetPer,
                                FloorLimitCheck, RandTransSel, VelocityCheck,
                                FloorLimit, Threshold, TACDenial,
                                TACOnline, TACDefault, AcquierId, dDOL,
                                tDOL, Version, RiskManData, EC_bTermLimitCheck,
                                EC_TermLimit,CL_bStatusCheck, CL_FloorLimit, CL_TransLimit,
                                CL_CVMLimit, TermQuali_byte2);
                        ret = EmvKernel.EmvLib_AddApp(PBOC_TEST_APP);	//D1 3D
                        if(ret != 0){
                            SendMsg("EmvLib_AddApp Fail", 0);
                            break;
                        }
                    }

                    ret = EmvKernel.EmvLib_GetParam(TermParam);
                    if(ret != 0){
                        SendMsg("EmvLib_GetParam Fail ret = " + ret, 0);
                        break;
                    }

                    {
                        ret = EmvKernel.EmvLib_SetParam(TermParam);
                        if(ret != 0){
                            SendMsg("EmvLib_SetParam Fail ret = " + ret, 0);
                            break;
                        }
                    }


                    if(slot == 0){   //IC插卡{
                        while(true){

                            Log.i(tag,"do 插卡");

                            iret = DetectInput();
                            if(iret != 0)
                                continue;

                            ProcessIcc(TransNo,TransAmount);
                            TransNo += 1;

                            break;
                        }
                    }else if(slot == 1){//非接
                        ret = EmvKernel.EmvLib_qPBOCPreProcess(TransAmount);
                        if(ret != 0){
                            SendMsg("EmvLib_qPBOCPreProcess ret = " + ret, 0);
                            break;
                        } else {
                            while(true){

                                Log.i(tag,"do 非接");

                                iret = DetectInputCL();
                                if(iret != 0)
                                    continue;

                                ProcessPicc(TransNo,TransAmount);
                                TransNo += 1;

                                break;
                            }
                        }
                    }else if(slot==2){//磁条刷卡

                        DetectInputMcr();
                        break;
                    }
                }while(false);
                m_bThreadFinished = true;
                return;
            }
        }
    };


    public byte track1[] = new byte[250];
    public byte track2[] = new byte[250];
    public byte track3[] = new byte[250];
    public byte msrKey[] = new byte[20];

    //磁条卡
    void DetectInputMcr(){

        while(true){

            Log.i(tag,"do 刷卡");

            int ret = -1;

            track1 = new byte[250];
            track2 = new byte[250];
            track3 = new byte[250];
            ret = Mcr.Lib_McrRead((byte)0, (byte)0, track1, track2, track3);
            Log.e(tag, "Lib_McrRead ret = " + ret);
            if (ret > 0) {
                Message msg = new Message();
                Bundle b = new Bundle();
                String string = "";
                Log.d(tag, "ret = " + ret);
                if(ret <= 7){
                    if((ret & 0x01) == 0x01)
                        string = "track1:" + new String(track1).trim();
                    if((ret & 0x02) == 0x02)
                        string = string + "\n\ntrack2:" + new String(track2).trim();
                    if((ret & 0x04) == 0x04)
                        string = string + "\n\ntrack3:" + new String(track3).trim();
                }else{
                    string = "Lib_MsrRead check data error";
                }

                b.putString("MSG", string);
                msg.setData(b);
                handler.sendMessage(msg);
                Log.i(tag, "Lib_MsrRead succeed!");
            } else {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("MSG", "Lib_MsrRead failed!");
                msg.setData(b);
                handler.sendMessage(msg);
                Log.e(tag, "Lib_MsrRead failed!");
            }

            String trackInfo3=new String(track3).trim();
            String trackInfo2=new String(track2).trim();

            if(trackInfo2.trim().length()>0&&trackInfo3.length()>10&&(!trackInfo3.trim().equals(""))){//是磁条卡
                trackInfo2=trackInfo2.replace("=","D")+"F";
                map.put("cardtype","2");//卡类型  1：ic卡 2：磁条卡3:非接
                map.put("mainaccount",trackInfo2.substring(0,trackInfo2.indexOf("D")));//主帐号(卡号)  tag:5a
//                map.put("cardserialnumber",cardserialnumber);//卡序列号  tag:5F34
                map.put("trackInfo2",trackInfo2);//2磁道等效数据 tag:57
                map.put("cardvalidity",trackInfo2.substring(trackInfo2.indexOf("D")+1,trackInfo2.indexOf("D")+5));//卡的有效期（选填） tag:5F24
                map.put("trackInfo3",trackInfo3.toString().replaceAll("=","D"));
//                Log.i(tag,"3磁道数据 is:"+trackInfo3.toString().replaceAll("=","D"));
                toInputPsw(map);
                return;//刷卡成功
            }else if(track2.length>0&&track3.length==0){//ic卡
                Toast.makeText(VPosPayActivty.this,"IC卡请使用插卡或非接方式读卡...",Toast.LENGTH_LONG).show();
                new Check_Thread().start();
                return;
                //提示IC卡请插卡
            }else{
                //刷卡不成功
                new Check_Thread().start();
                return;
            }
        }


    }

    //ic卡
    int DetectInput(){

        int ret = -1;
        byte slot = 0;
        byte vcc_mode = 1;
        byte ATR[] = new byte[40];

        ret = Icc.Lib_IccOpen(slot, vcc_mode, ATR);

        SendMsg("Icc.Lib_IccOpen ret = " + ret, 0);
        if(ret != 0){
            Log.e(tag, "Lib_IccOpen failed!");
            Icc.Lib_IccClose(slot);
            return -1;
        }
        else
        {
            return 0;
        }
    }

    //非接
    int DetectInputCL(){

        int ret;
        byte mode = 0;
        byte cardType[] = new byte[10];
        byte serialNo[] = new byte[50];


        ret = Picc.Lib_PiccOpen();
        SendMsg("Picc.Lib_PiccOpen ret = " + ret, 0);
        SleepMs(1300);
        if(ret == 0)
        {

            ret = Picc.Lib_PiccCheck(mode, cardType, serialNo);
            SendMsg("Picc.Lib_PiccCheck ret = " + ret, 0);
            SleepMs(1300);
            if(ret == 0)
            {
                return 0;
            }
            return -1;
        }
        else {

            return -1;
        }
    }


    /*
	* 插卡或非接读卡失败
	* */
    private void readFail(){
        new Check_Thread().start();
//        Toast.makeText(VPosPayActivty.this,"读卡失败，请重试",Toast.LENGTH_LONG).show();
//        if(pd!=null&&pd.isShowing())
//            pd.dismiss();

        SendMsg("",111);
    }

    void ProcessIcc(long transno,long TransAmount)
    {
        int ret;
        byte ifonline[] ={0,0};
        long backamt = 0;
        byte tdate[] = {(byte) 0x15,(byte)0x05,(byte)0x15};
        byte ttime[] = {(byte) 0x09,(byte)0x20,(byte)0x00};

        byte Result;
        byte RspCode[] = {0x30,0x30};
        byte AuthCode[] = new byte[6];
        int AuthCodeLen = 0;
        byte IAuthData[] = new byte[64];
        int IAuthDataLen = 0;
        byte Script[]= new byte[128];
        int ScriptLen = 0;

        int len[] = new int[1];
        byte buff[] = new byte[80];


        SendMsg("读卡中，请稍候...",110);

        Log.e(tag, "ProcessIcc Begin................");
//	    while (true)
        {
            ret = EmvKernel.EmvLib_BeforeTrans(TransAmount,backamt,tdate,ttime);
            if(ret != 0){
                SendMsg("EmvLib_BeforeTrans Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            Log.e(tag, "EmvLib_AppSel Begin................");
            ret = EmvKernel.EmvLib_AppSel(0, transno);
            if(ret != 0){
                SendMsg("EmvLib_AppSel Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            Log.e(tag, "EmvLib_ReadAppData Begin................");
            ret = EmvKernel.EmvLib_ReadAppData();
            if(ret != 0){
                SendMsg("EmvLib_ReadAppData Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            Log.e(tag, "EmvLib_CardAuth Begin................");
            ret = EmvKernel.EmvLib_CardAuth();
            if(ret != 0){
                SendMsg("EmvLib_CardAuth Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            Log.e(tag, "EmvLib_ProcRestriction Begin................");
            EmvKernel.EmvLib_ProcRestriction();

            Log.e(tag, "EmvLib_CardholderVerify Begin................");
            ret = EmvKernel.EmvLib_CardholderVerify();

            if(ret != 0){
                SendMsg("EmvLib_CardholderVerify Fail ret = " + ret, 0);
                readFail();
                return;
            }
            Log.e(tag, "EmvLib_ProcTransBeforeOnline Begin................");
            ret = EmvKernel.EmvLib_ProcTransBeforeOnline(0,ifonline);
            if(ret != 0){
                SendMsg("EmvLib_ProcTransBeforeOnline Fail ret = " + ret, 0);
                readFail();
                return;
            }
            Log.e(tag, "EmvLib_GetTLV Begin................");


            EmvKernel.EmvLib_GetTLV("5F34",buff,len);
            String cardserialnumber=ByteUtil.bytearrayToHexString(buff, len[0]);
            Log.d(tag, "卡序列号数据："+cardserialnumber);

            EmvKernel.EmvLib_GetTLV("57",buff,len);
            String trackInfo2=ByteUtil.bytearrayToHexString(buff, len[0]);
            Log.d(tag, "卡二磁道等效数据："+trackInfo2.replaceAll(" ","").trim().toString());

            EmvKernel.EmvLib_GetTLV("5a",buff,len);
            String mainaccount=ByteUtil.bytearrayToHexString(buff, len[0]);
            Log.d(tag, "卡号等效数据："+mainaccount.replaceAll(" ","").replaceAll("F","").trim().toString());

            EmvKernel.EmvLib_GetTLV("5F24",buff,len);
            String cardvalidity=ByteUtil.bytearrayToHexString(buff, len[0]);
            Log.d(tag, "卡有效期数据："+cardvalidity.replaceAll(" ","").trim().toString());

            String info55=get55Data();

            Log.d(tag, "55域数据类型："+info55);


            if(ifonline[0] == 1)
            {
                {
                    Result = 0;
                    RspCode[0] = 0x30;
                    RspCode[1] = 0x30;

                }
                Log.d(tag, "EmvLib_ProcTransComplete");
                ret=EmvKernel.EmvLib_ProcTransComplete(Result,RspCode,AuthCode,AuthCodeLen,IAuthData,IAuthDataLen,Script,ScriptLen);
                if(ret != 0){
                    SendMsg("EmvLib_ProcTransComplete Fail ret = " + ret, 0);
                    readFail();
                    return;
                }
            }

            if (info55.length()>=80){//获取到数据
                map.put("cardtype","1");//卡类型  1：ic卡 2：磁条卡3:非接
                map.put("mainaccount",mainaccount.replaceAll(" ","").replaceAll("F","").trim().toString());//主帐号(卡号)  tag:5a
                map.put("cardserialnumber",cardserialnumber);//卡序列号  tag:5F34
                map.put("iccardinfo",info55);//55域数据，IC卡必传，磁条卡不用传
                map.put("trackInfo2",trackInfo2.replaceAll(" ","").trim().toString());//2磁道等效数据 tag:57
                map.put("cardvalidity",cardvalidity.substring(0,5).replaceAll(" ",""));//卡的有效期（选填） tag:5F24
                toInputPsw(map);
            }else{//获取数据失败

            }

        }
    }

    void ProcessPicc(long transno,long TransAmount)
    {
        int ret;

        long backamt = 0;
        byte ifonline[] = {0,0};
        byte tdate[] = {(byte) 0x15,(byte)0x05,(byte)0x15};
        byte ttime[] = {(byte) 0x09,(byte)0x20,(byte)0x00};

        int len[] = new int[1];
        byte buff[] = new byte[80];

        byte Result;
        byte RspCode[] = {0,0};
        byte AuthCode[] = new byte[6];
        int AuthCodeLen = 0;
        byte IAuthData[] = new byte[64];
        int IAuthDataLen = 0;
        byte Script[]= new byte[128];
        int ScriptLen = 0;

        SendMsg("读卡中，请稍候...",110);

        Log.e(tag, "ProcessPicc Begin................");
        {
            ret = EmvKernel.EmvLib_BeforeTrans(TransAmount,0,tdate, ttime);

            if(ret != 0){
                SendMsg("EmvLib_BeforeTrans Fail, ret = " + ret, 0);
                readFail();
                return;
            }
            ret = EmvKernel.EmvLib_AppSel(1, transno);

            if(ret != 0){
                SendMsg("EmvLib_AppSel Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            ret = EmvKernel.EmvLib_ReadAppData();
            if(ret != 0){
                SendMsg("EmvLib_ReadAppData Fail, ret = " + ret, 0);
                readFail();
                return;
            }

            ret = EmvKernel.EmvLib_CardAuth();
            if(ret != 0){
                SendMsg("EmvLib_CardAuth Fail, ret = " + ret, 0);
                readFail();
                return;
            }


            ret = EmvKernel.EmvLib_ProcCLTransBeforeOnline(1,ifonline);
            if(ret != 0){
                SendMsg("EmvLib_ProcCLTransBeforeOnline Fail ret = " + ret, 0);
                readFail();
                return;
            }


            EmvKernel.EmvLib_GetTLV("5F34",buff,len);
            String cardserialnumber=ByteUtil.bytearrayToHexString(buff, len[0]);
            Log.d(tag, "卡序列号数据："+cardserialnumber);

            EmvKernel.EmvLib_GetTLV("57",buff,len);
            String trackInfo2=ByteUtil.bytearrayToHexString(buff, len[0]).replaceAll(" ","").trim().toString();
            Log.d(tag, "卡二磁道等效数据："+trackInfo2.replaceAll(" ","").trim().toString());

//            EmvKernel.EmvLib_GetTLV("5a",buff,len);
            String mainaccount=trackInfo2.substring(0,trackInfo2.indexOf("D"));
            Log.d(tag, "卡号等效数据："+mainaccount.replaceAll(" ","").replaceAll("F","").trim().toString()+"\n"+trackInfo2);
//
//            EmvKernel.EmvLib_GetTLV("5F24",buff,len);
            String cardvalidity=trackInfo2.substring(trackInfo2.indexOf("D")+1,trackInfo2.indexOf("D")+5);
            Log.d(tag, "卡有效期数据："+cardvalidity.replaceAll(" ","").trim().toString());


            String info55=get55Data();
            Log.d(tag, "55域数据类型："+info55);

            SendMsg("Test Finish", 0);
            if(ifonline[0] == 1)
            {
                {
                    Result = 0;
                    RspCode[0] = 0x30;
                    RspCode[1] = 0x30;

                }

                ret=EmvKernel.EmvLib_ProcCLTransComplete(Result,RspCode,AuthCode,AuthCodeLen);	//D1 17
                if(ret != 0){
                    SendMsg("EmvLib_ProcCLTransComplete Fail ret = " + ret, 0);
                    return;
                }
            }

            if (info55.length()>=80){//获取到数据
                map.put("cardtype","1");//卡类型  1：ic卡 2：磁条卡3:非接
                map.put("mainaccount",mainaccount);//主帐号(卡号)  tag:5a
                map.put("cardserialnumber",cardserialnumber);//卡序列号  tag:5F34
                map.put("iccardinfo",info55);//55域数据，IC卡必传，磁条卡不用传
                map.put("trackInfo2",trackInfo2);//2磁道等效数据 tag:57
                map.put("cardvalidity",cardvalidity.substring(0,5).replaceAll(" ",""));//卡的有效期（选填） tag:5F24
                toInputPsw(map);
            }else{//获取数据失败

            }
        }
    }

    void SleepMs(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String strInfo = b.getString("MSG");
            Log.d(tag, strInfo);
            switch (msg.what){
                case 110:
                    pd.show();
                    break;

                case 111:
                    if(pd!=null&&pd.isShowing()){
                        pd.dismiss();
                    }
                    break;
            }
        }
    };


    String data55="";
    public String get55Data(){
        int len[] = new int[1];
        byte buff[] = new byte[80];

        String hexStr="";

        DecimalFormat df=new DecimalFormat("00");

        EmvKernel.EmvLib_GetTLV("9f26",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        Log.i(tag,"hexString>>>>>>>>>>>>>"+df.format(hexStr.length()/2));
        data55+="9f26"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f27",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f27"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f10",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f10"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f37",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f37"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;


        EmvKernel.EmvLib_GetTLV("9f36",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f36"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("95",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="95"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9a",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9a"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9c",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9c"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f02",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f02"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("5f2a",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="5f2a"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("82",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="82"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f1a",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f1a"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f03",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f03"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;


        EmvKernel.EmvLib_GetTLV("9f33",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f33"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f34",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f34"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f35",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f35"+ df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f1e",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f1e"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("84",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="84"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f09",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f09"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        EmvKernel.EmvLib_GetTLV("9f41",buff,len);
        hexStr=ByteUtil.bytearrayToHexString(buff, len[0]).trim().replace(" ","");
        data55+="9f41"+df.format(Integer.parseInt(Integer.toHexString(hexStr.length()/2)))+hexStr;

        return data55.replaceAll(" ","").trim().toLowerCase();
    }


    /*
    * 跳转到输入密码界面
    * */
    Intent intent=new Intent();
    private void toInputPsw(HashMap map){

        Sys.Lib_Beep();//蜂鸣器响一下

        intent.setClass(VPosPayActivty.this,InputPswActivity.class);
        //创建Bundle对象，存放实现可序列化的SerMap
        Bundle bundle=new Bundle();
        bundle.putSerializable("map",getParamter());
        intent.putExtras(bundle);
        startActivity(intent);
        VPosPayActivty.this.finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        check_thread.exit=true;
        finish();
        Log.i(tag,"onBracPressed is run.....");
    }
}
