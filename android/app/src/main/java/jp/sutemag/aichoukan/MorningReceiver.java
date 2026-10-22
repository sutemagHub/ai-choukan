package jp.sutemag.aichoukan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

/** Runs at 7:00: if today's edition is on GitHub, shows the notification; otherwise retries a few times. */
public class MorningReceiver extends BroadcastReceiver {
    private static final int MAX_RETRIES = 9; // about 90 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        final int attempt = intent.getIntExtra(Scheduler.EXTRA_ATTEMPT, 0);
        final PendingResult result = goAsync();
        new Thread(() -> {
            try {
                String body = Net.fetch("data/latest.json");
                JSONObject edition = body == null ? null : new JSONObject(body);
                if (edition != null && Scheduler.todayJst().equals(edition.optString("date"))) {
                    Notifier.showEdition(app, edition);
                    Scheduler.scheduleNextMorning(app);
                } else if (attempt < MAX_RETRIES) {
                    Scheduler.scheduleRetry(app, attempt + 1);
                } else {
                    Scheduler.scheduleNextMorning(app);
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) Scheduler.scheduleRetry(app, attempt + 1);
                else Scheduler.scheduleNextMorning(app);
            } finally {
                result.finish();
            }
        }).start();
    }
}
