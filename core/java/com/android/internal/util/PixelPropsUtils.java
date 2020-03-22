/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *               2022 ReloadedOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.android.internal.util;

import android.app.ActivityTaskManager;
import android.app.Application;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Binder;
import android.os.Process;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.util.custom.customUtils;
import com.android.internal.R;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.android.internal.util.custom.JSONFetcher;

/**
 * @hide
 */
public final class PixelPropsUtils {

    private static final String TAG = "PixelPropsUtils";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_KEYS = true;
    private static final boolean DEBUG_PACKAGES = false;

    private static String BRAND = "google";
    private static String MANUFACTURER = "Google";
    private static String ID = "BP21.241121.009";
    private static String DEVICE = "tokay";
    private static String PRODUCT = "tokay_beta";
    private static String MODEL = "Pixel 9";
    private static String FINGERPRINT = "google/tokay_beta/tokay:Baklava/BP21.241121.009/12787338:user/release-keys";
    private static String TYPE = "user";
    private static String TAGS = "release-keys";
    private static String SECURITY_PATCH = "2024-12-05";
    private static int API_LEVEL = 21;
    private static boolean shouldWait = false;

    private static final String[] sRecentPixelPackages = {
        "com.android.chrome",
        "com.breel.wallpapers20",
        "com.google.android.aicore",
        "com.google.android.apps.aiwallpapers",
        "com.google.android.apps.bard",
        "com.google.android.apps.customization.pixel",
        "com.google.android.apps.emojiwallpaper",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.apps.pixel.agent",
        "com.google.android.apps.pixel.creativeassistant",
        "com.google.android.apps.pixel.support",
        "com.google.android.apps.privacy.wildlife",
        "com.google.android.apps.subscriptions.red",
        "com.google.android.apps.wallpaper",
        "com.google.android.apps.wallpaper.pixel",
        "com.google.android.apps.weather",
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox",
        "com.google.android.wallpaper.effects",
        "com.google.pixel.livewallpaper",
        "com.nhs.online.nhsonline",
        "com.google.android.inputmethod.latin",
        "com.google.android.tts",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music"
    };

    private static final Map<String, Object> sRecentPixelProps = Map.of(
        "BRAND", "google",
        "MANUFACTURER", "Google",
        "DEVICE", "caiman",
        "HARDWARE", "caiman",
        "ID", "AP4A.241205.013.C1",
        "PRODUCT", "caiman",
        "MODEL", "Pixel 9 Pro",
        "FINGERPRINT", "google/caiman/caiman:15/AP4A.241205.013.C1/12657666:user/release-keys"
    );

    private static final Map<String, Object> sPixelProps = Map.of(
        "BRAND", "google",
        "MANUFACTURER", "Google",
        "DEVICE", "barbet",
        "HARDWARE", "barbet",
        "ID", "AP2A.240805.004",
        "PRODUCT", "barbet",
        "MODEL", "Pixel 5a",
        "FINGERPRINT", "google/barbet/barbet:14/AP2A.240805.005.S4/12281092:user/release-keys"
    );

