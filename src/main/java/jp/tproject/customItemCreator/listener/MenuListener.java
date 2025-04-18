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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

/**
 * メニュー関連のイベントを処理するリスナークラス
 */
public class MenuListener implements Listener {

    private final CustomItemCreator plugin;

    // クラフトグリッドのスロット配列（クリック許可するスロット）
    private static final int[] CRAFT_SLOTS = {
            3, 4, 5,
            12, 13, 14,
            21, 22, 23
    };

    // クラフトグリッドの操作ボタン
    private static final int[] CRAFT_BUTTONS = {
            24, 25, 26 // クリア、保存、戻るボタン
    };

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

        // 編集状態を取得
        String editState = plugin.getItemManager().getPlayerEditState(player);

        // クラフトグリッド編集時の特別処理
        if (editState.equals("CRAFTING_GRID")) {
            int slot = event.getRawSlot();
            int inventorySize = event.getView().getTopInventory().getSize();

            // プレイヤーのインベントリ部分のクリックかどうか
            boolean isPlayerInventory = slot >= inventorySize;

            // クラフトグリッドのスロットか操作ボタンかを確認
            boolean isCraftSlot = isInCraftingSlot(slot);
            boolean isCraftButton = isInCraftingButton(slot);

            if (isPlayerInventory) {
                // プレイヤーのインベントリ部分のクリックは許可（キャンセルしない）
                // ただし、クリックの種類によって処理を分ける
                handleCraftingInventoryClick(event, player);
                return;
            } else if (isCraftSlot) {
                // クラフトグリッドのスロットは許可（キャンセルしない）
                // ただし、アイテムの置き換えを適切に処理
                handleCraftingGridClick(event, player);
                return;
            } else if (isCraftButton) {
                // 操作ボタンはキャンセルしてメソッド呼び出し
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    RecipeMenu.handleCraftingGridClick(player, slot, event.getCurrentItem(),
                            event.getCursor(), event.isShiftClick(), event.isRightClick());
                }
                return;
            } else {
                // それ以外の領域（背景など）はクリックキャンセル
                event.setCancelled(true);
                return;
            }
        }

        // 他のメニューの場合はイベントをキャンセル
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // 編集状態に応じてクリックを処理
        if (title.equals(ChatColor.DARK_PURPLE + "アイテムクリエーター")) {
            MainMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("RARITY")) {
            RarityMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("ATTRIBUTE_VALUE") || title.contains("値を入力") || editState.equals("CUSTOM_MODEL_DATA") || editState.equals("ENCHANTMENT_LEVEL_NEW")) {
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
        } else if (editState.equals("RECIPE_MENU")) {
            RecipeMenu.handleClick(player, event.getSlot());
        } else if (editState.equals("RECIPES_LIST")) {
            RecipeMenu.handleRecipesListClick(player, event.getSlot());
        } else if (editState.equals("RECIPE_ACTION")) {
            RecipeMenu.handleRecipeActionClick(player, event.getSlot());
        } else if (editState.equals("RECIPE_DELETE_CONFIRM")) {
            RecipeMenu.handleDeleteConfirmClick(player, event.getSlot());
        }
    }

    /**
     * プレイヤーインベントリからクラフトグリッドへのアイテム移動を処理
     * @param event クリックイベント
     * @param player プレイヤー
     */
    private void handleCraftingInventoryClick(InventoryClickEvent event, Player player) {
        // プレイヤーのインベントリからクラフトグリッドへの移動を処理
        switch (event.getClick()) {
            case LEFT:
            case RIGHT:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                // これらのクリックタイプは通常のアイテム移動を許可
                // ここではイベントをキャンセルせず、デフォルトの動作を許可
                break;
            default:
                // その他のクリックタイプはデフォルトの動作
                break;
        }
    }

    /**
     * クラフトグリッド内のアイテム配置を処理
     * @param event クリックイベント
     * @param player プレイヤー
     */
    private void handleCraftingGridClick(InventoryClickEvent event, Player player) {
        // クラフトグリッド内のスロットでのクリック
        switch (event.getClick()) {
            case LEFT:
            case RIGHT:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                // これらのクリックタイプは通常のアイテム移動を許可
                // ここではイベントをキャンセルせず、デフォルトの動作を許可
                break;
            default:
                // その他のクリックタイプはデフォルトの動作
                break;
        }
    }

    /**
     * ドラッグイベントを処理
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // カスタムアイテム関連のメニューかどうかを確認
        if (!title.startsWith(ChatColor.DARK_PURPLE.toString()) &&
                !title.startsWith(ChatColor.RED.toString())) {
            return;
        }

        // 編集状態を取得
        String editState = plugin.getItemManager().getPlayerEditState(player);

        // クラフトグリッド編集時は特別処理
        if (editState.equals("CRAFTING_GRID")) {
            boolean isCraftingSlotsOnly = true;

            // ドラッグされたすべてのスロットをチェック
            for (int slot : event.getRawSlots()) {
                // インベントリのサイズより小さいスロット（上部インベントリ）
                if (slot < event.getView().getTopInventory().getSize()) {
                    // クラフトスロットでなければキャンセル
                    if (!isInCraftingSlot(slot)) {
                        isCraftingSlotsOnly = false;
                        break;
                    }
                }
            }

            // クラフトスロット以外へのドラッグはキャンセル
            if (!isCraftingSlotsOnly) {
                event.setCancelled(true);
            }
            return;
        }

        // クラフトグリッド以外のメニューではドラッグをキャンセル
        event.setCancelled(true);
    }

    /**
     * スロットがクラフトグリッド内かどうかを確認
     * @param slot チェックするスロット
     * @return クラフトグリッド内の場合true
     */
    private boolean isInCraftingSlot(int slot) {
        return Arrays.stream(CRAFT_SLOTS).anyMatch(craftSlot -> craftSlot == slot);
    }

    /**
     * スロットがクラフトグリッドの操作ボタンかどうかを確認
     * @param slot チェックするスロット
     * @return 操作ボタンの場合true
     */
    private boolean isInCraftingButton(int slot) {
        return Arrays.stream(CRAFT_BUTTONS).anyMatch(button -> button == slot);
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

            // クラフトグリッドを閉じるときに編集中のレシピを保存するか確認
            if (editState.equals("CRAFTING_GRID")) {
                // キャンセル時はデータをクリア
                player.sendMessage(ChatColor.YELLOW + "レシピ編集をキャンセルしました。保存するには「保存」ボタンを押してください。");
            }

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