package jp.ac.meijou.android.s241205148;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

import jp.ac.meijou.android.s241205148.databinding.ActivityMain11Binding;
import jp.ac.meijou.android.s241205148.databinding.ActivityMainBinding;

public class MainActivity11 extends AppCompatActivity {

    private ActivityMain11Binding binding;
    private Calendar alarmTime; // 選択したアラーム時刻を保持

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMain11Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        // ✅ 通知権限リクエスト（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1000
                );
            }
        }

        // ✅ 通知チャンネル作成（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default", "通知テスト",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // ✅ Android 12+ 正確なアラーム権限チェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        // 🔹「時間を選択」ボタン
        binding.selectTimeButton.setOnClickListener(view -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (timePicker, hourOfDay, minute) -> {
                        // 5分単位に丸める
                        int adjustedMinute = (minute / 5) * 5;

                        alarmTime = Calendar.getInstance();
                        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarmTime.set(Calendar.MINUTE, adjustedMinute);
                        alarmTime.set(Calendar.SECOND, 0);

                        // 過去の場合は翌日に
                        if (alarmTime.before(now)) {
                            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        String timeText = String.format("%02d:%02d", hourOfDay, adjustedMinute);
                        binding.selectedTime.setText("選択中: " + timeText);
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );
            dialog.show();
        });

        // 🔹 スイッチ ON/OFF
        binding.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (alarmTime != null) {
                    setAlarm(alarmTime);
                } else {
                    Log.d("MainActivity11", "⚠ アラーム時間が未設定です");
                    binding.alarmSwitch.setChecked(false);
                }
            } else {
                cancelAlarm();
            }
        });
    }

    // アラームをセット
    private void setAlarm(Calendar calendar) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("NORMAL_ALARM");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
            Log.d("MainActivity11", "✅ アラームをセットしました: " + calendar.getTime());
        }
    }

    // アラームをキャンセル
    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("MainActivity11", "❌ アラームをキャンセルしました");
        }
    }
}
