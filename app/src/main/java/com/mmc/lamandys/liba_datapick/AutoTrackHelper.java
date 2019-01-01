package com.mmc.lamandys.liba_datapick;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.daijun.luffy_asm.R;
import com.mmc.lamandys.liba_datapick.core.AutoTrackUtil;
import com.mmc.lamandys.liba_datapick.core.LogConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019/1/1
 * @description
 */
public class AutoTrackHelper {
    private static SparseArray<Long> eventTimestamp = new SparseArray<>();

    /**
     * 防抖动
     */
    private static boolean isDebounceTrack(Object object) {
        boolean isDebounceTrack = false;
        long currentTimeMillis = System.currentTimeMillis();
        Long aLong = eventTimestamp.get(object.hashCode());
        if (aLong != null) {
            if (currentTimeMillis - aLong < 500) {
                isDebounceTrack = true;
            }
        }
        eventTimestamp.put(object.hashCode(), currentTimeMillis);
        return isDebounceTrack;
    }

    private static boolean isDebounceTrackForView(View view) {
        boolean isDebounceTrack = false;
        long currentTimeMillis = System.currentTimeMillis();
        String tag = (String) view.getTag(R.id.auto_track_tag_view_onclick_timestamp);
        if (!TextUtils.isEmpty(tag)) {
            long lastClickTimestamp = Long.parseLong(tag);
            if (currentTimeMillis - lastClickTimestamp < 500) {
                isDebounceTrack = true;
            }
        }
        view.setTag(R.id.auto_track_tag_view_onclick_timestamp, String.valueOf(currentTimeMillis));
        return isDebounceTrack;
    }

    /**
     * 对应的埋点方法{@link android.support.v4.app.Fragment#onViewCreated(View, Bundle)}
     *
     * @param object   Fragment对象
     * @param rootView 对应View对象
     * @param bundle   对应Bundle对象
     */
    public static void onFragmentViewCreated(Object object, View rootView, Bundle bundle) {
        System.out.println("测试：onFragmentViewCreated");
        if (!(object instanceof Fragment)) {
            return;
        }
        Fragment fragment = (Fragment) object;
        String fragmentName = fragment.getClass().getName();
        if (rootView instanceof ViewGroup) {
            AutoTrackUtil.traverseViewForFragment(fragmentName, (ViewGroup) rootView);
        } else {
            rootView.setTag(R.id.auto_track_tag_view_fragment_name, fragmentName);
        }
    }

    /**
     * 对应的埋点方法{@link android.support.v4.app.Fragment#onResume()}
     *
     * @param object Fragment对象
     */
    public static void trackFragmentResume(Object object) {
        if (!(object instanceof Fragment)) {
            return;
        }

        Fragment fragment = (Fragment) object;
        Fragment parentFragment = fragment.getParentFragment();
        if (parentFragment == null) {
            if (!fragment.isHidden() && fragment.getUserVisibleHint()) {
                trackFragmentAppViewScreen(fragment);
            }
        } else {

        }
    }

    /**
     * Fragment日志处理
     */
    private static void trackFragmentAppViewScreen(Fragment fragment) {
        try {
            String fragmentName = fragment.getClass().getCanonicalName();
            Activity activity = fragment.getActivity();
            JSONObject jsonObject = new JSONObject();
            if (activity != null) {
                String activityTitle = AutoTrackUtil.getActivityTitle(activity);
                if (!TextUtils.isEmpty(activityTitle)) {
                    jsonObject.put(LogConstants.AutoTrack.EVENT_SCAN_PAGE_TITLE, activityTitle);
                }
                String screenName = String.format(Locale.getDefault(), "%s|%s",
                        activity.getClass().getCanonicalName(), fragmentName);
                jsonObject.put(LogConstants.AutoTrack.SCREEN_NAME, screenName);
            } else {
                jsonObject.put(LogConstants.AutoTrack.SCREEN_NAME, fragmentName);
            }
            System.out.println("自动埋点:trackFragmentAppViewScreen:" + jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
