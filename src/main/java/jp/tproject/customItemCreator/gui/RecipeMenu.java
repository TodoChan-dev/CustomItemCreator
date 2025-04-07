package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.model.CustomRecipe;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * レシピ編集メニューを管理するクラス
 */
public class RecipeMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "レシピ編集";
    private static final int MENU_SIZE = 27;

    private static final String CRAFTING_GRID_TITLE = ChatColor.DARK_PURPLE + "クラフトグリッド";
    private static final int CRAFTING_GRID_SIZE = 27;

    private static final String RECIPES_LIST_TITLE = ChatColor.DARK_PURPLE + "登録レシピ一覧";
    private static final int RECIPES_LIST_SIZE = 54;

    /**
     * レシピメニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 保存済みレシピ一覧ボタン
        inventory.setItem(11, GuiUtil.createMenuItem(Material.BOOK,
                ChatColor.YELLOW + "保存済みレシピ一覧",
                "このアイテムの登録済みレシピを表示します"));

        // 新規レシピ作成ボタン
        inventory.setItem(13, GuiUtil.createMenuItem(Material.CRAFTING_TABLE,
                ChatColor.GREEN + "新規レシピを作成",
                "新しいクラフトレシピを作成します"));

        // プレビュー
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        inventory.setItem(15, customItem.getItemStack());

        // 戻るボタン
        inventory.setItem(22, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "メインメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "RECIPE_MENU");
    }

    /**
     * レシピ一覧メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void openRecipesList(Player player) {
        Inventory inventory = Bukkit.createInventory(null, RECIPES_LIST_SIZE, RECIPES_LIST_TITLE);

        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        String itemId = customItem.getItemId();

        // 登録済みレシピを取得
        List<CustomRecipe> recipes = CustomItemCreator.getInstance().getConfigManager().getRecipesForItem(itemId);

        if (recipes.isEmpty()) {
            // レシピが登録されていない場合
            inventory.setItem(22, GuiUtil.createMenuItem(Material.BARRIER,
                    ChatColor.RED + "レシピがありません",
                    "新しいレシピを作成してください"));
        } else {
            // 登録済みレシピを表示
            int slot = 0;
            for (CustomRecipe recipe : recipes) {
                if (slot >= 45) break; // 安全対策

                // レシピIDを一時保存
                CustomItemCreator.getInstance().getItemManager().setPlayerData(
                        player, "recipe_id_slot_" + slot, recipe.getRecipeId());

                // レシピ代表アイテム（最初に見つかる非空気アイテム）
                ItemStack representativeItem = null;
                for (ItemStack item : recipe.getIngredients()) {
                    if (item != null && item.getType() != Material.AIR) {
                        representativeItem = item.clone();
                        break;
                    }
                }

                if (representativeItem == null) {
                    representativeItem = new ItemStack(Material.CRAFTING_TABLE);
                }

                // アイテム名とロアを設定
                ItemStack button = representativeItem.clone();
                ItemMeta meta = button.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.YELLOW + "レシピ #" + (slot + 1));

                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "クリックして詳細を表示");
                    lore.add(ChatColor.GRAY + "レシピID: " + recipe.getRecipeId());
                    meta.setLore(lore);

                    button.setItemMeta(meta);
                }

                inventory.setItem(slot++, button);
            }
        }

        // 新規レシピ作成ボタン
        inventory.setItem(49, GuiUtil.createMenuItem(Material.CRAFTING_TABLE,
                ChatColor.GREEN + "新規レシピを作成",
                "新しいクラフトレシピを作成します"));

        // 戻るボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "レシピメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "RECIPES_LIST");
    }

    /**
     * クラフトグリッドメニューを開く
     * @param player メニューを開くプレイヤー
     * @param recipeId 編集するレシピID（新規の場合はnull）
     */
    public static void openCraftingGrid(Player player, String recipeId) {
        Inventory inventory = Bukkit.createInventory(null, CRAFTING_GRID_SIZE, CRAFTING_GRID_TITLE);

        // クラフトグリッドの枠を作成
        for (int i = 0; i < CRAFTING_GRID_SIZE; i++) {
            inventory.setItem(i, GuiUtil.createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", ""));
        }

        // クラフトグリッドのスロット
        int[] craftSlots = {
                3, 4, 5,
                12, 13, 14,
                21, 22, 23
        };

        // クラフトグリッドの空白を設定
        for (int slot : craftSlots) {
            inventory.setItem(slot, null);
        }

        // 矢印
        inventory.setItem(15, GuiUtil.createMenuItem(Material.ARROW, "→", ""));

        // 結果スロット
        inventory.setItem(16, CustomItemCreator.getInstance().getItemManager().getPlayerItem(player).getItemStack());

        // 保存ボタン
        inventory.setItem(25, GuiUtil.createMenuItem(Material.EMERALD_BLOCK,
                ChatColor.GREEN + "レシピを保存",
                "現在のレシピを保存します"));

        // クリアボタン
        inventory.setItem(24, GuiUtil.createMenuItem(Material.BARRIER,
                ChatColor.RED + "クリア",
                "クラフトグリッドをクリアします"));

        // 戻るボタン
        inventory.setItem(26, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "レシピメニューに戻ります"));

        // 既存のレシピを読み込む
        if (recipeId != null) {
            CustomRecipe recipe = CustomItemCreator.getInstance().getConfigManager().getRecipe(recipeId);
            if (recipe != null) {
                // 材料を設定
                ItemStack[] ingredients = recipe.getIngredients();
                for (int i = 0; i < craftSlots.length && i < ingredients.length; i++) {
                    if (ingredients[i] != null && ingredients[i].getType() != Material.AIR) {
                        inventory.setItem(craftSlots[i], ingredients[i].clone());
                    }
                }

                // 編集中のレシピIDを保存
                CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "editing_recipe_id", recipeId);
            }
        } else {
            // 新規レシピの場合、新しいUUIDを生成
            String newRecipeId = UUID.randomUUID().toString();
            CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "editing_recipe_id", newRecipeId);
        }

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "CRAFTING_GRID");
    }

    /**
     * レシピメニューのクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        switch (slot) {
            case 11: // 保存済みレシピ一覧
                openRecipesList(player);
                break;

            case 13: // 新規レシピを作成
                openCraftingGrid(player, null);
                break;

            case 22: // 戻る
                MainMenu.open(player);
                break;
        }
    }

    /**
     * レシピ一覧のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleRecipesListClick(Player player, int slot) {
        if (slot == 53) { // 戻る
            open(player);
            return;
        }

        if (slot == 49) { // 新規レシピを作成
            openCraftingGrid(player, null);
            return;
        }

        // レシピ選択
        String recipeId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "recipe_id_slot_" + slot);

        if (recipeId != null) {
            openRecipeActionMenu(player, recipeId);
        }
    }

    /**
     * レシピ操作メニューを開く
     * @param player プレイヤー
     * @param recipeId レシピID
     */
    private static void openRecipeActionMenu(Player player, String recipeId) {
        Inventory inventory = Bukkit.createInventory(null, 9,
                ChatColor.DARK_PURPLE + "レシピ操作");

        // レシピの代表アイテムを取得
        CustomRecipe recipe = CustomItemCreator.getInstance().getConfigManager().getRecipe(recipeId);
        ItemStack previewItem = new ItemStack(Material.CRAFTING_TABLE);

        if (recipe != null) {
            for (ItemStack item : recipe.getIngredients()) {
                if (item != null && item.getType() != Material.AIR) {
                    previewItem = item.clone();
                    break;
                }
            }
        }

        // レシピプレビュー
        inventory.setItem(4, previewItem);

        // 編集ボタン
        inventory.setItem(1, GuiUtil.createMenuItem(Material.ANVIL,
                ChatColor.YELLOW + "編集",
                "このレシピを編集します"));

        // 削除ボタン
        inventory.setItem(7, GuiUtil.createMenuItem(Material.LAVA_BUCKET,
                ChatColor.RED + "削除",
                "このレシピを削除します"));

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.BLUE + "戻る",
                "レシピ一覧に戻ります"));

        // レシピIDを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selected_recipe_id", recipeId);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "RECIPE_ACTION");

        player.openInventory(inventory);
    }

    /**
     * レシピ操作メニューのクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleRecipeActionClick(Player player, int slot) {
        String recipeId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "selected_recipe_id");

        if (recipeId == null) {
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 1: // 編集
                openCraftingGrid(player, recipeId);
                break;

            case 7: // 削除
                openDeleteConfirmation(player, recipeId);
                break;

            case 8: // 戻る
                openRecipesList(player);
                break;
        }
    }

    /**
     * レシピ削除確認メニューを開く
     * @param player プレイヤー
     * @param recipeId 削除するレシピID
     */
    private static void openDeleteConfirmation(Player player, String recipeId) {
        Inventory inventory = Bukkit.createInventory(null, 9,
                ChatColor.RED + "レシピ削除確認");

        // 確認ボタン
        inventory.setItem(2, GuiUtil.createMenuItem(Material.RED_CONCRETE,
                ChatColor.RED + "削除する",
                "このレシピを削除します"));

        // キャンセルボタン
        inventory.setItem(6, GuiUtil.createMenuItem(Material.GREEN_CONCRETE,
                ChatColor.GREEN + "キャンセル",
                "削除をキャンセルします"));

        // レシピIDを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "delete_recipe_id", recipeId);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "RECIPE_DELETE_CONFIRM");

        player.openInventory(inventory);
    }

    public static void handleDeleteConfirmClick(Player player, int slot) {
        String recipeId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "delete_recipe_id");

        if (recipeId == null) {
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 2: // 削除確定
                // レシピをサーバーから削除
                CustomItemCreator.getInstance().getRecipeManager().unregisterRecipe(recipeId);
                // 設定からレシピを削除
                CustomItemCreator.getInstance().getConfigManager().removeRecipe(recipeId);
                player.sendMessage(ChatColor.GREEN + "レシピを削除しました。");
                openRecipesList(player);
                break;

            case 6: // キャンセル
                openRecipesList(player);
                break;
        }
    }

    /**
     * クラフトグリッドのクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     * @param clickedItem クリックされたアイテム
     * @param cursor カーソルのアイテム
     * @param isShiftClick シフトクリックかどうか
     * @param isRightClick 右クリックかどうか
     */
    public static void handleCraftingGridClick(Player player, int slot, ItemStack clickedItem,
                                               ItemStack cursor, boolean isShiftClick, boolean isRightClick) {
        // クラフトグリッドのスロット配列
        int[] craftSlots = {
                3, 4, 5,
                12, 13, 14,
                21, 22, 23
        };

        Inventory inventory = player.getOpenInventory().getTopInventory();

        // クラフトグリッド以外のスロットのみ処理（クラフトグリッド内は素材配置のためイベントをキャンセルしない）
        if (slot == 24) { // クリアボタン
            for (int craftSlot : craftSlots) {
                inventory.setItem(craftSlot, null);
            }
            player.sendMessage(ChatColor.YELLOW + "クラフトグリッドをクリアしました。");
        } else if (slot == 25) { // 保存ボタン
            saveRecipe(player, inventory, craftSlots);
        } else if (slot == 26) { // 戻るボタン
            open(player);
        }
    }

    /**
     * レシピを保存
     * @param player プレイヤー
     * @param inventory インベントリ
     * @param craftSlots クラフトスロット配列
     */
    private static void saveRecipe(Player player, Inventory inventory, int[] craftSlots) {
        // クラフトグリッドからアイテムを取得
        ItemStack[] ingredients = new ItemStack[9];
        boolean hasIngredient = false;

        for (int i = 0; i < craftSlots.length; i++) {
            ItemStack item = inventory.getItem(craftSlots[i]);
            ingredients[i] = item != null ? item.clone() : null;

            if (item != null && item.getType() != Material.AIR) {
                hasIngredient = true;
            }
        }

        if (!hasIngredient) {
            player.sendMessage(ChatColor.RED + "レシピには少なくとも1つのアイテムが必要です。");
            return;
        }

        // 編集中のレシピIDを取得
        String recipeId = (String) CustomItemCreator.getInstance().getItemManager()
                .getPlayerData(player, "editing_recipe_id");

        if (recipeId == null) {
            recipeId = UUID.randomUUID().toString();
        }

        // カスタムアイテムを取得
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        // レシピを保存
        CustomRecipe recipe = new CustomRecipe(recipeId, customItem.getItemId(), ingredients);
        CustomItemCreator.getInstance().getConfigManager().saveRecipe(recipe);

        player.sendMessage(ChatColor.GREEN + "レシピを保存しました。");

        // レシピ一覧に戻る
        openRecipesList(player);
    }
}