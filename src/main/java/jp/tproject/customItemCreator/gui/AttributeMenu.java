package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.util.GuiUtil;
import jp.tproject.customItemCreator.util.TranslationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 属性設定メニューを管理するクラス
 */
public class AttributeMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "属性を選択";
    private static final int MENU_SIZE = 54;

    private static final String SLOT_MENU_TITLE = ChatColor.DARK_PURPLE + "装備スロットを選択";
    private static final int SLOT_MENU_SIZE = 9;

    private static final String OPERATION_MENU_TITLE = ChatColor.DARK_PURPLE + "操作タイプを選択";
    private static final int OPERATION_MENU_SIZE = 9;

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

            String jaName = TranslationUtil.getAttributeJaName(attribute);
            String desc = TranslationUtil.getAttributeDesc(attribute);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "クリックして値を設定");
            if (!desc.isEmpty()) {
                lore.add(ChatColor.ITALIC + desc);
            }

            inventory.setItem(slot++, GuiUtil.createMenuItem(Material.POTION,
                    ChatColor.LIGHT_PURPLE + jaName,
                    lore));
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
     * 操作タイプ選択メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void openOperationMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, OPERATION_MENU_SIZE, OPERATION_MENU_TITLE);

        // 操作タイプのボタンを作成
        inventory.setItem(0, GuiUtil.createMenuItem(Material.IRON_INGOT,
                ChatColor.YELLOW + TranslationUtil.getOperationJaName(AttributeModifier.Operation.ADD_NUMBER),
                Arrays.asList(
                        "数値をそのまま追加します",
                        "例: 10 → ベース値+10"
                )));

        inventory.setItem(1, GuiUtil.createMenuItem(Material.GOLD_INGOT,
                ChatColor.YELLOW + TranslationUtil.getOperationJaName(AttributeModifier.Operation.ADD_SCALAR),
                Arrays.asList(
                        "元の値に対する割合を他の修飾子適用後に加算します",
                        "例: 0.1 → (ベース値+他の修飾子)*1.1"
                )));

        inventory.setItem(2, GuiUtil.createMenuItem(Material.DIAMOND,
                ChatColor.YELLOW + TranslationUtil.getOperationJaName(AttributeModifier.Operation.MULTIPLY_SCALAR_1),
                Arrays.asList(
                        "元の値に対する割合を直接乗算します",
                        "例: 0.1 → ベース値*1.1"
                )));

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "スロット選択に戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "ATTRIBUTE_OPERATION");
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
            String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            // 日本語名から属性を検索
            for (Attribute attribute : Attribute.values()) {
                String jaName = TranslationUtil.getAttributeJaName(attribute);
                if (displayName.equals(jaName)) {
                    openSlotMenu(player, attribute);
                    return;
                }
            }

            // 見つからない場合、元のコードで試す
            try {
                String attributeName = displayName.toUpperCase().replace(" ", "_");
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

        // 操作タイプ選択メニューを開く
        openOperationMenu(player);
    }

    /**
     * 操作タイプ選択のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleOperationClick(Player player, int slot) {
        if (slot == 8) { // 戻るボタン
            EquipmentSlot equipmentSlot = (EquipmentSlot) CustomItemCreator.getInstance()
                    .getItemManager().getPlayerData(player, "selectedSlot");

            if (equipmentSlot != null) {
                openSlotMenu(player, (Attribute) CustomItemCreator.getInstance()
                        .getItemManager().getPlayerData(player, "selectedAttribute"));
            } else {
                open(player);
            }
            return;
        }

        // 操作タイプを保存
        if (slot >= 0 && slot <= 2) {
            AttributeModifier.Operation operation = TranslationUtil.getOperationByIndex(slot);
            CustomItemCreator.getInstance().getItemManager().setPlayerData(player, "selectedOperation", operation);

            // 値入力メニューを開く
            Attribute attribute = (Attribute) CustomItemCreator.getInstance()
                    .getItemManager().getPlayerData(player, "selectedAttribute");

            if (attribute != null) {
                // 属性値は小数点で扱うため、openDecimalメソッドを使用
                NumericInputMenu.openDecimal(player, TranslationUtil.getAttributeJaName(attribute) + " の値を入力", 1.0, "ATTRIBUTE_VALUE");
            }
        }
    }
}