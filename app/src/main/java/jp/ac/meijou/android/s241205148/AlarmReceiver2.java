package jp.ac.meijou.android.s241205148;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver2 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        String title;
        String message;

        if ("NORMAL_ALARM".equals(action)) {
            Log.d("AlarmReceiver", "📢 本番アラーム発火！");
            title = "アラーム通知";
            message = "設定した時刻になりました！";
        } else {
            Log.d("AlarmReceiver", "❓ 不明なアラーム発火！");
            title = "アラーム通知（不明）";
            message = "不明なアラームが発火しました。";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("AlarmReceiver", "通知権限なし");
            return;
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
