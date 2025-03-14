/**
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

package com.android.systemui.biometrics;

import static android.content.Intent.ACTION_USER_SWITCHED;

import static com.android.systemui.doze.util.BurnInHelperKt.getBurnInOffset;
import static com.android.systemui.doze.util.BurnInHelperKt.getBurnInProgressOffset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintSensorPropertiesInternal;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayUtils;
import android.util.Log;
import android.util.MathUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.graphics.Rect;

import com.android.internal.util.custom.customUtils;
import com.android.systemui.Dependency;
import com.android.systemui.res.R;
import com.android.systemui.biometrics.AuthController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.KeyguardStateController;

import java.util.Random;

public class UdfpsAnimation extends ImageView {

    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "UdfpsAnimations";

    private boolean mShowing = false;
    private Context mContext;
    private int mAnimationSize;
    private AnimationDrawable recognizingAnim;

    private final WindowManager.LayoutParams mAnimParams = new WindowManager.LayoutParams();
    private WindowManager mWindowManager;

    private boolean mIsKeyguard;
    private boolean mIsRandomizationEnabled = false;
    private int mAnimationCount = 0;
    private Random mRandom = new Random();

    private final int mMaxBurnInOffsetX;
    private final int mMaxBurnInOffsetY;

    private static final String UDFPS_ANIMATIONS_PACKAGE = "com.custom.udfps.resources";

    private final KeyguardStateController mKeyguardStateController;
    private final AuthController mAuthController;
    private final FingerprintSensorPropertiesInternal mProps;

    private boolean mAnimationAdded = false;

    private final KeyguardStateController.Callback keyguardStateCallback = new KeyguardStateController.Callback() {
        @Override
        public void onKeyguardFadingAwayChanged() {
            removeAnimation();
        }
        @Override
        public void onKeyguardGoingAwayChanged() {
            removeAnimation();
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                updateAnimationStyle();
            }           
        }
    };

    public UdfpsAnimation(Context context, WindowManager windowManager,
           FingerprintSensorPropertiesInternal props, AuthController authController) {
        super(context);
        mContext = context;
        mAuthController = authController;
        mProps = props;

        mWindowManager = windowManager;
        
        mKeyguardStateController = Dependency.get(KeyguardStateController.class);

        float scaleFactor = getDisplayFactor();

        mMaxBurnInOffsetX = (int) (context.getResources()
            .getDimensionPixelSize(R.dimen.udfps_burn_in_offset_x) * scaleFactor);
        mMaxBurnInOffsetY = (int) (context.getResources()
            .getDimensionPixelSize(R.dimen.udfps_burn_in_offset_y) * scaleFactor);

        mAnimationSize = mContext.getResources().getDimensionPixelSize(R.dimen.udfps_animation_size);

        mAnimParams.height = mAnimationSize;
        mAnimParams.width = mAnimationSize;

        mAnimParams.format = PixelFormat.TRANSLUCENT;
        mAnimParams.type = WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY; // it must be behind Udfps icon
        mAnimParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mAnimParams.gravity = Gravity.TOP | Gravity.CENTER;

        updatePosition();

        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private void addAnimation() {
        if (!mAnimationAdded) {
            IntentFilter filter = new IntentFilter(ACTION_USER_SWITCHED);
            mContext.registerReceiverAsUser(mIntentReceiver, UserHandle.ALL, filter, null, null);

            updateAnimationStyle();

            mKeyguardStateController.addCallback(keyguardStateCallback);
            mAnimationAdded = true;
        }

    }

    private void updateAnimationStyle() {
        int value = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.UDFPS_ANIM_STYLE, 0, UserHandle.USER_CURRENT);
        mContext.getMainExecutor().execute(() -> {
            updateAnimationStyle(value);
        });
    }

    private void updateAnimationStyle(int styleIdx) {
        Drawable bgDrawable = getBgDrawable(styleIdx);
        if (styleIdx == 0 || bgDrawable == null) {
            setBackground(null);
            recognizingAnim = null;
        } else if (mIsRandomizationEnabled) {
            randomize();
        } else {
            setBackground(bgDrawable);
            recognizingAnim = bgDrawable instanceof AnimationDrawable ? (AnimationDrawable) bgDrawable : null;
        }
    }

    private void updateAnimationStyleForRandomization(int styleIdx) {
        if (styleIdx == 0) {
            styleIdx = styleIdx+1;
        }
        Drawable bgDrawable = getBgDrawable(styleIdx);
        if (bgDrawable == null) {
            setBackground(null);
            recognizingAnim = null;
        } else {
            setBackground(bgDrawable);
            recognizingAnim = bgDrawable instanceof AnimationDrawable ? (AnimationDrawable) bgDrawable : null;
        }
    }

    public void randomize() {
        if (!mIsRandomizationEnabled) {
            return;
        }
        Log.i(LOG_TAG, "Randomize animations called!");
        if (mAnimationCount == 0) {
            getBgDrawable(1);
        }
        if (customUtils.getExclusive(mContext.getContentResolver())) {
            final int random = mRandom.nextInt(mAnimationCount);
            Log.i(LOG_TAG, "Settings current udfps animation to :- " + random + ", Total animations :- " + mAnimationCount);
            mContext.getMainExecutor().execute(() -> {
                updateAnimationStyleForRandomization(random);
            });
        } else {
            Log.i(LOG_TAG, "Random animation not enabled!");
        }
    }

    private Drawable getBgDrawable(int styleIdx) {
        String[] mStyleNames = null;
        Resources mApkResources = null;
        try {
            mApkResources = mContext.getPackageManager().getResourcesForApplication(UDFPS_ANIMATIONS_PACKAGE);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed to load package resources", e);
            return null;
        }
        if (mApkResources != null) {
            int res = mApkResources.getIdentifier("udfps_animation_styles",
                    "array", UDFPS_ANIMATIONS_PACKAGE);
            mStyleNames = mApkResources.getStringArray(res);
        }
        if (mStyleNames == null || styleIdx >= mStyleNames.length) {
            return null;
        }
        mAnimationCount = mStyleNames.length;
        mIsRandomizationEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.UDFPS_ANIM_RANDOMIZE, 0, UserHandle.USER_CURRENT) != 0;
        String drawableName = mStyleNames[styleIdx];
        if (DEBUG) Log.i(LOG_TAG, "Updating animation style to:" + drawableName);
        try {
            int resId = mApkResources.getIdentifier(drawableName, "drawable", UDFPS_ANIMATIONS_PACKAGE);
            if (DEBUG) Log.i(LOG_TAG, "Got resource id: "+ resId +" from package");
            return mApkResources.getDrawable(resId);
        } catch (Resources.NotFoundException e) {
            Log.w(LOG_TAG, "Drawable resource not found: " + drawableName, e);
            return null;
        }
    }

    public boolean isAnimationEnabled() {
        return recognizingAnim != null;
    }
    
    private float getDisplayFactor() {
        return DisplayUtils.getScaleFactor(mContext);
    }
    
    public void updatePosition() {
        Point displaySize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(displaySize);
        boolean isFullResolution = displaySize.y > 3000; 
        Point udfpsLocation = mAuthController.getUdfpsLocation();
        float scaleFactor = getDisplayFactor();
        float udfpsRadius = isFullResolution ? mAuthController.getUdfpsRadius() : mProps.getLocation().sensorRadius;
        float udfpsLocationY = isFullResolution && udfpsLocation != null ? udfpsLocation.y : mProps.getLocation().sensorLocationY;
        int animationOffset = (int) (mContext.getResources().getDimensionPixelSize(R.dimen.udfps_animation_offset) * scaleFactor);
        mAnimParams.y = (int) (udfpsLocationY * scaleFactor) - (int) (udfpsRadius * scaleFactor)
                        - (mAnimationSize / 2) + animationOffset;
        if (DEBUG) {
            Log.d(LOG_TAG, "updatePosition: displaySize=" + displaySize + 
                           ", isFullResolution=" + isFullResolution + 
                           ", udfpsLocation=" + udfpsLocation + 
                           ", udfpsRadius=" + udfpsRadius + 
                           ", scaleFactor=" + scaleFactor + 
                           ", udfpsLocationY=" + udfpsLocationY + 
                           ", animationOffset=" + animationOffset + 
                           ", mAnimParams.y=" + mAnimParams.y);
        }
    }

    public void show() {
        if (mShowing || !mIsKeyguard || recognizingAnim == null) return;
        try {
            if (getWindowToken() == null) {
                mWindowManager.addView(this, mAnimParams);
            } else {
                mWindowManager.updateViewLayout(this, mAnimParams);
            }
            mShowing = true;
            recognizingAnim.start();
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Error adding view to WindowManager", e);
        }
    }

    public void hide() {
        if (!mShowing && getWindowToken() == null) return;
        try {
            if (recognizingAnim != null) {
                clearAnimation();
                recognizingAnim.stop();
                recognizingAnim.selectDrawable(0);
            }
            mWindowManager.removeView(this);
            mShowing = false;
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Error removing view from WindowManager", e);
        }
    }

    public void removeAnimation() {
        hide();
        if (mAnimationAdded) {
            mContext.unregisterReceiver(mIntentReceiver);
            mKeyguardStateController.removeCallback(keyguardStateCallback);
            mAnimationAdded = false;
        }
    }

    public void setIsKeyguard(boolean isKeyguard) {
        mIsKeyguard = isKeyguard;
        if (mIsKeyguard) {
            addAnimation();
        } else {
            removeAnimation();
        }
    }
}
