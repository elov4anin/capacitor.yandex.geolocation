package com.ver.yandex.locator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.ver.yandex.locator.services.Locator;
import com.ver.yandex.locator.services.PostCallbackInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

@NativePlugin(
    requestCodes={YandexLocatorPlugin.REQUEST_LOCATION_PERMISSION},
    permissions = {
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET
    }
)
public class YandexLocatorPlugin extends Plugin
{
    private Locator locator;
    static final int REQUEST_LOCATION_PERMISSION = 9874;

    @PluginMethod()
    public void echo(PluginCall call) {
        saveCall(call);
        locator = new Locator(getContext());

        if (!call.getString("version").isEmpty()) {
            locator.version = call.getString("version");
        }

        if (!call.getString("url").isEmpty()) {
            locator.url = call.getString("url");
        }

        if (!call.getString("api_key").isEmpty()) {
            locator.apiKey = call.getString("api_key");
        }

        if (hasRequiredPermissions()) {
            this.prepareRequestData();
        } else {
            pluginRequestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INTERNET
            }, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        PluginCall savedCall = getSavedCall();

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            this.prepareRequestData();
        }
    }

    private void prepareRequestData()
    {
        PluginCall savedCall = getSavedCall();
        this.locator.prepareRequestData();

        this.locator.sendPost(new PostCallbackInterface() {
            @Override
            public void success(String code, String message) {
                bridge.triggerWindowJSEvent("yandexLocation",
                        "{'code': " + code + "', 'data': '" + message + "' }");
            }
        });

        savedCall.success(this.locator.result);
    }


}
