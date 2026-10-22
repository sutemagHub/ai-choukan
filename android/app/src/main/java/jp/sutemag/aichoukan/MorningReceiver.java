package jp.sutemag.aichoukan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

/** Runs at each paper's time: if today's edition is on GitHub, shows the notification; otherwise retries a few times. */
public class MorningReceiver extends BroadcastReceiver {
    private static final int MAX_RETRIES = 9; // about 90 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        final Feed feed = Feed.byKey(String.valueOf(intent.getStringExtra(Scheduler.EXTRA_FEED)));
        final int attempt = intent.getIntExtra(Scheduler.EXTRA_ATTEMPT, 0);
        final PendingResult result = goAsync();
        new Thread(() -> {
            try {
                String body = Net.fetch(feed.latestPath);
                JSONObject edition = body == null ? null : new JSONObject(body);
                if (edition != null && Scheduler.todayJst().equals(edition.optString("date"))) {
                    Notifier.showEdition(app, feed, edition);
                    Scheduler.scheduleNext(app, feed);
                } else if (attempt < MAX_RETRIES) {
                    Scheduler.scheduleRetry(app, feed, attempt + 1);
                } else {
                    Scheduler.scheduleNext(app, feed);
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) Scheduler.scheduleRetry(app, feed, attempt + 1);
                else Scheduler.scheduleNext(app, feed);
            } finally {
                result.finish();
            }
        }).start();
    }
}
