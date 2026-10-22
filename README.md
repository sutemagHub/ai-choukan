# 生成AI朝刊

毎朝7時に、その日の生成AIニュースを届けるAndroidアプリです。

- `android/` … アプリのコード（GitHub Actionsで自動ビルド）
- `data/` … ニュースのデータ。毎朝Claudeの定期タスクが更新します
  - `data/latest.json` … 最新の号
  - `data/editions/YYYY-MM-DD.json` … 各日の号
  - `data/index.json` … 号の一覧（新しい順）

## インストール

スマホで [最新リリース](https://github.com/sutemagHub/ai-choukan/releases/latest) を開き、`ai-choukan.apk` をダウンロードしてインストールします。
