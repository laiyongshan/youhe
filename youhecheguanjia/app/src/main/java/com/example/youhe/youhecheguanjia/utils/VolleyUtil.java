package com.example.youhe.youhecheguanjia.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.youhe.youhecheguanjia.app.AppContext;
import com.example.youhe.youhecheguanjia.db.biz.TokenSQLUtils;
import com.example.youhe.youhecheguanjia.logic.VolleyInterface;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/8 0008.
 * 封装Volley请求框架Get,POST的核心类
 */
public class VolleyUtil {
    private final static String TAG = "VolleyUtil";
    private static RequestQueue requestQueue = null;
    private static VolleyUtil volleyUtil;
    private Context context;

    public static synchronized VolleyUtil getVolleyUtil(Context context) {

        if (volleyUtil == null) {
            volleyUtil = new VolleyUtil();
        }
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(AppContext.getContext());
        return volleyUtil;
    }

    /**
     * Post请求 url,HashMap,volleyInterface,Class
     * 以实体类形式返回，然后进行强转
     */
    public void SendVolleyPostBean(String url, HashMap<?, ?> hashMap, final VolleyInterface volleyInterface, final Class<?> aClass) {
        Log.i(TAG, "开始请求");

        JSONObject jsonObject = new JSONObject(hashMap);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
//                Object userBean = (Object) g.fromJson(jsonObject.toString(),aClass);
//                volleyInterface.ResponseResult(userBean);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyInterface.ResponError(volleyError);
                Log.i(TAG, "请求错误");
            }
        });

        AddrequestQueue(jsonObjectRequest, true);
    }

    /**
     * JsonObjectRequest
     * GET请求 url,volleyInterface,Class
     * 以实体类形式返回，然后进行强转
     **/
    public void SendVolleyGetBean(String url, final VolleyInterface volleyInterface) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
//                Object userBean = (Object) g.fromJson(jsonObject.toString(),aClass);
//                volleyInterface.ResponseResult(userBean);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyInterface.ResponError(volleyError);
                Log.i(TAG, "请求错误");
            }
        });
        AddrequestQueue(jsonObjectRequest, true);
    }

    /**
     * JsonObjectRequest
     * POST请求 url,volleyInterface,Class
     * 以JSON形式返回，然后进行强转 为JSONobject
     **/
    public void SendVolleyPostJsonobject(String url, final VolleyInterface volleyInterface, final Class<?> aClass) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                volleyInterface.ResponseResult(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyInterface.ResponError(volleyError);
                Log.i(TAG, "请求错误");
            }
        });
        AddrequestQueue(jsonObjectRequest, true);
    }

    /**
     * JsonObjectRequest
     * GET请求 url,volleyInterface,Class
     * 以JSON形式返回，然后进行强转 为JSONobject
     **/
    public void StringRequestGetVolley(String url, final VolleyInterface volleyInterface) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                volleyInterface.ResponseResult(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyInterface.ResponError(volleyError);
                Log.i(TAG, "请求错误");
            }
        });
        //加入队列 及设置 请求时间
        AddrequestQueue(stringRequest, true);
    }


    /**
     * 此方法是 Volley配置方法
     */
    public void AddrequestQueue(Request req, boolean issave) {
        // 设置超时时间
        req.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 2, 1.0f));

        // 是否开启缓存；
//        req.setShouldCache(issave);
        // 将请求加入队列
        requestQueue.add(req);
        // 开始发起请求
//        requestQueue.start();
    }

    //取消所有的请求任务
    public void cancelAllQueue(Context context) {
        requestQueue.cancelAll(context);
    }


    /**
     * StringRequest
     * Post请求
     * url,volleyInterface,hashMap
     * 以JSON形式返回
     */

    public void StringRequestPostVolley(final String url, final HashMap<?, ?> hashMap, final VolleyInterface volleyInterface) {
        if (hashMap.containsKey("token")&& StringUtils.isEmpty(TokenSQLUtils.check())){
            VolleyError volleyError=new VolleyError("token值失效");
            volleyInterface.ResponseResult(volleyError);
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.i(TAG, "onResponse: 成功了");
                volleyInterface.ResponseResult(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i(TAG, "onErrorResponse: 失败了" + volleyError.getMessage() + url);
                volleyInterface.ResponseResult(volleyError);
                Toast.makeText(AppContext.getContext(),"请求服务器端失败，请稍候重试",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return (Map<String, String>) hashMap;
            }
        };

        //加入队列 及设置 请求时间
        AddrequestQueue(stringRequest, true);
    }

}
