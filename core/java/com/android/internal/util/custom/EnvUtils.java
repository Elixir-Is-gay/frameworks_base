package com.android.internal.util.custom;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.SigningInfo;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class EnvUtils {
    private static final String TAG = "ElixirUtils";

    public boolean checkApplicationSignature(Context mContext, String packageName, String uniqueSerial) {
        try {
            PackageInfo currentPackageInfo = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            SigningInfo currentSigningInfo = currentPackageInfo.signingInfo;
            if (currentSigningInfo != null) {
                android.content.pm.Signature[] signatures = currentSigningInfo.getApkContentsSigners();
                for (android.content.pm.Signature signature : signatures) {
                    byte[] signatureBytes = signature.toByteArray();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(signatureBytes);
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                    X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                    String serial = certificate.getSerialNumber().toString();
                    if (serial.equals(uniqueSerial)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking signature", e);
            return false;
        }
        return false;
    }

    public boolean checkApkSignature(Context mContext, String apkFilePath, String serialNumber) {
        try {
            FileInputStream fis = new FileInputStream(apkFilePath);
            byte[] buffer = new byte[fis.available()];
            int bytesRead = fis.read(buffer);
            fis.close();
            if (bytesRead != -1) {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(apkFilePath, PackageManager.GET_SIGNING_CERTIFICATES);
                SigningInfo signingInfo = packageInfo.signingInfo;

                if (signingInfo != null) {
                    android.content.pm.Signature[] signatures = signingInfo.getApkContentsSigners();
                    for (android.content.pm.Signature signature : signatures) {
                        byte[] signatureBytes = signature.toByteArray();
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(signatureBytes);
                        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                        String serial = certificate.getSerialNumber().toString();
                        if (serial.equals(serialNumber)) {
                            return true;
                        }
                    }
                } else {
                    Log.w(TAG, "No signing info found");
                    return false;
                }
            } else {
                Log.e(TAG, "Failed to read APK file");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading APK file", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking signature", e);
            return false;
        }
        return false;
    }

}
