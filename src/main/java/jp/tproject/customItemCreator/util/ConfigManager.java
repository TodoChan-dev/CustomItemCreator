package jp.tproject.customItemCreator.util;

import jp.tproject.customItemCreator.CustomItemCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * プラグインの設定ファイルとアイテムデータを管理するクラス
 */
public class ConfigManager {

    private final CustomItemCreator plugin;

    // アイテム保存用YAML
    private final File itemsFile;
    private FileConfiguration itemsConfig;

    // データベース接続
    private DatabaseUtil databaseUtil;

    // ストレージタイプ
    public enum StorageType {
        YAML,
        MYSQL
    }

    private StorageType storageType;

    /**
     * コンフィグマネージャーを初期化
     * @param plugin プラグインインスタンス
     */
    public ConfigManager(CustomItemCreator plugin) {
        this.plugin = plugin;

        // デフォルト設定ファイルを保存
        plugin.saveDefaultConfig();

        // ストレージタイプを読み込み
        String storageTypeStr = plugin.getConfig().getString("storage.type", "YAML");
        try {
            this.storageType = StorageType.valueOf(storageTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無効なストレージタイプ: " + storageTypeStr + "、YAMLに設定します");
            this.storageType = StorageType.YAML;
        }

        // アイテム用のコンフィグファイルを初期化
        this.itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists() && storageType == StorageType.YAML) {
            itemsFile.getParentFile().mkdirs();
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "アイテム設定ファイルを作成できませんでした", e);
            }
        }

        this.itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        // MySQLを使用する場合はデータベース接続を初期化
        if (storageType == StorageType.MYSQL) {
            this.databaseUtil = new DatabaseUtil(plugin);

            // 接続テスト
            if (!databaseUtil.connect()) {
                plugin.getLogger().warning("MySQLへの接続に失敗しました。YAMLストレージにフォールバックします。");
                this.storageType = StorageType.YAML;
            }
        }

        plugin.getLogger().info("ストレージタイプ: " + this.storageType);
    }

    /**
     * 設定ファイルを保存
     */
    public void saveConfig() {
        // YAMLファイルを保存
        if (storageType == StorageType.YAML) {
            try {
                itemsConfig.save(itemsFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "アイテム設定を保存できませんでした", e);
            }
        }

        // MySQLの場合は接続を閉じる
        if (storageType == StorageType.MYSQL && databaseUtil != null) {
            databaseUtil.disconnect();
        }
    }

    /**
     * アイテムを保存
     * @param itemId アイテムID
     * @param item アイテム
     * @param name アイテム名
     */
    public void saveItem(String itemId, ItemStack item, String name) {
        if (storageType == StorageType.MYSQL) {
            // MySQLに保存
            databaseUtil.saveItem(itemId, item, name);
        } else {
            // YAMLに保存
            // 保存用のセクションがなければ作成
            if (!itemsConfig.contains("items")) {
                itemsConfig.createSection("items");
            }

            // Base64に変換して保存
            String encodedItem = ItemSerializer.toBase64(item);

            itemsConfig.set("items." + itemId + ".data", encodedItem);
            itemsConfig.set("items." + itemId + ".name", name);

            saveConfig();
        }
    }

    /**
     * アイテムをIDで取得
     * @param itemId アイテムID
     * @return 取得したアイテム、見つからなければnull
     */
    public ItemStack getItem(String itemId) {
        if (storageType == StorageType.MYSQL) {
            // MySQLから取得
            return databaseUtil.getItem(itemId);
        } else {
            // YAMLから取得
            if (!itemsConfig.contains("items." + itemId + ".data")) {
                return null;
            }

            String encodedItem = itemsConfig.getString("items." + itemId + ".data");
            return ItemSerializer.fromBase64(encodedItem);
        }
    }

    /**
     * アイテムを削除
     * @param itemId 削除するアイテムID
     */
    public void removeItem(String itemId) {
        if (storageType == StorageType.MYSQL) {
            // MySQLから削除
            databaseUtil.removeItem(itemId);
        } else {
            // YAMLから削除
            itemsConfig.set("items." + itemId, null);
            saveConfig();
        }
    }

    /**
     * 全てのアイテムを取得
     * @return アイテムIDとItemStackのマップ
     */
    public Map<String, ItemStack> getAllItems() {
        if (storageType == StorageType.MYSQL) {
            // MySQLから全アイテム取得
            return databaseUtil.getAllItems();
        } else {
            // YAMLから全アイテム取得
            Map<String, ItemStack> items = new HashMap<>();

            if (!itemsConfig.contains("items")) {
                return items;
            }

            Set<String> itemIds = itemsConfig.getConfigurationSection("items").getKeys(false);
            for (String itemId : itemIds) {
                String encodedItem = itemsConfig.getString("items." + itemId + ".data");
                ItemStack item = ItemSerializer.fromBase64(encodedItem);

                if (item != null) {
                    items.put(itemId, item);
                }
            }

            return items;
        }
    }

    /**
     * 設定をリロード
     */
    public void reloadConfig() {
        // プラグイン設定をリロード
        plugin.reloadConfig();

        // YAMLファイルをリロード
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        // ストレージタイプを更新
        String storageTypeStr = plugin.getConfig().getString("storage.type", "YAML");
        try {
            StorageType newStorageType = StorageType.valueOf(storageTypeStr.toUpperCase());

            // ストレージタイプが変わった場合
            if (newStorageType != storageType) {
                plugin.getLogger().info("ストレージタイプが変更されました: " + storageType + " -> " + newStorageType);

                // MySQLに変更された場合は接続を初期化
                if (newStorageType == StorageType.MYSQL) {
                    if (databaseUtil == null) {
                        databaseUtil = new DatabaseUtil(plugin);
                    } else {
                        databaseUtil.disconnect(); // 既存の接続を閉じる
                    }

                    // 接続テスト
                    if (!databaseUtil.connect()) {
                        plugin.getLogger().warning("MySQLへの接続に失敗しました。YAMLストレージを維持します。");
                        return; // ストレージタイプを変更しない
                    }
                }

                this.storageType = newStorageType;
            }

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無効なストレージタイプ: " + storageTypeStr + "、現在の設定を維持します");
        }
    }

    /**
     * 現在のストレージタイプを取得
     * @return ストレージタイプ
     */
    public StorageType getStorageType() {
        return storageType;
    }

    /**
     * アイテムをYAMLからMySQLに移行
     * @return 移行したアイテム数
     */
    public int migrateFromYamlToMysql() {
        if (storageType != StorageType.MYSQL || databaseUtil == null) {
            plugin.getLogger().warning("MySQLが有効ではないため、移行はできません");
            return 0;
        }

        int count = 0;

        // YAMLから全アイテムを読み込み
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(itemsFile);

        if (!yamlConfig.contains("items")) {
            return 0;
        }

        Set<String> itemIds = yamlConfig.getConfigurationSection("items").getKeys(false);
        for (String itemId : itemIds) {
            String encodedItem = yamlConfig.getString("items." + itemId + ".data");
            String name = yamlConfig.getString("items." + itemId + ".name", "Unknown Item");

            ItemStack item = ItemSerializer.fromBase64(encodedItem);

            if (item != null) {
                // MySQLに保存
                if (databaseUtil.saveItem(itemId, item, name)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * アイテムをMySQLからYAMLに移行
     * @return 移行したアイテム数
     */
    public int migrateFromMysqlToYaml() {
        if (databaseUtil == null) {
            plugin.getLogger().warning("MySQLが初期化されていないため、移行はできません");
            return 0;
        }

        int count = 0;

        // MySQLから全アイテムを取得
        Map<String, ItemStack> items = databaseUtil.getAllItems();

        // YAMLセクションを初期化
        if (!itemsConfig.contains("items")) {
            itemsConfig.createSection("items");
        }

        // 各アイテムをYAMLに保存
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            String itemId = entry.getKey();
            ItemStack item = entry.getValue();

            String name = item.getItemMeta().hasDisplayName() ?
                    item.getItemMeta().getDisplayName() : item.getType().name();

            // Base64に変換して保存
            String encodedItem = ItemSerializer.toBase64(item);

            itemsConfig.set("items." + itemId + ".data", encodedItem);
            itemsConfig.set("items." + itemId + ".name", name);

            count++;
        }

        // 設定を保存
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "アイテム設定の保存に失敗しました", e);
        }

        return count;
    }
}