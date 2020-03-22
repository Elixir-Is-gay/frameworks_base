/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.shared.clocks;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.systemui.customization.R;

public class ClockLayout extends FrameLayout {

    private static final String TAG = "ElixirClockLayout";
    private static final Boolean DEBUG = true;

    private View mDepthParentLayout;
    private View mDepthMainLayout;
    private View mDepthImageView;

    public ClockLayout(Context context) {
        this(context, null);
    }

    public ClockLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDepthParentLayout = findViewById(R.id.depthParentLayout);
        if (mDepthParentLayout != null) {
            dlog("mDepthParentLayout: Searching for mDepthMainLayout");
            mDepthMainLayout = mDepthParentLayout.findViewById(R.id.depthMainLayout);
            mDepthImageView = mDepthParentLayout.findViewById(R.id.depthClockImageView);
        } else {
            elog("mDepthParentLayout is null!");
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int currentMargin = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LSCLOCK_DEPTH_CLOCK_AUTO_MARGIN, 700);
        int currentImageMargin = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LSCLOCK_DEPTH_CLOCK_AUTO_IMAGE_MARGIN, 0);
        adjustBottomMargin(currentMargin);
        if (currentImageMargin != 0) {
            adjustImageViewBottomMargin(currentImageMargin);
        }
    }

    public void adjustImageViewBottomMargin(int newBottomMargin) {
        if (mDepthImageView != null) {
            try {
                dlog("mDepthImageView is found! Changing its padding...");
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDepthImageView.getLayoutParams();
                if (params.bottomMargin != newBottomMargin) {
                    dlog("Settings newBottomMargin of depth clock imageview to :- " + newBottomMargin + ", Old margin was :- " + params.bottomMargin);
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, newBottomMargin);
                    mDepthImageView.setLayoutParams(params);
                } else {
                    dlog("Skip setting bottom margin because new margin is same as old!");
                }
            } catch (Exception e) {
                Log.e(TAG, "adjustBottomMargin: Error applying margin \n" + e.getMessage());
            }
        } else {
            elog("mDepthImageView is null!");
        }

    }

    public void adjustBottomMargin(int newBottomMargin) {
        if (mDepthMainLayout != null) {
            try {
                dlog("mDepthMainLayout is found! Changing its padding...");
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDepthMainLayout.getLayoutParams();
                if (params.bottomMargin != newBottomMargin) {
                    dlog("Settings newBottomMargin of depth clock to :- " + newBottomMargin + ", Old margin was :- " + params.bottomMargin);
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, newBottomMargin);
                    mDepthMainLayout.setLayoutParams(params);
                } else {
                    dlog("Skip setting bottom margin because new margin is same as old!");
                }
            } catch (Exception e) {
                Log.e(TAG, "adjustBottomMargin: Error applying margin \n" + e.getMessage());
            }
        } else {
            elog("mDepthMainLayout is null!");
        }

    }

    private void dlog(String str) {
        if (DEBUG) {
            Log.i(TAG, str);
        }
    }

    private void elog(String str) {
        Log.e(TAG, str);
    }

}
