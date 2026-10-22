package jp.sutemag.aichoukan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Sets the daily 7:00 (Japan time) alarm that checks for the new edition. */
final class Scheduler {
    static final TimeZone JST = TimeZone.getTimeZone("Asia/Tokyo");
    static final String EXTRA_ATTEMPT = "attempt";
    private static final int REQUEST_CODE = 100;

    private Scheduler() {}

    static long nextMorning() {
        Calendar cal = Calendar.getInstance(JST);
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= now) cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    static void scheduleNextMorning(Context context) {
        set(context, nextMorning(), 0);
    }

    /** Checks again in 10 minutes when today's edition hasn't arrived yet. */
    static void scheduleRetry(Context context, int attempt) {
        set(context, System.currentTimeMillis() + 10L * 60L * 1000L, attempt);
    }

    static String todayJst() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(JST);
        return f.format(new Date());
    }

    private static void set(Context context, long when, int attempt) {
        Intent intent = new Intent(context, MorningReceiver.class).putExtra(EXTRA_ATTEMPT, attempt);
        PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
    }
}
