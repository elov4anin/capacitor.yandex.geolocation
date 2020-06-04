package com.getcapacitor.android;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.ver.yandex.locator.YandexLocatorPlugin;
import com.ver.yandex.locator.services.Locator;
import com.ver.yandex.locator.services.PostCallbackInterface;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionNetRule = GrantPermissionRule .grant(android.Manifest.permission.ACCESS_NETWORK_STATE);

    @Rule
    public GrantPermissionRule mRuntimePermissionWifiRule = GrantPermissionRule .grant(Manifest.permission.ACCESS_WIFI_STATE);

    @Rule
    public GrantPermissionRule mRuntimePermissionFLRule = GrantPermissionRule .grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public GrantPermissionRule mRuntimePermissionCLRule = GrantPermissionRule .grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    @Rule
    public GrantPermissionRule mRuntimePermissionINETRule = GrantPermissionRule .grant(Manifest.permission.INTERNET);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Locator locator = new Locator(appContext);
        locator.prepareRequestData();

        locator.sendPost(new PostCallbackInterface() {
            @Override
            public void success(String code, String message) {
                Log.d("locator test", code);
                Log.d("locator test", message);
            }
        });
    }
}
