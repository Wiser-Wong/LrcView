package com.wiser.songlrcview;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class LrcApplication extends Application {

	@Override public void onCreate() {
		super.onCreate();
		LeakCanary.install(this);
	}
}
