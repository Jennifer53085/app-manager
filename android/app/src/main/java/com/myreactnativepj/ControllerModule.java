package com.myreactnativepj;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.List;
import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.app.PendingIntent;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.Manifest;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.util.Base64;

public class ControllerModule extends ReactContextBaseJavaModule {

    private static final int PERMISSION_REQUEST_CODE = 1; // 申請權限
    private Context context = getReactApplicationContext();
    private PackageManager packageManager = context.getPackageManager();
    private PackageInstaller packageInstaller = packageManager.getPackageInstaller();
    private int sdkVersion = Build.VERSION.SDK_INT;// 偵測SDK版本號

    ControllerModule(ReactApplicationContext context) {
        super(context);
    }

    private String drawableToBase64(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public String getName() {
        return "ControllerModule";
    }

    //
    @ReactMethod
    public void searchApps(Callback callback) {
        if (sdkVersion >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.QUERY_ALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(),
                        new String[] { Manifest.permission.QUERY_ALL_PACKAGES }, PERMISSION_REQUEST_CODE);
                Log.d("ControllerModule", "Permission not granted");
            } else {
                Log.d("ControllerModule", "Permission already");
            }
        }

        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        JSONArray jsonArray = new JSONArray();
        for (ApplicationInfo appInfo : apps) {
            // 過濾系統及應用程式
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("packageName", appInfo.packageName);
                jsonObject.put("appName", packageManager.getApplicationLabel(appInfo).toString());
                jsonObject.put("isSystem", appInfo.FLAG_SYSTEM);
                // convert the appIcon to a base64 string or another suitable format before
                // adding it to JSON
                Drawable drawableIcon = packageManager.getApplicationIcon(appInfo);
                if (drawableIcon != null) {
                    String base64Icon = drawableToBase64(bitmap);
                    jsonObject.put("appIcon", drawableIcon);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                callback.invoke(e);
            } catch (IllegalArgumentException e) {
                callback.invoke(e);
            }
            jsonArray.put(jsonObject);

        }

        String appListString = jsonArray.toString();

        // Call the callback function and pass the JSON string to the frontend
        callback.invoke(appListString);
    }

    @ReactMethod
    public void installApp(String filePath, Callback callback) {
        File file = File(filePath);
        Uri apkUri = FileProvider.getUriForFile(reactContext, "${reactContext.packageName}.fileprovider",
                File(filePath));
        Log.d("INSTALL", apkUri.toString());
        Intent intent = Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        reactContext.startActivity(intent);
    }

    
    @ReactMethod
    public void deleteApp(String packageName, Callback statusCallback) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.REQUEST_DELETE_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getCurrentActivity(),
                    new String[] { Manifest.permission.REQUEST_DELETE_PACKAGES }, PERMISSION_REQUEST_CODE);
            Log.d("ControllerModule", "Permission not granted");
        } else {
            Log.d("ControllerModule", "Permission already");

            // 該方法為由client端進行刪除(會跳出對話框)
            try {
                Uri packageUri = Uri.parse("package:" + packageName);
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 添加此行以確保在非Activity上下文中啟動
                context.startActivity(intent, null);
                // 回傳刪除成功訊息
                statusCallback.invoke("Delete Success");
            } catch (Exception e) {
                e.printStackTrace();
                // 回傳刪除失敗訊息
                statusCallback.invoke("Delete Fail");
            }
            try {
                packageInstaller.uninstall(packageName, PendingIntent
                        .getActivity(context.getApplicationContext(), 0, new Intent(), 0).getIntentSender());
                statusCallback.invoke("Delete Success");
            } catch (Exception e) {
                Log.e("ControllerModule", "Delete Fail: " + e.getMessage());
            }
        }
    }
   
}