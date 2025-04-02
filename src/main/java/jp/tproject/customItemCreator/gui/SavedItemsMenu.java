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

import java.util.Map;

/**
 * 保存アイテム一覧メニューを管理するクラス
 */
public class SavedItemsMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "保存済みアイテム";
    private static final int MENU_SIZE = 54;

    /**
     * 保存アイテム一覧メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 保存済みアイテムを取得
        Map<String, ItemStack> savedItems = CustomItemCreator.getInstance().getConfigManager().getAllItems();

        if (savedItems.isEmpty()) {
            // 保存アイテムがない場合
            inventory.setItem(22, GuiUtil.createMenuItem(Material.BARRIER,
                    ChatColor.RED + "アイテムがありません",
                    "アイテムを作成してください"));
        } else {
            // 保存アイテムを表示
            int slot = 0;
            for (Map.Entry<String, ItemStack> entry : savedItems.entrySet()) {
                if (slot >= 53) break; // 安全対策

                // アイテムIDを一時データとして保存
                CustomItemCreator.getInstance().getItemManager().setPlayerData(
                        player, "item_id_slot_" + slot, entry.getKey());

                inventory.setItem(slot++, entry.getValue());
            }
        }

        // 新規作成ボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.EMERALD,
                ChatColor.GREEN + "新規作成",
                "新しいアイテムを作成します"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "SAVED_ITEMS");
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     * @param clickedItem クリックされたアイテム
     */
    public static void handleClick(Player player, int slot, ItemStack clickedItem) {
        if (slot == 53) { // 新規作成
            CustomItemCreator.getInstance().getItemManager().setPlayerItem(
                    player, new CustomItem(Material.DIAMOND_SWORD));
            MainMenu.open(player);
            return;
        }

        // 保存アイテムを選択
        if (clickedItem != null && clickedItem.getType() != Material.BARRIER) {
            // アイテムIDを取得
            String itemId = (String) CustomItemCreator.getInstance().getItemManager()
                    .getPlayerData(player, "item_id_slot_" + slot);

            if (itemId != null) {
                // アイテム編集のサブメニューを表示
                openItemActionMenu(player, clickedItem, itemId);
            }
        }
    }

    /**
     * アイテム操作サブメニューを開く
     * @param player プレイヤー
     * @param item 選択されたアイテム
     * @param itemId アイテムのID
     */
    private static void openItemActionMenu(Player player, ItemStack item, String itemId) {
        Inventory inventory = Bukkit.createInventory(null, 9,
                ChatColor.DARK_PURPLE + "アイテム操作: " +
                        (item.getItemMeta().hasDisplayName() ?
                                item.getItemMeta().getDisplayName() :
                                item.getType().name()));

        // アイテムプレビュー
        inventory.setItem(4, item);

        // 編集ボタン
        inventory.setItem(1, GuiUtil.createMenuItem(Material.ANVIL,
                ChatColor.YELLOW + "編集",
                "このアイテムを編集します"));

        // 取得ボタン
        inventory.setItem(3, GuiUtil.createMenuItem(Material.CHEST,
                ChatColor.GREEN + "取得",
                "このアイテムを取得します"));

        // 削除ボタン
        inventory.setItem(5, GuiUtil.createMenuItem(Material.LAVA_BUCKET,
                ChatColor.RED + "削除",
                "このアイテムを削除します"));

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.BLUE + "戻る",
                "アイテム一覧に戻ります"));

        player.openInventory(inventory);

        // アイテムIDを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selected_item_id", itemId);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ITEM_ACTION");
    }

    /**
     * アイテム操作メニューのクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleActionClick(Player player, int slot) {
        String itemId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "selected_item_id");

        if (itemId == null) {
            player.closeInventory();
            return;
        }

        ItemStack item = CustomItemCreator.getInstance().getConfigManager().getItem(itemId);

        if (item == null) {
            player.sendMessage(ChatColor.RED + "そのアイテムは存在しません。");
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 1: // 編集
                CustomItemCreator.getInstance().getItemManager().setPlayerItem(player, item);
                MainMenu.open(player);
                break;

            case 3: // 取得
                player.getInventory().addItem(item.clone());
                player.sendMessage(ChatColor.GREEN + "アイテムを取得しました。");
                player.closeInventory();
                break;

            case 5: // 削除
                player.closeInventory();
                openDeleteConfirmation(player, itemId, item);
                break;

            case 8: // 戻る
                open(player);
                break;
        }
    }

    /**
     * 削除確認メニューを開く
     * @param player プレイヤー
     * @param itemId 削除するアイテムのID
     * @param item 削除するアイテム
     */
    private static void openDeleteConfirmation(Player player, String itemId, ItemStack item) {
        Inventory inventory = Bukkit.createInventory(null, 9,
                ChatColor.RED + "削除確認: " + (item.getItemMeta().hasDisplayName() ?
                        item.getItemMeta().getDisplayName() :
                        item.getType().name()));

        // アイテムプレビュー
        inventory.setItem(4, item);

        // 確認ボタン
        inventory.setItem(2, GuiUtil.createMenuItem(Material.RED_CONCRETE,
                ChatColor.RED + "削除する",
                "このアイテムを削除します"));

        // キャンセルボタン
        inventory.setItem(6, GuiUtil.createMenuItem(Material.GREEN_CONCRETE,
                ChatColor.GREEN + "キャンセル",
                "削除をキャンセルします"));

        player.openInventory(inventory);

        // アイテムIDを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "delete_item_id", itemId);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "DELETE_CONFIRM");
    }

    /**
     * 削除確認メニューのクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleDeleteConfirmClick(Player player, int slot) {
        String itemId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "delete_item_id");

        if (itemId == null) {
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 2: // 削除確定
                CustomItemCreator.getInstance().getConfigManager().removeItem(itemId);
                player.sendMessage(ChatColor.GREEN + "アイテムを削除しました。");
                player.closeInventory();
                open(player);
                break;

            case 6: // キャンセル
                open(player);
                break;
        }
    }
}