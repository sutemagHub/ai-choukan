package jp.sutemag.aichoukan;

/** The two papers in the app: when each arrives and where its newest edition lives. */
final class Feed {
    final String key;
    final int hour;
    final int minute;
    final String latestPath;
    final String channelId;
    final String channelName;
    final String channelDescription;
    final String titlePrefix;
    final int requestCode;
    final int notificationId;

    private Feed(String key, int hour, int minute, String latestPath, String channelId, String channelName,
                 String channelDescription, String titlePrefix, int requestCode, int notificationId) {
        this.key = key;
        this.hour = hour;
        this.minute = minute;
        this.latestPath = latestPath;
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        this.titlePrefix = titlePrefix;
        this.requestCode = requestCode;
        this.notificationId = notificationId;
    }

    static final Feed AI = new Feed("ai", 7, 0, "data/latest.json",
            "morning", "毎朝のニュース", "毎朝7時に生成AI朝刊が届いたことをお知らせします",
            "けさの生成AI朝刊", 100, 1);

    static final Feed MUSK = new Feed("musk", 11, 30, "data/musk/latest.json",
            "musk", "マスク・スペースXのニュース", "毎日11時30分にマスク・スペースXのニュースが届いたことをお知らせします",
            "マスク・スペースXのニュース", 101, 2);

    static final Feed[] ALL = {AI, MUSK};

    static Feed byKey(String key) {
        for (Feed f : ALL) if (f.key.equals(key)) return f;
        return AI;
    }
}