    private static String getBuildID(String fingerprint) {
        Pattern pattern = Pattern.compile("([A-Za-z0-9]+\\.\\d+\\.\\d+\\.\\w+)");
        Matcher matcher = pattern.matcher(fingerprint);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static final Map<String, Object> sPixelXLProps =
            createGoogleSpoofProps("marlin", "Pixel XL",
                    "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");

    private static final Map<String, Object> sROG6Props = Map.of(
        "BRAND", "asus",
        "MANUFACTURER", "asus",
        "DEVICE", "AI2201",
        "MODEL", "ASUS_AI2201"
    );

    private static final Map<String, Object> sXP5Props = Map.of(
        "MANUFACTURER", "Sony",
        "MODEL", "SO-52A"
    );

    private static final Map<String, Object> sOP8PProps = Map.of(
        "MANUFACTURER", "OnePlus",
        "MODEL", "IN2020"
    );

    private static final Map<String, Object> sOP9PProps = Map.of(
        "MANUFACTURER", "OnePlus",
        "MODEL", "LE2123"
    );

    private static final Map<String, Object> s11TProps = Map.of(
        "MANUFACTURER", "Xiaomi",
        "MODEL", "21081111RG"
    );

    private static final Map<String, Object> s13PProps = Map.of(
        "BRAND", "Xiaomi",
        "MANUFACTURER", "Xiaomi",
        "MODEL", "2210132C"
    );

    private static final Map<String, Object> sF4Props = Map.of(
        "MANUFACTURER", "Xiaomi",
        "MODEL", "22021211RG"
    );

    private static final List<String> sExtraPackages = List.of(
        "com.android.chrome",
        "com.android.vending"
    );

    private static final List<String> packagesToChangeROG6 = List.of(
        "com.activision.callofduty.shooter",
        "com.ea.gp.fifamobile",
        "com.gameloft.android.ANMP.GloftA9HM",
        "com.madfingergames.legends",
        "com.mobile.legends",
        "com.pearlabyss.blackdesertm",
        "com.pearlabyss.blackdesertm.gl"
    );

    private static final List<String> packagesToChangeXP5 = List.of(
        "com.garena.game.codm",
        "com.tencent.tmgp.kr.codm",
        "com.vng.codmvn"
    );

    private static final List<String> packagesToChangeOP8P = List.of(
        "com.netease.lztgglobal",
        "com.pubg.krmobile",
        "com.rekoo.pubgm",
        "com.riotgames.league.wildrift",
        "com.riotgames.league.wildrifttw",
        "com.riotgames.league.wildriftvn",
        "com.tencent.ig",
        "com.tencent.tmgp.pubgmhd",
        "com.vng.pubgmobile",
        "com.pubg.imobile"
    );

    private static final List<String> packagesToChangeOP9P = List.of(
        "com.epicgames.fortnite",
        "com.epicgames.portal",
        "com.tencent.lolm"
    );

    private static final List<String> packagesToChange11T = List.of(
        "com.ea.gp.apexlegendsmobilefps",
        "com.levelinfinite.hotta.gp",
        "com.supercell.clashofclans",
        "com.vng.mlbbvn"
    );

    private static final List<String> packagesToChangeMI13P = List.of(
        "com.levelinfinite.sgameGlobal",
        "com.tencent.tmgp.sgame"
    );

    private static final List<String> packagesToChangeF4 = List.of(
        "com.dts.freefiremax",
        "com.dts.freefireth"
    );

    private static final List<String> sPackageWhitelist = List.of(
        "com.google.android.apps.motionsense.bridge",
        "com.google.android.apps.pixelmigrate",
        "com.google.android.apps.recorder",
        "com.google.android.apps.restore",
        "com.google.android.apps.tachyon",
        "com.google.android.apps.tycho",
        "com.google.android.apps.wearables.maestro.companion",
        "com.google.android.settings.intelligence",
        "com.google.android.apps.youtube.kids",
        "com.google.android.as",
        "com.google.android.dialer",
        "com.google.android.euicc",
        "com.google.android.setupwizard",
        "com.google.ar.core",
        "com.google.oslo"
    );

    private static final List<String> sFeatureBlacklist = List.of(
        "PIXEL_2017_PRELOAD",
        "PIXEL_2018_PRELOAD",
        "PIXEL_2019_MIDYEAR_PRELOAD",
        "PIXEL_2019_PRELOAD",
        "PIXEL_2020_EXPERIENCE",
        "PIXEL_2020_MIDYEAR_EXPERIENCE",
        "PIXEL_2021_EXPERIENCE",
        "PIXEL_2021_MIDYEAR_EXPERIENCE",
        "PIXEL_2022_EXPERIENCE",
        "PIXEL_2022_MIDYEAR_EXPERIENCE",
        "PIXEL_2023_EXPERIENCE",
        "PIXEL_2023_MIDYEAR_EXPERIENCE",
        "PIXEL_2024_EXPERIENCE",
        "PIXEL_2024_MIDYEAR_EXPERIENCE",
        "PIXEL_TABLET_2023_EXPERIENCE"
    );

    private static final String PACKAGE_PREFIX_GOOGLE = "com.google.android.";
    private static final String PACKAGE_GPHOTOS = "com.google.android.apps.photos";
    private static final String PACKAGE_FINSKY = "com.android.vending";
    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PROCESS_GMS_UNSTABLE = PACKAGE_GMS + ".unstable";
    private static final String SYS_GAMES_SPOOF = "persist.sys.pixelprops.games";
    private static final String ELIXIR_BLOCK_KEY_ATTESTAION = "persist.sys.elixir.block.keyattestation";

    private static volatile String sProcessName;

    private static volatile boolean sIsGms = false;
    private static volatile boolean sIsFullGms = false;
    private static volatile boolean sIsFinsky = false;
    private static volatile boolean sIsPhotos = false;
    private static volatile boolean sIsGamesPropEnabled = SystemProperties.getBoolean(SYS_GAMES_SPOOF, false);
    private static volatile boolean sIsKeyAttestationBlockEnabled = SystemProperties.getBoolean(ELIXIR_BLOCK_KEY_ATTESTAION, true);

    public static void setProps(Context context) {
        final String packageName = context.getPackageName();
        final String processName = Application.getProcessName();

        sProcessName = processName;

        if (DEBUG_PACKAGES) {
            Log.d(TAG, "setProps packageName=" + packageName + " processName=" + processName);
        }

        if (TextUtils.isEmpty(packageName) || processName == null
                || sPackageWhitelist.contains(packageName)) {
            return;
        }

        sIsGms = packageName.equals(PACKAGE_GMS) && processName.equals(PROCESS_GMS_UNSTABLE);
        sIsFullGms = packageName.equals(PACKAGE_GMS);
        sIsFinsky = packageName.equals(PACKAGE_FINSKY);
        sIsPhotos = packageName.equals(PACKAGE_GPHOTOS);

        if (sIsGms) {
            dlog("Spoofing build for GMS");
            setCertifiedPropsForGms();
        } else if (sIsPhotos) {
            dlog("Spoofing Pixel XL for Google Photos");
            sPixelXLProps.forEach(PixelPropsUtils::setPropValue);
        } else if ((Arrays.asList(sRecentPixelPackages).contains(packageName))) {
            dlog("Spoofing Pixel 9 Pro for :- " + packageName);
            sRecentPixelProps.forEach(PixelPropsUtils::setPropValue);
        } else if ((packageName.startsWith(PACKAGE_PREFIX_GOOGLE)
                && !packageName.toLowerCase().contains("camera"))
                || sExtraPackages.contains(packageName)) {
            dlog("Setting pixel props for: " + packageName + " process: " + processName);
            sPixelProps.forEach(PixelPropsUtils::setPropValue);
        }
        if (sIsFullGms) {
            dlog("Spoofing build time for GMS");
            setPropValue("TIME", System.currentTimeMillis());
        } 
        setGamesProp(packageName, processName);
    }

    private static void setCertifiedPropsForGms() {
        shouldWait = true;
        JSONFetcher.fetchDataAsync("https://raw.githubusercontent.com/nishant6342/PLAY_FP/main/fingerprint.json", new JSONFetcher.JsonCallback() {
            @Override
            public void onSuccess(String jsonData) {
                try {
                    Log.d(TAG, "Got value from server: " + jsonData);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    BRAND = jsonObject.getString("BRAND");
                    MANUFACTURER = jsonObject.getString("MANUFACTURER");
                    ID = jsonObject.getString("ID");
                    DEVICE = jsonObject.getString("DEVICE");
                    PRODUCT = jsonObject.getString("PRODUCT");
                    MODEL = jsonObject.getString("MODEL");
                    FINGERPRINT = jsonObject.getString("FINGERPRINT");
                    TYPE = "user";
                    TAGS = "release-keys";
                    SECURITY_PATCH = jsonObject.getString("VERSION:SECURITY_PATCH");
                    API_LEVEL = jsonObject.getInt("VERSION:API_LEVEL");
                    Log.d(TAG, "Received JSON from server :- " + "BRAND :- " + BRAND + "MANUFACTURER :- " + MANUFACTURER + "ID :- " + ID + "DEVICE :- " + DEVICE + "PRODUCT :- " + PRODUCT + "MODEL :- " + MODEL + "FINGERPRINT :- " + FINGERPRINT + "SECURITY_PATCH :- " + SECURITY_PATCH);
                    shouldWait = false;
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    shouldWait = false;
                } finally {
                    shouldWait = false;
                }
                shouldWait = false;
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching data", e);
                shouldWait = false;
            }
        });
        int count = 0;
        while(shouldWait) {
            count++;
            try{
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                // ignore
            }
            if (count == 10) { // wait 10 seconds ansd break the loop
                break;
            }
        }
        dlog("Applying certified props for GMS");
        setPropValue("BRAND", BRAND);
        setPropValue("MANUFACTURER", MANUFACTURER);
        setPropValue("ID", ID);
        setPropValue("DEVICE", DEVICE);
        setPropValue("PRODUCT", PRODUCT);
        setPropValue("MODEL", MODEL);
        setPropValue("FINGERPRINT", FINGERPRINT);
        setPropValue("TYPE", TYPE);
        setPropValue("TAGS", TAGS);
        setVersionFieldString("SECURITY_PATCH", SECURITY_PATCH);
        setVersionFieldInt("DEVICE_INITIAL_SDK_INT", API_LEVEL);
    }

    private static Map<String, Object> createGoogleSpoofProps(String device, String model, String fingerprint) {
        Map<String, Object> props = new HashMap<>();
        props.put("BRAND", "google");
        props.put("MANUFACTURER", "Google");
        props.put("ID", getBuildID(fingerprint));
        props.put("DEVICE", device);
        props.put("PRODUCT", device);
        props.put("MODEL", model);
        props.put("FINGERPRINT", fingerprint);
        props.put("TYPE", "user");
        props.put("TAGS", "release-keys");
        return props;
    }

    private static void setVersionFieldString(String key, String value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value);
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setGamesProp(String packageName, String processName) {
        if (!sIsGamesPropEnabled) {
            // Games prop switch is turned off
            return;
        }
        if (packagesToChangeROG6.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            sROG6Props.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChangeXP5.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            sXP5Props.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChangeOP8P.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            sOP8PProps.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChangeOP9P.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            sOP9PProps.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChange11T.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            s11TProps.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChangeMI13P.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            s13PProps.forEach(PixelPropsUtils::setPropValue);
        } else if (packagesToChangeF4.contains(packageName)) {
            dlog("Setting Games props for: " + packageName + " process: " + processName);
            sF4Props.forEach(PixelPropsUtils::setPropValue);
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            keylog("Setting prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionFieldInt(String key, int value) {
        try {
            dlog("Defining version field " + key + " to " + String.valueOf(value));
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set version field int " + key, e);
        }
    }

    private static void setVersionField(String key, Object value) {
        try {
            dlog("Defining version field " + key + " to " + value.toString());
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set version field " + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        if (!sIsKeyAttestationBlockEnabled) {
            dlog("Key attestation blocking disabled! Returning...");
        } else if (isCallerSafetyNet() || sIsFinsky) {
            dlog("Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }

    public static boolean hasSystemFeature(String name, boolean def) {
        if (sIsPhotos && def &&
                sFeatureBlacklist.stream().anyMatch(name::contains)) {
            dlog("Blocked system feature " + name + " for Google Photos");
            return false;
        }
        return def;
    }

    private static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, "[" + sProcessName + "] " + msg);
    }

    private static void keylog(String msg) {
        if (DEBUG_KEYS) Log.d(TAG, "[" + sProcessName + "] " + msg);
    }
}
