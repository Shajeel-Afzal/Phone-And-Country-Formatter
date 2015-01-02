package com.shajeelafzal.phoneandcountryformattersample;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

	public static volatile Context mApplicationContext = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mApplicationContext = getApplicationContext();
	}

}
