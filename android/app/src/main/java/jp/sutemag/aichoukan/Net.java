package jp.sutemag.aichoukan;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Reads the edition files that the morning task publishes to GitHub. */
final class Net {
    static final String BASE = "https://raw.githubusercontent.com/sutemagHub/ai-choukan/main/";

    private Net() {}

    static boolean validPath(String path) {
        return path != null && path.matches("data/[a-z0-9\\-/]+\\.json");
    }

    /** Fetches a file from GitHub; null on any failure. */
    static String fetch(String path) {
        if (!validPath(path)) return null;
        HttpURLConnection con = null;
        try {
            // The minute stamp gets past GitHub's few-minute cache so a new edition shows up promptly.
            URL url = new URL(BASE + path + "?t=" + (System.currentTimeMillis() / 60000L));
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            con.setUseCaches(false);
            if (con.getResponseCode() != 200) return null;
            try (InputStream in = con.getInputStream()) {
                return readAll(in);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (con != null) con.disconnect();
        }
    }

    /** Fetches a file and keeps a copy, so the last edition can still be read offline. */
    static String fetchWithCache(Context context, String path) {
        if (!validPath(path)) return null;
        File file = new File(context.getFilesDir(), "cache_" + path.replace('/', '_'));
        String body = fetch(path);
        if (body != null) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
            return body;
        }
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                return readAll(in);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }
}
