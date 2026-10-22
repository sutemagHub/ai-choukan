package jp.sutemag.aichoukan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

final class Notifier {
    private Notifier() {}

    static void ensureChannels(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        for (Feed f : Feed.ALL) {
            if (nm.getNotificationChannel(f.channelId) != null) continue;
            NotificationChannel ch = new NotificationChannel(f.channelId, f.channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription(f.channelDescription);
            nm.createNotificationChannel(ch);
        }
    }

    static void showEdition(Context context, Feed feed, JSONObject edition) {
        ensureChannels(context);
        JSONArray items = edition.optJSONArray("items");
        int count = items == null ? 0 : items.length();

        String first = "";
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < Math.min(3, count); i++) {
            JSONObject it = items.optJSONObject(i);
            if (it == null) continue;
            String title = it.optString("title");
            if (first.isEmpty()) first = title;
            if (lines.length() > 0) lines.append('\n');
            lines.append("・").append(title);
        }

        Intent open = new Intent(context, MainActivity.class)
                .putExtra(Scheduler.EXTRA_FEED, feed.key)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, feed.requestCode, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new Notification.Builder(context, feed.channelId)
                .setSmallIcon(R.drawable.ic_stat_sun)
                .setColor(0xFFB4512C)
                .setContentTitle(feed.titlePrefix + "（" + count + "本）")
                .setContentText(first)
                .setStyle(new Notification.BigTextStyle().bigText(lines.toString()))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(feed.notificationId, n);
    }
}
