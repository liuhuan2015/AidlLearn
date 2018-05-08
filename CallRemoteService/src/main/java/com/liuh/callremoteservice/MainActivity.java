package com.liuh.callremoteservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liuh.aidllearn.IService;

public class MainActivity extends AppCompatActivity {

    private IService iService;//远程服务

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void bind(View view) {

        MyServiceConnection conn = new MyServiceConnection();

        Intent intent = new Intent();
        intent.setAction("com.liuh.aidllearn.RemoteService");
        intent.setPackage("com.liuh.aidllearn");
        bindService(intent, conn, BIND_AUTO_CREATE);//BIND_AUTO_CREATE,调用者被销毁后，服务也将随之被销毁
    }

    public void callMethodInRemoteService(View view) {
        try {
            iService.callMethodInRemoteService();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iService = IService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
