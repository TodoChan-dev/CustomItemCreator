package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import jp.tproject.customItemCreator.util.TranslationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

            String enchantKey = enchantment.getKey().getKey();
            String jaName = TranslationUtil.getEnchantmentJaName(enchantKey);
            String desc = TranslationUtil.getEnchantmentDesc(enchantKey);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "クリックして選択");
            if (!desc.isEmpty()) {
                lore.add(ChatColor.ITALIC + desc);
            }

            inventory.setItem(slot++, GuiUtil.createMenuItem(Material.ENCHANTED_BOOK,
                    ChatColor.AQUA + jaName,
                    lore));
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

        // 現在のエンチャントレベルを取得
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        int currentLevel = customItem.getItemStack().getEnchantmentLevel(enchantment);

        // 現在のレベル表示
        if (currentLevel > 0) {
            inventory.setItem(0, GuiUtil.createMenuItem(Material.BOOK,
                    ChatColor.YELLOW + "現在のレベル: " + currentLevel,
                    "このエンチャントは既に適用されています"));
        }

        // レベル選択ボタン（1～5）
        for (int i = 1; i <= 5; i++) {
            inventory.setItem(i, GuiUtil.createMenuItem(Material.EXPERIENCE_BOTTLE,
                    ChatColor.GREEN + "レベル " + i,
                    "クリックして選択"));
        }

        // レベル10ボタン
        inventory.setItem(6, GuiUtil.createMenuItem(Material.EXPERIENCE_BOTTLE,
                ChatColor.GOLD + "レベル 10",
                "クリックして選択"));

        // レベル-1ボタン（レベルを1減らす）
        if (currentLevel > 0) {
            inventory.setItem(7, GuiUtil.createMenuItem(Material.RED_DYE,
                    ChatColor.RED + "レベル -1",
                    "現在のレベルから1減らします"));
        }

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
            String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            // 日本語名から元のキーを逆引き
            for (Enchantment enchantment : Enchantment.values()) {
                String jaName = TranslationUtil.getEnchantmentJaName(enchantment.getKey().getKey());
                if (displayName.equals(jaName)) {
                    openLevelMenu(player, enchantment);
                    return;
                }
            }

            // 見つからない場合は下記の方法で試す
            for (Enchantment enchantment : Enchantment.values()) {
                if (displayName.equalsIgnoreCase(formatEnchantmentName(enchantment.getKey().getKey()))) {
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

        // エンチャントを取得
        Enchantment enchantment = (Enchantment) CustomItemCreator.getInstance()
                .getItemManager().getPlayerData(player, "selectedEnchantment");

        if (enchantment != null) {
            CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

            if (slot == 7) {  // レベル-1ボタン
                // 現在のレベルを取得
                int currentLevel = customItem.getItemStack().getEnchantmentLevel(enchantment);

                if (currentLevel > 1) {
                    // レベルを1減らす
                    customItem.addEnchant(enchantment, currentLevel - 1);
                    player.sendMessage(ChatColor.GREEN + "エンチャント " +
                            TranslationUtil.getEnchantmentJaName(enchantment.getKey().getKey()) +
                            " のレベルを " + (currentLevel - 1) + " に設定しました。");
                } else {
                    // エンチャントを削除
                    ItemMeta meta = customItem.getItemStack().getItemMeta();
                    if (meta != null) {
                        meta.removeEnchant(enchantment);
                        customItem.getItemStack().setItemMeta(meta);
                    }
                    player.sendMessage(ChatColor.GREEN + "エンチャント " +
                            TranslationUtil.getEnchantmentJaName(enchantment.getKey().getKey()) +
                            " を削除しました。");
                }
            } else if (slot >= 1 && slot <= 6) {
                // レベル選択
                int level = 1;
                if (slot >= 1 && slot <= 5) {
                    level = slot;
                } else if (slot == 6) {
                    level = 10;
                }

                // エンチャントを追加
                customItem.addEnchant(enchantment, level);

                String enchantName = TranslationUtil.getEnchantmentJaName(enchantment.getKey().getKey());
                player.sendMessage(ChatColor.GREEN + "エンチャント " +
                        enchantName +
                        " レベル " + level + " を追加しました。");
            }

            MainMenu.open(player);
        }
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