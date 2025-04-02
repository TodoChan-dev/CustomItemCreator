package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 属性設定メニューを管理するクラス
 */
public class AttributeMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "属性を選択";
    private static final int MENU_SIZE = 54;

    private static final String SLOT_MENU_TITLE = ChatColor.DARK_PURPLE + "装備スロットを選択";
    private static final int SLOT_MENU_SIZE = 9;

    /**
     * 属性選択メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 各属性のボタンを作成
        int slot = 0;
        for (Attribute attribute : Attribute.values()) {
            if (slot >= 53) break; // 安全対策

            String name = formatAttributeName(attribute.name());

            inventory.setItem(slot++, GuiUtil.createMenuItem(Material.POTION,
                    ChatColor.LIGHT_PURPLE + name,
                    Collections.singletonList(ChatColor.GRAY + "クリックして値を設定")));
        }

        // 戻るボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "メインメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ATTRIBUTE");
    }

    /**
     * 装備スロット選択メニューを開く
     * @param player メニューを開くプレイヤー
     * @param attribute 選択された属性
     */
    public static void openSlotMenu(Player player, Attribute attribute) {
        Inventory inventory = Bukkit.createInventory(null, SLOT_MENU_SIZE, SLOT_MENU_TITLE);

        // 選択した属性を保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selectedAttribute", attribute);

        // 各装備スロットのボタンを作成
        inventory.setItem(0, GuiUtil.createMenuItem(Material.WOODEN_SWORD,
                ChatColor.GOLD + "メインハンド",
                "クリックして選択"));

        inventory.setItem(1, GuiUtil.createMenuItem(Material.SHIELD,
                ChatColor.GOLD + "オフハンド",
                "クリックして選択"));

        inventory.setItem(2, GuiUtil.createMenuItem(Material.IRON_HELMET,
                ChatColor.GOLD + "頭",
                "クリックして選択"));

        inventory.setItem(3, GuiUtil.createMenuItem(Material.IRON_CHESTPLATE,
                ChatColor.GOLD + "胴体",
                "クリックして選択"));

        inventory.setItem(4, GuiUtil.createMenuItem(Material.IRON_LEGGINGS,
                ChatColor.GOLD + "脚",
                "クリックして選択"));

        inventory.setItem(5, GuiUtil.createMenuItem(Material.IRON_BOOTS,
                ChatColor.GOLD + "足",
                "クリックして選択"));

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "属性選択に戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ATTRIBUTE_SLOT");
    }

    /**
     * 属性選択のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     * @param clickedItem クリックされたアイテム
     */
    public static void handleAttributeClick(Player player, int slot, ItemStack clickedItem) {
        if (slot == 53) { // 戻るボタン
            MainMenu.open(player);
            return;
        }

        // 属性選択
        if (clickedItem.getType() == Material.POTION) {
            String attributeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())
                    .toUpperCase().replace(" ", "_");

            try {
                Attribute attribute = Attribute.valueOf(attributeName);
                openSlotMenu(player, attribute);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "属性の設定中にエラーが発生しました。");
            }
        }
    }

    /**
     * スロット選択のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleSlotClick(Player player, int slot) {
        if (slot == 8) { // 戻るボタン
            open(player);
            return;
        }

        // スロット選択
        EquipmentSlot equipmentSlot;
        switch (slot) {
            case 0:
                equipmentSlot = EquipmentSlot.HAND;
                break;
            case 1:
                equipmentSlot = EquipmentSlot.OFF_HAND;
                break;
            case 2:
                equipmentSlot = EquipmentSlot.HEAD;
                break;
            case 3:
                equipmentSlot = EquipmentSlot.CHEST;
                break;
            case 4:
                equipmentSlot = EquipmentSlot.LEGS;
                break;
            case 5:
                equipmentSlot = EquipmentSlot.FEET;
                break;
            default:
                return;
        }

        // 選択したスロットを保存
        CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selectedSlot", equipmentSlot);

        // 値入力メニューを開く
        Attribute attribute = (Attribute) CustomItemCreator.getInstance()
                .getItemManager().getPlayerData(player, "selectedAttribute");

        if (attribute != null) {
            NumericInputMenu.open(player, attribute.name() + " の値を入力", 1, "ATTRIBUTE_VALUE");
        }
    }

    /**
     * 属性名を整形（SNAKE_CASE → Title Case）
     * @param name 属性名（スネークケース）
     * @return 整形された名前
     */
    private static String formatAttributeName(String name) {
        return Arrays.stream(name.toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
