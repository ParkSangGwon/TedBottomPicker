package gun0912.tedbottompickerdemo;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        setLeakCanary();
    }

    private void setLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
