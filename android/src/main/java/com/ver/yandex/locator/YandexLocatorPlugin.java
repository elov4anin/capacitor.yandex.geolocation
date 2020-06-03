package com.ver.yandex.locator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.Calendar;

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

        savedCall.success(result);
    }

    private void sendYandexRequest()
    {

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
