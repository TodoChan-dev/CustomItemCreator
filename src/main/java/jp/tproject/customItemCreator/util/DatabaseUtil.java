package jp.tproject.customItemCreator.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jp.tproject.customItemCreator.CustomItemCreator;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * MySQL接続とアイテムデータの保存・取得を管理するユーティリティクラス
 * HikariCPを使用したコネクションプール実装
 */
public class DatabaseUtil {

    private final CustomItemCreator plugin;
    private HikariDataSource dataSource;
    private String tablePrefix;

    // データベース接続情報
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private boolean useSSL;
    private int poolSize;
    private long connectionTimeout;
    private long idleTimeout;
    private long maxLifetime;

    /**
     * データベースユーティリティを初期化
     * @param plugin プラグインインスタンス
     */
    public DatabaseUtil(CustomItemCreator plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 設定ファイルから接続情報を読み込み
     */
    private void loadConfig() {
        this.host = plugin.getConfig().getString("storage.mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("storage.mysql.port", 3306);
        this.database = plugin.getConfig().getString("storage.mysql.database", "minecraft");
        this.username = plugin.getConfig().getString("storage.mysql.username", "root");
        this.password = plugin.getConfig().getString("storage.mysql.password", "password");
        this.tablePrefix = plugin.getConfig().getString("storage.mysql.table-prefix", "customitem_");
        this.useSSL = plugin.getConfig().getBoolean("storage.mysql.use-ssl", false);

        // HikariCP 特有の設定
        this.poolSize = plugin.getConfig().getInt("storage.mysql.pool-size", 10);
        this.connectionTimeout = plugin.getConfig().getLong("storage.mysql.connection-timeout", 30000);
        this.idleTimeout = plugin.getConfig().getLong("storage.mysql.idle-timeout", 600000);
        this.maxLifetime = plugin.getConfig().getLong("storage.mysql.max-lifetime", 1800000);
    }

    /**
     * データベースに接続
     * @return 接続成功の場合true
     */
    public boolean connect() {
        try {
            // 既に接続済みの場合は何もしない
            if (dataSource != null && !dataSource.isClosed()) {
                return true;
            }

            // HikariCP設定
            HikariConfig config = new HikariConfig();

            // JDBC接続URL
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
            if (!useSSL) {
                jdbcUrl += "?useSSL=false";
            }

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            // コネクションプール設定
            config.setPoolName("CustomItemCreator-HikariPool");
            config.setMaximumPoolSize(poolSize);
            config.setConnectionTimeout(connectionTimeout);
            config.setIdleTimeout(idleTimeout);
            config.setMaxLifetime(maxLifetime);

            // 追加設定
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            // データソース初期化
            dataSource = new HikariDataSource(config);

            // テーブルの初期化
            initTables();

            plugin.getLogger().info("MySQL データベースに接続しました (HikariCP)");
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "MySQLデータベースへの接続に失敗しました", e);
            return false;
        }
    }

    /**
     * データベース接続を閉じる
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MySQL データベース接続を閉じました");
        }
    }

    /**
     * コネクションを取得
     * @return データベース接続
     * @throws SQLException 接続エラーの場合
     */
    private Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            connect();
        }
        return dataSource.getConnection();
    }

    /**
     * 必要なテーブルを初期化
     */
    private void initTables() {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            // アイテムデータを保存するテーブル
            String createItemsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "items (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "data MEDIUMTEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

            statement.execute(createItemsTable);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "テーブルの初期化に失敗しました", e);
        }
    }

    /**
     * アイテムをデータベースに保存
     * @param itemId アイテムID
     * @param item アイテム
     * @param name アイテム名
     * @return 保存成功の場合true
     */
    public boolean saveItem(String itemId, ItemStack item, String name) {
        if (!connect()) {
            return false;
        }

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "REPLACE INTO " + tablePrefix + "items (id, name, data) VALUES (?, ?, ?)")) {

            // Base64エンコード
            String encodedItem = ItemSerializer.toBase64(item);

            // パラメータ設定
            statement.setString(1, itemId);
            statement.setString(2, name);
            statement.setString(3, encodedItem);

            // クエリを実行
            statement.executeUpdate();

            return true;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテムの保存に失敗しました: " + itemId, e);
            return false;
        }
    }

    /**
     * アイテムをデータベースから取得
     * @param itemId アイテムID
     * @return アイテム、見つからなければnull
     */
    public ItemStack getItem(String itemId) {
        if (!connect()) {
            return null;
        }

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT data FROM " + tablePrefix + "items WHERE id = ?")) {

            statement.setString(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String encodedItem = resultSet.getString("data");

                    // Base64からItemStackに復元
                    return ItemSerializer.fromBase64(encodedItem);
                }
            }

            return null;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテムの取得に失敗しました: " + itemId, e);
            return null;
        }
    }

    /**
     * アイテムをデータベースから削除
     * @param itemId アイテムID
     * @return 削除成功の場合true
     */
    public boolean removeItem(String itemId) {
        if (!connect()) {
            return false;
        }

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "DELETE FROM " + tablePrefix + "items WHERE id = ?")) {

            statement.setString(1, itemId);

            // クエリを実行
            int result = statement.executeUpdate();

            return result > 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテムの削除に失敗しました: " + itemId, e);
            return false;
        }
    }

    /**
     * 全てのアイテムをデータベースから取得
     * @return アイテムIDとItemStackのマップ
     */
    public Map<String, ItemStack> getAllItems() {
        Map<String, ItemStack> items = new HashMap<>();

        if (!connect()) {
            return items;
        }

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT id, data FROM " + tablePrefix + "items ORDER BY updated_at DESC")) {

            while (resultSet.next()) {
                String itemId = resultSet.getString("id");
                String encodedItem = resultSet.getString("data");

                // Base64からItemStackに復元
                ItemStack item = ItemSerializer.fromBase64(encodedItem);

                if (item != null) {
                    items.put(itemId, item);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテム一覧の取得に失敗しました", e);
        }

        return items;
    }

    /**
     * アイテム数を取得
     * @return データベース内のアイテム数
     */
    public int getItemCount() {
        if (!connect()) {
            return 0;
        }

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) as count FROM " + tablePrefix + "items")) {

            if (resultSet.next()) {
                return resultSet.getInt("count");
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテム数の取得に失敗しました", e);
        }

        return 0;
    }

    /**
     * データベースの状態をチェック
     * @return データベースが正常な場合true
     */
    public boolean checkDatabase() {
        if (!connect()) {
            return false;
        }

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            // 簡単なクエリでデータベース接続を確認
            statement.execute("SELECT 1");
            return true;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "データベース接続チェックに失敗しました", e);
            return false;
        }
    }

    /**
     * テスト用のダミーアイテムを生成
     * @param count 生成数
     */
    public void generateTestItems(int count) {
        for (int i = 0; i < count; i++) {
            String itemId = UUID.randomUUID().toString();
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_SWORD);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("テストアイテム #" + i);
            item.setItemMeta(meta);

            saveItem(itemId, item, "テストアイテム #" + i);
        }
    }
}