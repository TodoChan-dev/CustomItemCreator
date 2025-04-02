package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * エンチャント選択メニューを管理するクラス
 */
public class EnchantmentMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "エンチャントを選択";
    private static final int MENU_SIZE = 54;

    private static final String LEVEL_MENU_TITLE = ChatColor.DARK_PURPLE + "エンチャントレベルを選択";
    private static final int LEVEL_MENU_SIZE = 9;

    /**
     * エンチャント選択メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 各エンチャントのボタンを作成
        int slot = 0;
        for (Enchantment enchantment : Enchantment.values()) {
            if (slot >= 53) break; // 安全対策

            String name = enchantment.getKey().getKey();
            name = formatEnchantmentName(name);

            inventory.setItem(slot++, GuiUtil.createMenuItem(Material.ENCHANTED_BOOK,
                    ChatColor.AQUA + name,
                    Collections.singletonList(ChatColor.GRAY + "クリックして選択")));
        }

        // 戻るボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "メインメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ENCHANTMENT");
    }

    /**
     * エンチャントレベル選択メニューを開く
     * @param player メニューを開くプレイヤー
     * @param enchantment 選択されたエンチャント
     */
    public static void openLevelMenu(Player player, Enchantment enchantment) {
        Inventory inventory = Bukkit.createInventory(null, LEVEL_MENU_SIZE, LEVEL_MENU_TITLE);

        // 選択したエンチャントを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selectedEnchantment", enchantment);

        // レベル選択ボタン（1～5）
        for (int i = 1; i <= 5; i++) {
            inventory.setItem(i - 1, GuiUtil.createMenuItem(Material.EXPERIENCE_BOTTLE,
                    ChatColor.GREEN + "レベル " + i,
                    "クリックして選択"));
        }

        // レベル10ボタン
        inventory.setItem(5, GuiUtil.createMenuItem(Material.EXPERIENCE_BOTTLE,
                ChatColor.GOLD + "レベル 10",
                "クリックして選択"));

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "エンチャント選択に戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ENCHANTMENT_LEVEL");
    }

    /**
     * エンチャント選択のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     * @param clickedItem クリックされたアイテム
     */
    public static void handleEnchantmentClick(Player player, int slot, ItemStack clickedItem) {
        if (slot == 53) { // 戻るボタン
            MainMenu.open(player);
            return;
        }

        // エンチャント選択
        if (clickedItem.getType() == Material.ENCHANTED_BOOK) {
            String enchantName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())
                    .toLowerCase().replace(" ", "_");

            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.getKey().getKey().equalsIgnoreCase(enchantName)) {
                    openLevelMenu(player, enchantment);
                    return;
                }
            }
        }
    }

    /**
     * エンチャントレベル選択のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleLevelClick(Player player, int slot) {
        if (slot == 8) { // 戻るボタン
            open(player);
            return;
        }

        // レベル選択
        int level = 1;
        if (slot >= 0 && slot <= 4) {
            level = slot + 1;
        } else if (slot == 5) {
            level = 10;
        }

        // エンチャントを追加
        Enchantment enchantment = (Enchantment) CustomItemCreator.getInstance()
                .getItemManager().getPlayerData(player, "selectedEnchantment");

        if (enchantment != null) {
            CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
            customItem.addEnchant(enchantment, level);

            player.sendMessage(ChatColor.GREEN + "エンチャント " +
                    formatEnchantmentName(enchantment.getKey().getKey()) +
                    " レベル " + level + " を追加しました。");
        }

        MainMenu.open(player);
    }

    /**
     * エンチャント名を整形（snake_case → Title Case）
     * @param name エンチャント名（スネークケース）
     * @return 整形された名前
     */
    private static String formatEnchantmentName(String name) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = name.split("_");

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }
}