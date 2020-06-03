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
        Manifest.permission.ACCESS_COARSE_LOCATION
    }
)
public class YandexLocatorPlugin extends Plugin
{
    static final int REQUEST_LOCATION_PERMISSION = 9874;
    private JSObject result = new JSObject();

    protected String version = "1.0";
    protected String url     = "http://api.lbs.yandex.net/geolocation";
    protected String apiKey  = "";

    @PluginMethod()
    public void echo(PluginCall call) {
        saveCall(call);

        if (!call.getString("version").isEmpty()) {
            this.version = call.getString("version");
        }

        if (!call.getString("url").isEmpty()) {
            this.url = call.getString("url");
        }

        if (!call.getString("api_key").isEmpty()) {
            this.apiKey = call.getString("api_key");
        }

        if (hasRequiredPermissions()) {
            this.prepareRequestData();
        } else {
            pluginRequestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
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
        this.result.put("gsm_cells", getGsmCellLocation());
        this.result.put("wifi_networks", getCurrentNetworkInfo());

        sendPost();

        savedCall.success(result);
    }

    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(YandexLocatorPlugin.this.url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();

                    JSONObject commonObj = new JSONObject();
                    commonObj.put("version", YandexLocatorPlugin.this.version);
                    commonObj.put("api_key", YandexLocatorPlugin.this.apiKey);
                    jsonParam.put("common", commonObj);

                    JSObject gsm_cells = (JSObject) YandexLocatorPlugin.this.result.get("gsm_cells");
                    if (gsm_cells.has("country")) {
                        JSONArray gsmCellsList = new JSONArray();
                        JSONObject gsmCellObj = new JSObject();
                        gsmCellObj.put("countrycode", gsm_cells.get("country"));
                        gsmCellObj.put("operatorid", gsm_cells.get("operatorId"));
                        gsmCellObj.put("cellid", gsm_cells.get("cid"));
                        gsmCellObj.put("lac", gsm_cells.get("lac"));
                        gsmCellsList.put(gsmCellObj);
                        jsonParam.put("gsm_cells", gsmCellsList);
                    }

                    JSObject wifi_networks = (JSObject) YandexLocatorPlugin.this.result.get("wifi_networks");
                    if (wifi_networks.has("mac")) {
                        JSONArray networkCellsList = new JSONArray();
                        JSONObject networkCellObj = new JSObject();
                        networkCellObj.put("mac", wifi_networks.get("mac"));
                        networkCellsList.put(networkCellObj);
                        jsonParam.put("wifi_networks", networkCellsList);
                    }

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    bridge.triggerWindowJSEvent("yandexLocation",
                            "{'code': " + String.valueOf(conn.getResponseCode()) + "', 'data': '" + conn.getResponseMessage() + "' }");

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private JSObject getGsmCellLocation() {
        final Calendar calendar = Calendar.getInstance();
        TelephonyManager telMgr = (TelephonyManager) getContext()
                .getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        final JSObject json = new JSObject();

        if (telMgr == null) {
            return json;
        }

        @SuppressLint("MissingPermission")
        GsmCellLocation gc = (GsmCellLocation) telMgr.getCellLocation();

        if(gc != null){
            String operator = telMgr.getNetworkOperator();
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));

            json.put("country", telMgr.getSimCountryIso());
            json.put("operatorId", telMgr.getSimOperator());
            json.put("timestamp", calendar.getTimeInMillis());
            json.put("cid", gc.getCid());
            json.put("lac", gc.getLac());
            json.put("psc", gc.getPsc());
            json.put("mcc", mcc);
            json.put("mnc", mnc);
        }

        return json;
    }

    @SuppressLint("HardwareIds")
    private JSObject getCurrentNetworkInfo() {
        final JSObject json = new JSObject();
        ConnectivityManager cm = (ConnectivityManager) getContext()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return json;
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return json;
        }

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) getContext()
                    .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return json;
            }

            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && connectionInfo.getSSID() != null) {
                json.put("ssid", connectionInfo.getSSID());
                json.put("mac", connectionInfo.getMacAddress());
                json.put("ip", connectionInfo.getIpAddress());
            }
        }

        return json;
    }
}
