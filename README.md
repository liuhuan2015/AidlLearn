# AidlLearn
一个aidl的简单使用demo
>我的aidl知识隔一段时间就忘完了，于是就来github写一下。就是这样。
#### 一、aidl是什么
aidl，android interface definition language，字面翻译意思是安卓接口定义语言，用途是进行进程间通信，即IPC，inter process communication，在一个进程内调用另外一个进程中的方法。它是谷歌封装的规范，让程序员不需要知道底层的实现，就可以很方便的进行进程间通信的操作。<br>
一些概念:<br>
本地服务和远程服务<br>
本地服务：服务的代码在当前应用程序的内部<br>
远程服务：服务的代码在另外一个程序的内部<br>
aidl的使用场景：一些应用需要提供给其它应用一些服务的接口，使其它应用可以使用本应用的一些功能，比如支付宝对外暴露接口，使一些外部应用可以通过aidl来调起支付宝的支付功能。

#### 二、基本的代码编写流程

##### 1.在RemoteService工程里面编写一个aidl文件，在AS中可以右键新建aidl文件的。
    package com.liuh.aidllearn;

    // Declare any non-default types here with import statements

    interface IService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void callMethodInRemoteService();
    }
新建完成后，编译一下项目，项目会自动生成一个对应的接口类。

    IService.aild  --------编译后会生成-------->IService.java
##### 2.在RemoteService工程里面编写一个RemoteService，继承Service，重写onBind方法，在这个方法中返回一个IBinder对象，这个对象就是用来进行数据传输的。

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
##### 3.在CallRemoteService工程里面使用隐式意图来bind一个RemoteService。
    
        public void bind(View view) {
        MyServiceConnection conn = new MyServiceConnection();

        Intent intent = new Intent();
        intent.setAction("com.liuh.aidllearn.RemoteService");
        intent.setPackage("com.liuh.aidllearn");
        bindService(intent, conn, BIND_AUTO_CREATE);//BIND_AUTO_CREATE,调用者被销毁后，服务也将随之被销毁
    }
    
在绑定成功后，会触发onServiceConnected方法，在其中我们可以拿到一个远程服务的操作接口(IInterface的实现类)的对象，拿着这个对象我们就能调起远程服务里面的方法了。
    
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iService = IService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    
##### 4.需要注意的地方：要使用远程服务，需要把远程服务中的aidl文件复制到本地项目中，并且aidl文件的包名要和远程服务中的aidl文件的包名一致。

#### 三、aidl中涉及到的一些类的解释
IBinder/IInterface/Binder/BinderProxy/Stub
##### 1.IBinder
    public interface IBinder {
      ...
    }
IBinder是一个接口，它代表了一种跨进程传输的能力,它负责数据传输。只要某个类实现了这个接口，那么这个类的对象就可以进行跨进程的传递；这是驱动底层支持的，在跨进程数据流经过
驱动的时候，驱动会识别IBinder类型的数据，从而自动完成不同进程Binder本地对象和Binder代理对象的转换。
##### 2.IInterface
    public interface IInterface {
    IBinder asBinder();
    }
IInterface代表的就是远程Server对象具有什么能力，具体来说，就是aidl里面的接口，系统会将我们的aidl文件编译成java文件.
    
    public interface IService extends android.os.IInterface{
      ...
    }
##### 3.java层的Binder类，代表的就是Binder本地对象。BinderProxy类是Binder类的一个内部类，它代表远程进程的Binder对象的本地代理。这两个类都继承自IBinder,因而都具有跨进程传输的能力；实际上，在跨越进程的时候，Binder驱动会自动完成这两个对象的转换。<br>

##### 4.在使用aidl的时候，编译工具会给我们生成一个Stub的静态内部类，这个类继承自Binder，说明它是一个Binder本地对象，它实现了IInterface接口，说明它具有远程Server承诺给Client的一些功能；Stub是一个抽象类，具体的IInterface的相关实现需要我们手动完成，使用了策略模式。
    
    public static abstract class Stub extends android.os.Binder implements com.liuh.aidllearn.IService{
      ...
    }
#### 四、总结
aidl的固定模式：一个需要跨进程传递的对象一定继承自IBinder，如果是Binder本地对象，那么一定继承Binder，实现IInterface，如果是代理对象，那么就实现了IInterface并持有IBinder引用。<br>
Binder机制跨进程原理:<br>
Binder跨进程传输并不是真的把一个对象传输到了另外一个进程；传输过程好像是Binder跨进程穿越的时候，它在一个进程留下了一个真身，在另外一个进程幻化出一个影子（这个影子可以很多个）；Client进程的操作其实是对于影子的操作，影子利用Binder驱动最终让真身完成操作。<br>
Android系统实现这种机制使用的是代理模式, 对于Binder的访问，如果是在同一个进程（不需要跨进程），那么直接返回原始的Binder实体；如果在不同进程，那么就给他一个代理对象（影子）；我们在系统源码以及AIDL的生成代码里面可以看到很多这种实现。<br>
一句话总结就是：Client进程只不过是持有了Server端的代理；代理对象协助驱动完成了跨进程通信。<br>
最后附上两张图<br>
![Binder通信模型](https://github.com/liuhuan2015/AidlLearn/blob/master/images/Binder%E9%80%9A%E4%BF%A1%E6%A8%A1%E5%9E%8B.png)<br>

![Binder通信机制](https://github.com/liuhuan2015/AidlLearn/blob/master/images/BInder%E9%80%9A%E4%BF%A1%E6%9C%BA%E5%88%B6.png)<br>















    
    
    
