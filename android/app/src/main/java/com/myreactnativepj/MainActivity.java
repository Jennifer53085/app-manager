package com.myreactnativepj;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

import android.os.Bundle;
import androidx.annotation.Nullable;

public class MainActivity extends ReactActivity {
    /**
     * Returns the name of the main component registered from JavaScript. This is
     * used to schedule
     * rendering of the component.
     */

    @Override
    protected @Nullable String getMainComponentName() {
        return "MyReactNativePJ";
    }

    /**
     * Returns the instance of the [ReactActivityDelegate]. We use
     * [DefaultReactActivityDelegate]
     * which allows you to enable New Architecture with a single boolean flags
     * [fabricEnabled]
     */
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                return new ReactRootView(MainActivity.this);
            }
        };
    }
}
