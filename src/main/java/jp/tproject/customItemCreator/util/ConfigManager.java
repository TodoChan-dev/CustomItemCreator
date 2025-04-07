package jp.tproject.customItemCreator.util;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // レシピ保存用YAML
    private final File recipesFile;
    private FileConfiguration recipesConfig;

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

        // レシピ用のコンフィグファイルを初期化
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            recipesFile.getParentFile().mkdirs();
            try {
                recipesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "レシピ設定ファイルを作成できませんでした", e);
            }
        }

        this.recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);

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

        // レシピファイルを保存
        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "レシピ設定を保存できませんでした", e);
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

        // アイテムに関連するレシピも削除
        removeRecipesForItem(itemId);
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
     * レシピを保存
     * @param recipe 保存するレシピ
     */
    public void saveRecipe(CustomRecipe recipe) {
        // 保存用のセクションがなければ作成
        if (!recipesConfig.contains("recipes")) {
            recipesConfig.createSection("recipes");
        }

        // レシピIDのセクションを作成
        String recipeId = recipe.getRecipeId();

        // 結果アイテムIDを保存
        recipesConfig.set("recipes." + recipeId + ".resultItemId", recipe.getResultItemId());

        // 材料を保存
        ItemStack[] ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] != null && ingredients[i].getType() != org.bukkit.Material.AIR) {
                // Base64に変換して保存
                String encodedItem = ItemSerializer.toBase64(ingredients[i]);
                recipesConfig.set("recipes." + recipeId + ".ingredients." + i, encodedItem);
            } else {
                recipesConfig.set("recipes." + recipeId + ".ingredients." + i, null);
            }
        }

        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "レシピ設定を保存できませんでした", e);
        }
    }

    /**
     * レシピを取得
     * @param recipeId レシピID
     * @return 取得したレシピ、見つからなければnull
     */
    public CustomRecipe getRecipe(String recipeId) {
        if (!recipesConfig.contains("recipes." + recipeId)) {
            return null;
        }

        String resultItemId = recipesConfig.getString("recipes." + recipeId + ".resultItemId");

        if (resultItemId == null) {
            return null;
        }

        // 材料を読み込み
        ItemStack[] ingredients = new ItemStack[9];
        ConfigurationSection ingredientsSection = recipesConfig.getConfigurationSection("recipes." + recipeId + ".ingredients");

        if (ingredientsSection != null) {
            for (String key : ingredientsSection.getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    if (index >= 0 && index < 9) {
                        String encodedItem = ingredientsSection.getString(key);
                        if (encodedItem != null) {
                            ingredients[index] = ItemSerializer.fromBase64(encodedItem);
                        }
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("レシピの材料インデックスが無効です: " + key);
                }
            }
        }

        return new CustomRecipe(recipeId, resultItemId, ingredients);
    }

    /**
     * レシピを削除
     * @param recipeId 削除するレシピID
     */
    public void removeRecipe(String recipeId) {
        recipesConfig.set("recipes." + recipeId, null);

        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "レシピ設定を保存できませんでした", e);
        }
    }

    /**
     * 指定されたアイテムに関連する全てのレシピを削除
     * @param itemId アイテムID
     */
    public void removeRecipesForItem(String itemId) {
        if (!recipesConfig.contains("recipes")) {
            return;
        }

        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }

        List<String> toRemove = new ArrayList<>();

        for (String recipeId : recipesSection.getKeys(false)) {
            String resultId = recipesConfig.getString("recipes." + recipeId + ".resultItemId");
            if (itemId.equals(resultId)) {
                toRemove.add(recipeId);
            }
        }

        for (String recipeId : toRemove) {
            recipesConfig.set("recipes." + recipeId, null);
        }

        if (!toRemove.isEmpty()) {
            try {
                recipesConfig.save(recipesFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "レシピ設定を保存できませんでした", e);
            }
        }
    }

    /**
     * 指定されたアイテムに関連する全てのレシピを取得
     * @param itemId アイテムID
     * @return カスタムレシピのリスト
     */
    public List<CustomRecipe> getRecipesForItem(String itemId) {
        List<CustomRecipe> recipes = new ArrayList<>();

        if (!recipesConfig.contains("recipes")) {
            return recipes;
        }

        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return recipes;
        }

        for (String recipeId : recipesSection.getKeys(false)) {
            String resultId = recipesConfig.getString("recipes." + recipeId + ".resultItemId");
            if (itemId.equals(resultId)) {
                CustomRecipe recipe = getRecipe(recipeId);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
        }

        return recipes;
    }

    /**
     * 全てのレシピを取得
     * @return カスタムレシピのリスト
     */
    public List<CustomRecipe> getAllRecipes() {
        List<CustomRecipe> recipes = new ArrayList<>();

        if (!recipesConfig.contains("recipes")) {
            return recipes;
        }

        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return recipes;
        }

        for (String recipeId : recipesSection.getKeys(false)) {
            CustomRecipe recipe = getRecipe(recipeId);
            if (recipe != null) {
                recipes.add(recipe);
            }
        }

        return recipes;
    }

    /**
     * 設定をリロード
     */
    public void reloadConfig() {
        // プラグイン設定をリロード
        plugin.reloadConfig();

        // YAMLファイルをリロード
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);

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