package io.flutter.plugins.videoplayer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.exoplayer2.util.NotificationUtil;

public class MediaPlayerService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "MediaPlayerService_1100";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消Notification
        if (notificationManager != null)
            notificationManager.cancel(NOTIFICATION_PENDING_ID);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        setupNotification();
        silentForegroundNotification();
    }

    // 定义Binder类-当然也可以写成外部类
    private ServiceBinder serviceBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        Service getService() {
            return MediaPlayerService.this;
        }
    }

    private static MediaPlayerService bindService;
    private static boolean isBindService = false;
    private static Context context;

    // 绑定服务 必须先调用 registerReceiver
    public static void bindService(Context context) {
        MediaPlayerService.context = context;
        if (!MediaPlayerService.isBindService) {
            Intent intent = new Intent(context, MediaPlayerService.class);
            /*
             * Service：Service的桥梁
             * ServiceConnection：处理链接状态
             * flags：BIND_AUTO_CREATE, BIND_DEBUG_UNBIND, BIND_NOT_FOREGROUND, BIND_ABOVE_CLIENT, BIND_ALLOW_OOM_MANAGEMENT, or BIND_WAIVE_PRIORITY.
             */
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    // 解除绑定
    public static void unBind() {
        if (isBindService) {
            bindService.onDestroy();
            context.unbindService(serviceConnection);
            isBindService = false;
        }
    }

    /**
     * serviceConnection是一个ServiceConnection类型的对象，它是一个接口，用于监听所绑定服务的状态
     */
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * 该方法用于处理与服务已连接时的情况。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiceBinder binder = (ServiceBinder) service;
            bindService = (MediaPlayerService) binder.getService();
            isBindService = true;
        }

        /**
         * 该方法用于处理与服务断开连接时的情况。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindService = null;
        }

    };

    private static final int NOTIFICATION_PENDING_ID = 1;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private void setupNotification() {
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("");
        builder.setContentText("");

        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);
        builder.setShowWhen(true);
        builder.setSmallIcon(R.drawable.ic_launcher);

        // 获取NotificationManager实例
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "后台录轨迹的通知", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_PENDING_ID, builder.build());
        // 前台服务
        startForeground(NOTIFICATION_PENDING_ID, builder.build());
    }
    @TargetApi(26)
    private void silentForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle("");
            builder.setContentText("");

            builder.setDefaults(Notification.DEFAULT_ALL);
            builder.setAutoCancel(true);
            builder.setShowWhen(true);
            builder.setSmallIcon(R.drawable.ic_launcher);

            // 这里两个通知使用同一个id且必须按照这个顺序后调用startForeground
            int id = NOTIFICATION_PENDING_ID;
            NotificationManagerCompat.from(this).notify(id, builder.build());
            startForeground(id, builder.build());
        }
    }
}
