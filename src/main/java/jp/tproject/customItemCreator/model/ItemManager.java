package jp.tproject.customItemCreator.model;


import jp.tproject.customItemCreator.CustomItemCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーごとのカスタムアイテム編集状態を管理するクラス
 */
public class ItemManager {

    private final CustomItemCreator plugin;

    // プレイヤーごとの編集中アイテム
    private final Map<UUID, CustomItem> playerItems = new HashMap<>();

    // プレイヤーごとの編集状態
    private final Map<UUID, String> playerEditState = new HashMap<>();

    // プレイヤーごとの数値編集値
    private final Map<UUID, Integer> playerNumericValue = new HashMap<>();

    // プレイヤーごとのその他の一時データ
    private final Map<UUID, Object> playerData = new HashMap<>();

    /**
     * アイテムマネージャーを初期化
     * @param plugin プラグインインスタンス
     */
    public ItemManager(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤーの編集アイテムを取得
     * @param player プレイヤー
     * @return 編集中のカスタムアイテム
     */
    public CustomItem getPlayerItem(Player player) {
        return playerItems.get(player.getUniqueId());
    }

    /**
     * プレイヤーの編集アイテムを設定
     * @param player プレイヤー
     * @param customItem 設定するカスタムアイテム
     */
    public void setPlayerItem(Player player, CustomItem customItem) {
        playerItems.put(player.getUniqueId(), customItem);
    }

    /**
     * プレイヤーの編集アイテムを設定（ItemStackから）
     * @param player プレイヤー
     * @param itemStack 設定するアイテム
     */
    public void setPlayerItem(Player player, ItemStack itemStack) {
        playerItems.put(player.getUniqueId(), new CustomItem(itemStack));
    }

    /**
     * プレイヤーの編集状態を取得
     * @param player プレイヤー
     * @return 編集状態の文字列
     */
    public String getPlayerEditState(Player player) {
        return playerEditState.getOrDefault(player.getUniqueId(), "");
    }

    /**
     * プレイヤーの編集状態を設定
     * @param player プレイヤー
     * @param state 設定する状態
     */
    public void setPlayerEditState(Player player, String state) {
        playerEditState.put(player.getUniqueId(), state);
    }

    /**
     * プレイヤーが編集中かどうか
     * @param player プレイヤー
     * @return 編集中の場合true
     */
    public boolean isEditing(Player player) {
        return playerEditState.containsKey(player.getUniqueId());
    }

    /**
     * プレイヤーの数値編集値を取得
     * @param player プレイヤー
     * @return 数値
     */
    public int getPlayerNumericValue(Player player) {
        return playerNumericValue.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * プレイヤーの数値編集値を設定
     * @param player プレイヤー
     * @param value 設定する数値
     */
    public void setPlayerNumericValue(Player player, int value) {
        playerNumericValue.put(player.getUniqueId(), value);
    }

    /**
     * プレイヤーのカスタムデータを取得
     * @param player プレイヤー
     * @param key データキー
     * @param <T> データ型
     * @return データ
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerData(Player player, String key) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> playerDataMap = (Map<String, Object>) playerData.getOrDefault(playerId, new HashMap<String, Object>());
        return (T) playerDataMap.get(key);
    }

    /**
     * プレイヤーのカスタムデータを設定
     * @param player プレイヤー
     * @param key データキー
     * @param value データ値
     */
    @SuppressWarnings("unchecked")
    public void setPlayerData(Player player, String key, Object value) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> playerDataMap = (Map<String, Object>) playerData.getOrDefault(playerId, new HashMap<String, Object>());
        playerDataMap.put(key, value);
        playerData.put(playerId, playerDataMap);
    }

    /**
     * プレイヤーデータを削除
     * @param player 削除するプレイヤー
     */
    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        playerItems.remove(playerId);
        playerEditState.remove(playerId);
        playerNumericValue.remove(playerId);
        playerData.remove(playerId);
    }
}