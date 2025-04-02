package jp.tproject.customItemCreator.listener;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

/**
 * メニュー関連のイベントを処理するリスナークラス
 */
public class MenuListener implements Listener {

    private final CustomItemCreator plugin;

    /**
     * リスナーを初期化
     * @param plugin プラグインインスタンス
     */
    public MenuListener(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    /**
     * インベントリクリックイベントを処理
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // カスタムアイテム関連のメニューかどうかを確認
        if (!title.startsWith(ChatColor.DARK_PURPLE.toString()) &&
                !title.startsWith(ChatColor.RED.toString())) {
            return;
        }

        // メニュー内のクリックはキャンセル
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // 編集状態に応じてクリックを処理
        String editState = plugin.getItemManager().getPlayerEditState(player);

        if (title.equals(ChatColor.DARK_PURPLE + "アイテムクリエーター")) {
            MainMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("RARITY")) {
            RarityMenu.handleClick(player, event.getSlot());
        } else if (title.contains("数値を入力")) {
            NumericInputMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("ENCHANTMENT")) {
            EnchantmentMenu.handleEnchantmentClick(player, event.getSlot(), event.getCurrentItem());
        } else if (editState.equals("ENCHANTMENT_LEVEL")) {
            EnchantmentMenu.handleLevelClick(player, event.getSlot());
        } else if (editState.equals("ATTRIBUTE")) {
            AttributeMenu.handleAttributeClick(player, event.getSlot(), event.getCurrentItem());
        } else if (editState.equals("ATTRIBUTE_SLOT")) {
            AttributeMenu.handleSlotClick(player, event.getSlot());
        } else if (editState.equals("ATTRIBUTE_OPERATION")) {
            AttributeMenu.handleOperationClick(player, event.getSlot());
        } else if (editState.equals("CONFIRMATION")) {
            ConfirmationMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("SAVED_ITEMS")) {
            SavedItemsMenu.handleClick(player, event.getSlot(), event.getCurrentItem());
        } else if (editState.equals("ITEM_ACTION")) {
            SavedItemsMenu.handleActionClick(player, event.getSlot());
        } else if (editState.equals("DELETE_CONFIRM")) {
            SavedItemsMenu.handleDeleteConfirmClick(player, event.getSlot());
        } else if (editState.equals("LORE_EDIT")) {
            LoreMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("LORE_CLEAR_CONFIRM")) {
            LoreMenu.handleClearConfirmClick(player, event.getSlot());
        }
    }

    /**
     * 書籍編集イベントを処理（レガシーコード - 看板エディタへの移行後に削除可能）
     */
    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        String editState = plugin.getItemManager().getPlayerEditState(player);

        if (editState.equals("NAME") || editState.equals("LORE")) {
            BookMeta bookMeta = event.getNewBookMeta();

            if (bookMeta.hasPages()) {
                TextEditor.handleBookEdit(player, bookMeta);
            }
        }
    }

    /**
     * インベントリを閉じたときのイベントを処理
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.startsWith(ChatColor.DARK_PURPLE.toString()) ||
                title.startsWith(ChatColor.RED.toString())) {

            String editState = plugin.getItemManager().getPlayerEditState(player);

            // 看板編集や特定の確認メニューの場合は編集状態を保持
            if (editState.startsWith("SIGN_EDIT_") ||
                    editState.equals("CONFIRMATION") ||
                    editState.equals("DELETE_CONFIRM")) {
                return;
            }

            // 他のメニューを閉じた場合、編集状態を確認
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String currentTitle = player.getOpenInventory().getTitle();
                if (!currentTitle.startsWith(ChatColor.DARK_PURPLE.toString()) &&
                        !currentTitle.startsWith(ChatColor.RED.toString())) {
                    // 別のメニューが開かれていなければ編集状態をクリア
                    plugin.getItemManager().setPlayerEditState(player, "");
                }
            }, 1L);
        }
    }
}