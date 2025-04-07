package jp.tproject.customItemCreator;

import jp.tproject.customItemCreator.command.ItemMenuCommand;
import jp.tproject.customItemCreator.command.StorageCommand;
import jp.tproject.customItemCreator.gui.SignEditor;
import jp.tproject.customItemCreator.listener.ChatInputListener;
import jp.tproject.customItemCreator.listener.MenuListener;
import jp.tproject.customItemCreator.model.ItemManager;
import jp.tproject.customItemCreator.util.ConfigManager;
import jp.tproject.customItemCreator.util.RecipeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CustomItemCreator メインクラス
 * アイテム作成・編集プラグインのエントリーポイント
 */
public class CustomItemCreator extends JavaPlugin {

    private static CustomItemCreator instance;
    private NamespacedKey customItemKey;
    private ConfigManager configManager;
    private ItemManager itemManager;
    private SignEditor signEditor;
    private RecipeManager recipeManager;
    private ChatInputListener chatInputListener;

    @Override
    public void onEnable() {
        // シングルトンインスタンス
        instance = this;

        // デフォルト設定ファイルをコピー
        saveDefaultConfig();

        // 初期化
        this.customItemKey = new NamespacedKey(this, "custom_item_id");
        this.configManager = new ConfigManager(this);
        this.itemManager = new ItemManager(this);
        this.signEditor = new SignEditor(this);
        this.recipeManager = new RecipeManager(this);
        this.chatInputListener = new ChatInputListener(this);

        // コマンド登録
        getCommand("itemmenu").setExecutor(new ItemMenuCommand());
        getCommand("itemstorage").setExecutor(new StorageCommand(this));

        // イベントリスナー登録
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputListener, this);

        // カスタムレシピを登録
        this.recipeManager.registerAllRecipes();

        getLogger().info("CustomItemCreator プラグインが有効になりました。");
        getLogger().info("ストレージタイプ: " + configManager.getStorageType());
    }

    @Override
    public void onDisable() {
        // レシピを削除
        if (recipeManager != null) {
            recipeManager.clearAllRecipes();
        }

        // 設定を保存
        configManager.saveConfig();

        getLogger().info("CustomItemCreator プラグインが無効になりました。");
    }

    /**
     * プラグインのインスタンスを取得
     * @return プラグインインスタンス
     */
    public static CustomItemCreator getInstance() {
        return instance;
    }

    /**
     * カスタムアイテムIDのNamespacedKeyを取得
     * @return カスタムアイテムキー
     */
    public NamespacedKey getCustomItemKey() {
        return customItemKey;
    }

    /**
     * コンフィグマネージャーを取得
     * @return コンフィグマネージャー
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * アイテムマネージャーを取得
     * @return アイテムマネージャー
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * 看板エディタを取得
     * @return 看板エディタ
     */
    public SignEditor getSignEditor() {
        return signEditor;
    }

    /**
     * レシピマネージャーを取得
     * @return レシピマネージャー
     */
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * チャット入力リスナーを取得
     * @return チャット入力リスナー
     */
    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }

    /**
     * 設定をリロード
     */
    public void reloadPlugin() {
        reloadConfig();
        configManager.reloadConfig();

        // レシピを再登録
        recipeManager.registerAllRecipes();

        getLogger().info("設定をリロードしました。ストレージタイプ: " + configManager.getStorageType());
    }
}