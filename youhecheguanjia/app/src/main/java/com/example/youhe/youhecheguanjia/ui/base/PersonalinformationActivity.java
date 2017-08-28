package com.example.youhe.youhecheguanjia.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.app.AppContext;
import com.example.youhe.youhecheguanjia.bean.FirstEvent;
import com.example.youhe.youhecheguanjia.biz.SaveNameDao;
import com.example.youhe.youhecheguanjia.city.CityActivty;
import com.example.youhe.youhecheguanjia.db.biz.NamePathSQLUtils;
import com.example.youhe.youhecheguanjia.db.biz.StatusSQLUtils;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.dialog.UIDialog;
import com.example.youhe.youhecheguanjia.https.URLs;
import com.example.youhe.youhecheguanjia.logic.MainService;
import com.example.youhe.youhecheguanjia.logic.Task;
import com.example.youhe.youhecheguanjia.logic.TaskType;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;
import com.example.youhe.youhecheguanjia.utils.BitmapScale;
import com.example.youhe.youhecheguanjia.utils.ClickUtils;
import com.example.youhe.youhecheguanjia.slidingmenu.HeadPortraitPopupWindow;
import com.example.youhe.youhecheguanjia.utils.EncryptUtil;
import com.example.youhe.youhecheguanjia.utils.ImageUtils;
import com.example.youhe.youhecheguanjia.utils.ImgDownload;
import com.example.youhe.youhecheguanjia.utils.Misidentification;
import com.example.youhe.youhecheguanjia.utils.StringUtils;
import com.example.youhe.youhecheguanjia.utils.SystemBarUtil;
import com.example.youhe.youhecheguanjia.utils.VolleyUtil;
import com.example.youhe.youhecheguanjia.widget.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.IOException;
import java.util.HashMap;

/**
 * 个人信息界面
 */
public class PersonalinformationActivity extends Activity {
    private ImageView circleImageView;//头像
    private TextView name,tv_num;//昵称和手机号
    private TextView clity;//选择的城市位置
    private String city;
    private String nameText;
    private SaveNameDao saveNameDao;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private TokenSQLUtils tokenSQLUtils;
    private StatusSQLUtils statusSQLUtils;
    private ImageUtils imageUtils;
    private RelativeLayout exitBtn;//退出按钮

