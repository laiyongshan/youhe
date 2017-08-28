package com.example.youhe.youhecheguanjia.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.example.youhe.youhecheguanjia.logic.YeoheActivity;
import com.example.youhe.youhecheguanjia.logic.YeoheFragment;

import java.util.Stack;


public class AppManager {

    private static Stack<Activity> activityStack;
    private static Stack<YeoheFragment> fragmentStack;
    private static AppManager instance;

    private AppManager() {
    }

    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }


    public void addFragment(YeoheFragment fragment) {
        if (fragmentStack == null) {
            fragmentStack = new Stack<YeoheFragment>();
        }
        fragmentStack.add(fragment);
    }


    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    public YeoheFragment cuurentFragment() {
        YeoheFragment fragment = fragmentStack.lastElement();
        return fragment;

    }

    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    public void finishFragment() {
        YeoheFragment fragment = fragmentStack.lastElement();
        finishFragment(fragment);
    }

    public void finishActivity(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            activityStack.remove(activity);
            activity.finish();
        }
    }

    public void finishFragment(YeoheFragment fragment) {
        if (fragment != null && !fragment.isHidden()) {
            fragmentStack.remove(fragment);
            fragment = null;
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
                break;
            }
        }
    }

    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                finishActivity(activityStack.get(i));
                break;
            }
        }
        activityStack.clear();
    }

    public static Activity getActivity(Class<?> cls) {
        if (activityStack != null)
            for (Activity activity : activityStack) {
                if (activity.getClass().equals(cls)) {
                    return activity;
                }
            }
        return null;
    }


    public static YeoheFragment getFragmentByName(String name) {
        if (fragmentStack != null)
            for (YeoheFragment fragment : fragmentStack) {
                if (fragment.getClass().getName().indexOf(name) >= 0) {
                    return fragment;
                }
            }
        return null;
    }

    public static YeoheActivity getActivityByName(String name) {
        for (Activity activity : activityStack) {
            if (activity.getClass().getName().indexOf(name) >= 0) {
                return (YeoheActivity) activity;
            }
        }
        return null;
    }


    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {

        try {
            finishAllActivity();
            // System.exit(0);
        } catch (Exception e) {
        }

        // 获取packagemanager的实例
        try {
            ActivityManager activityMgr = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            activityStack = null;
            instance=null;
            System.exit(0);
        } catch (Exception e) {
            System.exit(0);
        }

    }
}