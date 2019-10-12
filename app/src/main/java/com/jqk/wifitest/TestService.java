package com.jqk.wifitest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TestService extends Service {
    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
