# CustomItemCreator

Minecraft Spigotサーバー向けの直感的なGUIベースのカスタムアイテム作成プラグインです。サーバー管理者はこのプラグインを使って、プログラミング知識なしでカスタムアイテムを簡単に作成・管理できます。

# バージョン: 1.0.0
# Minecraft: 1.21+
# API: REST

## 機能

### 🎮 GUIベースのアイテム編集
- インベントリメニューを使った直感的な操作
- アイテム名、説明文（ロア）のカスタマイズ
- 6段階のレア度設定（Common～Mythic）
- カスタムモデルデータのサポート
- エンチャントの追加と管理
- 属性（Attribute）の詳細設定

### 📝 クラフトレシピシステム
- カスタムアイテム用のオリジナルレシピ作成
- 複数レシピの管理
- サーバー再起動後も保持

### 🌐 Web API連携
- RESTful APIによる外部連携
- カスタムアイテムのJSON形式提供
- CORS対応でウェブアプリケーションとの連携

### 💾 データ永続化
- YAML形式でのファイル保存
- MySQL対応（HikariCP使用）
- ストレージタイプの切り替えと移行機能


## 使い方

### 基本コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/itemmenu` | アイテム作成メニューを開く | customitemcreator.use |
| `/itemmenu list` | 保存済みアイテム一覧を表示 | customitemcreator.use |
| `/itemmenu get <ID>` | 指定IDのアイテムを取得 | customitemcreator.use |
| `/itemstorage info` | ストレージ情報を表示 | customitemcreator.admin |
| `/itemstorage reload` | 設定を再読み込み | customitemcreator.admin |
| `/itemstorage migrate <mysql/yaml>` | ストレージ間でデータを移行 | customitemcreator.admin |

### アイテム作成手順

1. `/itemmenu`コマンドでメインメニューを開く
2. 「アイテム名を編集」で名前を設定
3. 「説明文（ロア）を編集」でアイテムの説明を追加
4. 「レア度を設定」で希望のレア度を選択
5. 必要に応じて「エンチャントを追加」や「属性を設定」を行う
6. 「作成」ボタンでアイテムを保存
7. クラフトレシピを設定する場合は「レシピを設定」から作成

### Web API

プラグインはポート2002でAPIサーバーを起動します。以下のエンドポイントが利用可能：

- `GET http://localhost:2002/api/items` - 全アイテムのリストを取得
- `GET http://localhost:2002/api/items/<アイテムID>` - 特定アイテムの詳細情報を取得

例:
```json
[
  {
    "id": "abcd1234-5678-efgh-9012",
    "type": "DIAMOND_SWORD",
    "displayName": "伝説の剣",
    "customModelData": 1001
  },
  ...
]
```

## 設定

`config.yml`で以下の設定が可能です：

```yaml
# データストレージの設定
storage:
  # 保存方法: YAML または MYSQL
  type: YAML

  # MySQLの設定 (type: MYSQL の場合のみ使用)
  mysql:
    host: localhost
    port: 3306
    database: database
    username: name
    password: pass
    table-prefix: customitem_
    use-ssl: false

# その他の設定
settings:
  # デバッグモード (詳細なログを出力)
  debug: false
```

## 開発環境

### 必要環境
- Java 17以上
- Gradle

## Web UIとの連携

このプラグインはNext.jsで作成されたWeb管理インターフェイスと連携できます。
API経由でアイテムを表示・管理するための基盤を提供しています。

## 将来の計画

- [ ] 多言語サポート
- [ ] カスタムエフェクトのサポート
- [ ] モブドロップカスタマイズ
- [ ] アイテムセット機能


## クレジット

- 開発者: とどめん（TodoChan-dev）/ 櫻井 汐音（同一人物）

---
