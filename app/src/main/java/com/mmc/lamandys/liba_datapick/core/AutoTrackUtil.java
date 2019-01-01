package com.mmc.lamandys.liba_datapick.core;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daijun.luffy_asm.R;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019/1/1
 * @description
 */
public class AutoTrackUtil {

    public static String getActivityTitle(Activity activity) {
        if (activity == null) {
            return null;
        }
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null && !TextUtils.isEmpty(actionBar.getTitle())) {
                return String.valueOf(actionBar.getTitle());
            }
        }
        // 获取清单配置标题
        if (!TextUtils.isEmpty(activity.getTitle())) {
            return String.valueOf(activity.getTitle());
        }
        return null;
    }

    public static String getViewPath(View view) {
        if (view == null) {
            return null;
        }
        ViewParent viewParent;
        List<String> viewPath = new ArrayList<>();
        do {
            viewParent = view.getParent();
            int index = getChildIndex(viewParent, view);
            viewPath.add(String.format(Locale.getDefault(), "%s[%d]", view.getClass().getCanonicalName(), index));
            if (viewParent instanceof ViewGroup) {
                view = (View) viewParent;
            }
        } while (viewParent instanceof ViewGroup);
        Collections.reverse(viewPath);
        return TextUtils.join("/", viewPath);
    }

    /**
     * 获取view在viewparent中的索引位置
     * @param viewParent
     * @param child
     * @return
     */
    private static int getChildIndex(ViewParent viewParent, View child) {
        if (!(viewParent instanceof ViewGroup)) {
            return  -1;
        }
        ViewGroup viewGroup = (ViewGroup) viewParent;
        String childIdName = getViewId(child);
        String childClassName = child.getClass().getCanonicalName();
        int index = 0;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View brother = viewGroup.getChildAt(i);
            if (!hasClassName(brother, childClassName)) {
                continue;
            }
            String brotherIdName = getViewId(brother);
            if (childIdName != null && !childIdName.equals(brotherIdName)) {
                index++;
                continue;
            }
            if (brother == child) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static boolean hasClassName(Object o, String className) {
        Class clazz = o.getClass();
        while (clazz != null && clazz.getCanonicalName() != null) {
            if (clazz.getCanonicalName().equals(className)) {
                return true;
            }
            if (clazz == Object.class) {
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    private static String getViewId(View child) {
        if (child != null && child.getId() != View.NO_ID) {
            return child.getResources().getResourceEntryName(child.getId());
        }
        return null;
    }

    public static String traverseView(StringBuilder stringBuilder, ViewGroup root) {
        if (root == null) {
            return stringBuilder.toString();
        }
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = root.getChildAt(i);
            if (childAt.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (childAt instanceof ViewGroup) {
                traverseView(stringBuilder, (ViewGroup) childAt);
            } else {
                CharSequence text = traverseViewOnly(childAt);
                if (!TextUtils.isEmpty(text)) {
                    stringBuilder.append(text).append("-");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static CharSequence traverseViewOnly(View child) {
        if (child instanceof CheckBox || child instanceof RadioButton) {
            return ((CompoundButton) child).getText();
        }
        if (child instanceof SwitchCompat) {
            SwitchCompat compat = (SwitchCompat) child;
            return compat.isChecked() ? compat.getTextOn(): compat.getTextOff();
        }
        if (child instanceof ToggleButton) {
            ToggleButton compat = (ToggleButton) child;
            return compat.isChecked() ? compat.getTextOn(): compat.getTextOff();
        }
        if (child instanceof TextView) {
            return ((TextView) child).getText();
        }
        if (child instanceof ImageView) {
            return child.getContentDescription();
        }
        return null;
    }

    /**
     * 用于在{@link com.mmc.lamandys.liba_datapick.AutoTrackHelper#onFragmentViewCreated(Object, View, Bundle)}
     * 方法中给对应的view打上Fragment名称
     *
     * @param fragmentName Fragment名称
     * @param root         参数对应的view
     */
    public static void traverseViewForFragment(String fragmentName, ViewGroup root) {
        if (TextUtils.isEmpty(fragmentName) || root == null) {
            return;
        }
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = root.getChildAt(i);
            if (child instanceof ListView
                    || child instanceof GridView
                    || child instanceof Spinner
                    || child instanceof RadioGroup) {
                child.setTag(R.id.auto_track_tag_view_fragment_name, fragmentName);
            } else if (child instanceof ViewGroup) {
                traverseViewForFragment(fragmentName, (ViewGroup) child);
            } else {
                child.setTag(R.id.auto_track_tag_view_fragment_name, fragmentName);
            }
        }
    }

    /**
     * 获取当前页面全类名,优先级activity+fragment->fragment->activity
     */
    public static String getScreenNameFromView(Activity activity, View view) {
        if (view == null) {
            return null;
        }
        String fragmentName = (String) view.getTag(R.id.auto_track_tag_view_fragment_name);
        String activityName = activity.getClass().getCanonicalName();
        if (!TextUtils.isEmpty(fragmentName) && !TextUtils.isEmpty(activityName)) {
            return String.format(Locale.getDefault(), "%s|%s", activityName, fragmentName);
        }
        if (!TextUtils.isEmpty(fragmentName)) {
           return fragmentName;
        }
        if (!TextUtils.isEmpty(activityName)) {
            return activityName;
        }
        return null;
    }

    /**
     * 从View中利用context获取所属Activity的名字
     */
    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        return getActivityFromContext(context);
    }

    public static Activity getActivityFromContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            //Activity有可能被系统＂装饰＂，看看context.base是不是Activity
            context = ((ContextWrapper) context).getBaseContext();
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }

    public static void mergeJsonObject(JSONObject source, JSONObject dest) {
        try {
            Iterator<String> keys = source.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = source.get(key);
                if (value instanceof Date) {
                    dest.put(key, FORMAT.get().format((Date) value));
                } else {
                    dest.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ThreadLocal<DateFormat> FORMAT = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        }
    };
}
