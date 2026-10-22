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

/** Sets the daily alarms (Japan time) that check for each paper's new edition. */
final class Scheduler {
    static final TimeZone JST = TimeZone.getTimeZone("Asia/Tokyo");
    static final String EXTRA_ATTEMPT = "attempt";
    static final String EXTRA_FEED = "feed";

    private Scheduler() {}

    static void scheduleAll(Context context) {
        for (Feed f : Feed.ALL) scheduleNext(context, f);
    }

    static long nextTime(Feed feed) {
        Calendar cal = Calendar.getInstance(JST);
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, feed.hour);
        cal.set(Calendar.MINUTE, feed.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= now) cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    static void scheduleNext(Context context, Feed feed) {
        set(context, feed, nextTime(feed), 0);
    }

    /** Checks again in 10 minutes when today's edition hasn't arrived yet. */
    static void scheduleRetry(Context context, Feed feed, int attempt) {
        set(context, feed, System.currentTimeMillis() + 10L * 60L * 1000L, attempt);
    }

    static String todayJst() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(JST);
        return f.format(new Date());
    }

    private static void set(Context context, Feed feed, long when, int attempt) {
        Intent intent = new Intent(context, MorningReceiver.class)
                .putExtra(EXTRA_FEED, feed.key)
                .putExtra(EXTRA_ATTEMPT, attempt);
        PendingIntent pi = PendingIntent.getBroadcast(context, feed.requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
    }
}
