package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * 確認メニューを管理するクラス
 */
public class ConfirmationMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "アイテム作成確認";
    private static final int MENU_SIZE = 27;

    /**
     * 確認メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        // アイテムプレビュー
        inventory.setItem(13, customItem.getItemStack());

        // 作成ボタン
        inventory.setItem(11, GuiUtil.createMenuItem(Material.EMERALD_BLOCK,
                ChatColor.GREEN + "作成",
                "アイテムを作成してDBに保存します"));

        // 修正ボタン
        inventory.setItem(15, GuiUtil.createMenuItem(Material.ANVIL,
                ChatColor.YELLOW + "修正",
                "メインメニューに戻って修正します"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "CONFIRMATION");
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        switch (slot) {
            case 11: // 作成
                saveItem(player, customItem);
                player.closeInventory();
                break;

            case 15: // 修正
                MainMenu.open(player);
                break;
        }
    }

    /**
     * アイテムを保存
     * @param player プレイヤー
     * @param customItem 保存するアイテム
     */
    private static void saveItem(Player player, CustomItem customItem) {
        ItemStack item = customItem.getItemStack();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // アイテムIDを設定
            String itemId = customItem.getItemId();
            meta.getPersistentDataContainer().set(
                    CustomItemCreator.getInstance().getCustomItemKey(),
                    PersistentDataType.STRING,
                    itemId);

            item.setItemMeta(meta);

            // アイテム名を取得（IDがない場合はアイテムタイプを使用）
            String name = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();

            // アイテムを保存
            CustomItemCreator.getInstance().getConfigManager().saveItem(itemId, item, name);

            player.sendMessage(ChatColor.GREEN + "アイテムが正常に保存されました。ID: " + itemId);

            // プレイヤーのインベントリにアイテムを追加
            player.getInventory().addItem(item.clone());
        }
    }
}
