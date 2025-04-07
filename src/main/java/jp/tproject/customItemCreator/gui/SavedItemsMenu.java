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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 保存アイテム一覧メニューを管理するクラス
 */
public class SavedItemsMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "保存済みアイテム";
    private static final int MENU_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45; // 5行 x 9列 = 45スロット

    /**
     * 保存アイテム一覧メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        open(player, 0); // デフォルトは0ページ目から
    }

    /**
     * 保存アイテム一覧メニューを指定ページで開く
     * @param player メニューを開くプレイヤー
     * @param page ページ番号（0から始まる）
     */
    public static void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE + " - ページ " + (page + 1));

        // 保存済みアイテムを取得
        Map<String, ItemStack> savedItems = CustomItemCreator.getInstance().getConfigManager().getAllItems();

        if (savedItems.isEmpty()) {
            // 保存アイテムがない場合
            inventory.setItem(22, GuiUtil.createMenuItem(Material.BARRIER,
                    ChatColor.RED + "アイテムがありません",
                    "アイテムを作成してください"));
        } else {
            // アイテムの総数とページ数を計算
            int totalItems = savedItems.size();
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

            // ページが範囲外なら調整
            if (page < 0) page = 0;
            if (page >= totalPages) page = totalPages - 1;

            // 現在のページのアイテムのみ表示
            List<Map.Entry<String, ItemStack>> itemList = new ArrayList<>(savedItems.entrySet());
            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

            // アイテムを表示
            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, ItemStack> entry = itemList.get(i);
                int slot = i - startIndex;

                // アイテムIDを一時データとして保存
                CustomItemCreator.getInstance().getItemManager().setPlayerData(
                        player, "item_id_slot_" + slot, entry.getKey());

                // アイテムの表示（アイテムIDもロアに追加）
                ItemStack displayItem = entry.getValue().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.GRAY + "ID: " + entry.getKey());
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }

                inventory.setItem(slot, displayItem);
            }

            // ページ情報をプレイヤーに保存
            CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "current_page", page);
            CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "total_pages", totalPages);

            // ページ情報表示
            inventory.setItem(49, GuiUtil.createMenuItem(Material.BOOK,
                    ChatColor.YELLOW + "ページ " + (page + 1) + "/" + totalPages,
                    Arrays.asList(
                            ChatColor.GRAY + "合計 " + totalItems + " 個のアイテム",
                            ChatColor.GRAY + "各ページ " + ITEMS_PER_PAGE + " 個表示"
                    )));

            // 前のページボタン（最初のページでなければ表示）
            if (page > 0) {
                inventory.setItem(48, GuiUtil.createMenuItem(Material.ARROW,
                        ChatColor.AQUA + "前のページ",
                        "前のページに移動します"));
            }

            // 次のページボタン（最後のページでなければ表示）
            if (page < totalPages - 1) {
                inventory.setItem(50, GuiUtil.createMenuItem(Material.ARROW,
                        ChatColor.AQUA + "次のページ",
                        "次のページに移動します"));
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

        if (slot == 48) { // 前のページ
            Integer currentPage = (Integer) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "current_page");
            if (currentPage != null && currentPage > 0) {
                open(player, currentPage - 1);
            }
            return;
        }

        if (slot == 50) { // 次のページ
            Integer currentPage = (Integer) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "current_page");
            Integer totalPages = (Integer) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "total_pages");
            if (currentPage != null && totalPages != null && currentPage < totalPages - 1) {
                open(player, currentPage + 1);
            }
            return;
        }

        if (slot == 49) { // ページ情報（クリックしても何もしない）
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
                // 現在のページを取得して戻る
                Integer currentPage = (Integer) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "current_page");
                open(player, currentPage != null ? currentPage : 0);
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

        // 現在のページを取得
        Integer currentPage = (Integer) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "current_page");
        int page = currentPage != null ? currentPage : 0;

        switch (slot) {
            case 2: // 削除確定
                CustomItemCreator.getInstance().getConfigManager().removeItem(itemId);
                player.sendMessage(ChatColor.GREEN + "アイテムを削除しました。");

                // 関連するレシピも削除
                CustomItemCreator.getInstance().getRecipeManager().unregisterRecipe(itemId);

                // 前のページを開く（削除後にアイテム数が減る可能性があるため）
                open(player, page);
                break;

            case 6: // キャンセル
                open(player, page);
                break;
        }
    }
}