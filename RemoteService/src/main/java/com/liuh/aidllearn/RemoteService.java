package com.liuh.aidllearn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Date: 2018/5/8 10:00
 * Description:远程服务，提供给其他程序调用，以使用本应用内的一些功能
 */

public class RemoteService extends Service {

    //当服务被绑定后，这个IBinder是要返回给远程调用者的
    private IBinder iBinder = new IService.Stub() {
        @Override
        public void callMethodInRemoteService() throws RemoteException {
            methodInRemoteService();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("远程服务被创建了");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("远程服务被销毁了");
    }

    public void methodInRemoteService() {
        System.out.println("我是远程服务中的方法，我被调用了");
    }

}