    private final int CITY_REQUESTCODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalinformation);

        // 4.4及以上版本开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarUtil.setTranslucentStatus(true,PersonalinformationActivity.this);
        }
        SystemBarUtil.useSystemBarTint(PersonalinformationActivity.this);

        x.view().inject(this);
        in();//初始化

    }

    /**
     * 初始化
     */
    private void in() {
        EventBus.getDefault().register(this);
        statusSQLUtils = new StatusSQLUtils(this);
        imageUtils = new ImageUtils();
        circleImageView = (ImageView) findViewById(R.id.iv_headportrait);//头像
        tokenSQLUtils = new TokenSQLUtils(this);
        clity = (TextView) findViewById(R.id.clity);//城市位置

        saveNameDao = new SaveNameDao(this);
        name = (TextView) findViewById(R.id.tv_name);//昵称
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_num.setText(StringUtils.showNum(saveNameDao.readText("phonenumbe.txt")));//把登录的电话号码显示出来
        sp = getSharedPreferences("judges", Context.MODE_PRIVATE);
        editor = sp.edit();

        exitBtn= (RelativeLayout) findViewById(R.id.rlay6);

        setCity();//设置城市
    }

    /**
     * 布局中的菜单点击事件
     * @param view
     */
    public void dianji(View view){

        switch (view.getId()){

            case R.id.rlay1://头像
                inSelectMeu();//弹出菜单选择操作
                break;

            case R.id.rlay2://昵称
                startActivity(new Intent(this,TheNameOfTheSetActivity.class));
                break;

            case R.id.rlay4://地区
                startActivityForResult(new Intent(this,CityActivty.class),CITY_REQUESTCODE);
                break;

            case R.id.rlay5://更换手机号码
                startActivity(new Intent(this,ReplaceMobilePhoneNumberActivity.class));
                break;

            case R.id.rlay7://修改密码
                Intent intent = new Intent(this,ChangePasswordActivity.class);
                startActivity(intent);
                this.overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_from_left);
                break;

            case R.id.rlay6://退出登录
                saveTheLoginStatus();
                editor.putBoolean("judge", false);
                editor.commit();
                break;
        }
    }


    private void inSelectMeu() {//初始化弹出菜单
        Intent intent1 = new Intent(PersonalinformationActivity.this, SelectPicActivity.class);
        startActivityForResult(intent1, 1);
    }

    /**
     * 返回键点击事件
     *
     */
    public void fanhui(View view){
        finish();
    }

    private Handler handler = new Handler(){//退出清除Token值
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    uidialog.hide();

                    HashMap params=new HashMap();
                    Task ts=new Task(TaskType.TS_USER_EXIT,params);//退出
                    MainService.newTask(ts);
                    finish();
                    break;

            }
        }
    };
    private UIDialog uidialog;
    /**
     * 保存退出登录状态
     */
    public void saveTheLoginStatus(){
        uidialog = new UIDialog(this,"正在退出.......");
        uidialog.show();
        tokenSQLUtils.delete();//清除Token值
        statusSQLUtils.undateApi("no");//保存退出状态
        EventBus.getDefault().post(new FirstEvent("no"));
        handler.sendEmptyMessageDelayed(1,1500);

        HashMap params1 = new HashMap();
        params1.put("auth",-1);
        Task ts1 = new Task(TaskType.TS_REAL_NAME, params1);//退出成功后更新个人中心数据
        MainService.newTask(ts1);

        Intent ldIntent = new Intent("android.net.conn.LOADDATA");
        sendBroadcast(ldIntent);

    }

    @Subscribe
    public void onEventMainThread(FirstEvent event) {
        if (event.getMsg().equals("rush")){
            setName();//设置昵称
        }
        if (event.getMsg().equals("huan")){
            tv_num.setText(saveNameDao.readText("phonenumbe.txt"));//把登录的电话号码显示出来
        }

    }

    /**
     * 每次这个界面出现都要判断
     */
    @Override
    public void onResume() {
        super.onResume();

        touxiang();//头像
        setName();//设置昵称

    }

    /**
     * 设置头像
     */
    public void touxiang(){

        Bitmap head_bitmap =BitmapScale.getDiskBitmap(Environment.getExternalStorageDirectory().toString()+"/yeohe/head_bitmap/"+ "head.jpg");
        if(head_bitmap!=null){
            circleImageView.setImageBitmap(ImgDownload.toRoundBitmap(head_bitmap));
        }else {
            circleImageView.setImageResource(R.drawable.gerz_01);
        }
    }

    /**
     * 设置名称
     */
    private NamePathSQLUtils sqlUtils;
    public void setName(){
        sqlUtils = new NamePathSQLUtils(this);
        String httpNames = sqlUtils.check();//网上的昵称
        if (httpNames.equals("")){

        }else {
            name.setText(httpNames);
        }


    }

    public void setCity(){
        city = ((AppContext) (PersonalinformationActivity.this.getApplication())).getLocalCity().toString();
        if (city != null && (!city.equals(""))) {
            clity.setText(city.toString());
        } else {
            clity.setText("广州");
        }
        clity.setText(city);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private String imgPath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1) {//头像选择结果

           if (data != null) {
               imgPath = data.getStringExtra("photo_path");

           }

           if (imgPath != null) {
               Bitmap head_bitmap = BitmapScale.getBitmap(imgPath);

               if (head_bitmap != null) {
                   Log.i("imgPath","imgPath："+imgPath);
                   uploadOnePhoto(BitmapScale.getImage(head_bitmap),head_bitmap);
               }else{
                   circleImageView.setImageResource(R.drawable.gerz_01);
               }
           }
       }

       if(requestCode==CITY_REQUESTCODE) {
           if (data != null) {
               city = data.getStringExtra("city") + "";
               Log.i("TAG", city + "");
               if ((city != null) && (!city.equals(""))) {
                   clity.setText(city.toString());
               } else {
                   clity.setText("广州");
               }
           }
       }
    }


    /**
     * 以base64上传图片
     *
     * @param bitmap
     */
    HashMap pams=new HashMap();
    public void httpImgs(final Bitmap bitmap,int imgid) {

        pams.put("token", tokenSQLUtils.check());
        pams.put("headimg", imgid);
        VolleyUtil.getVolleyUtil(PersonalinformationActivity.this).StringRequestPostVolley(URLs.UPLOAD_THE_PICTURE, EncryptUtil.encrypt(pams), new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject){
                jsonParsing(EncryptUtil.decryptJson(jsonObject.toString(),PersonalinformationActivity.this), bitmap);
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                ToastUtil.getShortToastByString(PersonalinformationActivity.this, "网络连接失败,无法发送请求");
            }
        });

    }

    public void jsonParsing(String json, Bitmap mbitmap) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.getString("status");
            if (status.equals("ok")) {
                BitmapScale.saveFile(mbitmap);//保存图片
                //把图片变成圆
                circleImageView.setImageBitmap(ImgDownload.toRoundBitmap(mbitmap));
                try {
                    ImageUtils.saveImage(circleImageView.getContext(),"touxiang.png",mbitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveNameDao.writeTxtToFile("yes", "qqq.txt");
                ToastUtil.getLongToastByString(PersonalinformationActivity.this, "头像上传成功");

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    HashMap map;
    private final String IMAGE_TYPE="head";
    private HashMap getOnePhotoParam(String imgtype){
        map = new HashMap();
        map.put("imgtype",IMAGE_TYPE);
        map.put("token",TokenSQLUtils.check()+"");
        return EncryptUtil.encrypt(map);
    }

    private void uploadOnePhoto(String strImg, final Bitmap bitmap){

        map=getOnePhotoParam(IMAGE_TYPE);
        map.put("img",strImg);
        VolleyUtil.getVolleyUtil(PersonalinformationActivity.this).StringRequestPostVolley(URLs.ADD_IMG_URL,map,new VolleyInterface() {
            @Override
            public void ResponseResult(Object jsonObject) {
                try {
                    String  json=EncryptUtil.decryptJson(jsonObject.toString(),PersonalinformationActivity.this);

                    parseJson(json,bitmap);//解析解密之后的数据

                } catch (Exception e){
                    e.printStackTrace();
                }finally {
                }
            }

            @Override
            public void ResponError(VolleyError volleyError) {
                Toast.makeText(PersonalinformationActivity.this,"上传头像返回的错误信息："+volleyError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseJson(String json,Bitmap bitmap){

        try {
            JSONObject newObj = new JSONObject(json);
            String status=newObj.getString("status");
            if(status.equals("ok")) {
                JSONObject dataObj = newObj.getJSONObject("data");
                int imgid = dataObj.getInt("imgid");
                httpImgs(bitmap,imgid);
            }else {
                Toast.makeText(PersonalinformationActivity.this,"上传头像失败，请重试",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
