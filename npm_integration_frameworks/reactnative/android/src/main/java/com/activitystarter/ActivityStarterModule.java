package com.activitystarter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public final class ActivityStarterModule extends ReactContextBaseJavaModule {
    
    private String LOG_TAG  = "ORDER_RUN -- ActivityStarterModule";
    
    ActivityStarterModule(ReactApplicationContext context) {
        super(context);
        Log.d(LOG_TAG, "ActivityStarterModule constructor called");
    }

    @Override
    public String getName() {
        Log.d(LOG_TAG, "ActivityStarterModule::getName requested");
        return "ActivityStarterModule";
    }

    @ReactMethod
    void startActivityByName(String targetActivity) {
        Log.d(LOG_TAG, "ActivityStarterModule::startActivityByName");
        Activity activity = getCurrentActivity();
        try {
            Class<?> klass = Class.forName(targetActivity);
            Intent intent = new Intent(activity, klass);
            activity.startActivity(intent);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }
}
