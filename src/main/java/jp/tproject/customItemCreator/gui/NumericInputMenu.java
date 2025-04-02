package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import jp.tproject.customItemCreator.util.TranslationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.EquipmentSlot;

import java.text.DecimalFormat;
import java.util.Collections;

/**
 * 数値入力メニューを管理するクラス
 */
public class NumericInputMenu {

    /**
     * 数値入力メニューを開く
     * @param player プレイヤー
     * @param title メニュータイトル
     * @param initialValue 初期値
     * @param editType 編集タイプ
     */
    public static void open(Player player, String title, int initialValue, String editType) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + title);

        // 編集状態と現在値を設定
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, editType);
        CustomItemCreator.getInstance().getItemManager().setPlayerNumericValue(player, initialValue);

        // -10, -5, -1 ボタン
        inventory.setItem(0, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-10",
                "クリックして10減らす"));
        inventory.setItem(1, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-5",
                "クリックして5減らす"));
        inventory.setItem(2, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-1",
                "クリックして1減らす"));

        // 現在値表示
        updateValueDisplay(inventory, initialValue);

        // +1, +5, +10 ボタン
        inventory.setItem(6, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+1",
                "クリックして1増やす"));
        inventory.setItem(7, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+5",
                "クリックして5増やす"));
        inventory.setItem(8, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+10",
                "クリックして10増やす"));

        player.openInventory(inventory);
    }

    /**
     * 小数点数値入力メニューを開く
     * @param player プレイヤー
     * @param title メニュータイトル
     * @param initialValue 初期値
     * @param editType 編集タイプ
     */
    public static void openDecimal(Player player, String title, double initialValue, String editType) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + title);

        // 編集状態と現在値を設定
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, editType);
        CustomItemCreator.getInstance().getItemManager().setPlayerDecimalValue(player, initialValue);

        // デバッグメッセージ
        player.sendMessage(ChatColor.GRAY + "初期値を " + initialValue + " に設定しました");

        // -1.0, -0.5, -0.1 ボタン
        inventory.setItem(0, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-1.0",
                "クリックして1.0減らす"));
        inventory.setItem(1, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-0.5",
                "クリックして0.5減らす"));
        inventory.setItem(2, GuiUtil.createMenuItem(Material.RED_CONCRETE, ChatColor.RED + "-0.1",
                "クリックして0.1減らす"));

        // 現在値表示
        updateDecimalValueDisplay(inventory, initialValue);

        // +0.1, +0.5, +1.0 ボタン
        inventory.setItem(6, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+0.1",
                "クリックして0.1増やす"));
        inventory.setItem(7, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+0.5",
                "クリックして0.5増やす"));
        inventory.setItem(8, GuiUtil.createMenuItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+1.0",
                "クリックして1.0増やす"));

        player.openInventory(inventory);
    }

    /**
     * 数値表示を更新
     * @param inventory インベントリ
     * @param value 表示する値
     */
    private static void updateValueDisplay(Inventory inventory, int value) {
        inventory.setItem(4, GuiUtil.createMenuItem(Material.PAPER,
                ChatColor.YELLOW + "現在の値: " + value,
                Collections.singletonList(ChatColor.GRAY + "クリックして確定")));
    }

    /**
     * 小数点数値表示を更新
     * @param inventory インベントリ
     * @param value 表示する値
     */
    private static void updateDecimalValueDisplay(Inventory inventory, double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        inventory.setItem(4, GuiUtil.createMenuItem(Material.PAPER,
                ChatColor.YELLOW + "現在の値: " + df.format(value),
                Collections.singletonList(ChatColor.GRAY + "クリックして確定")));
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        String editType = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);

        if (editType.equals("ATTRIBUTE_VALUE")) {
            // 属性値は小数点で扱う
            double currentValue = CustomItemCreator.getInstance().getItemManager().getPlayerDecimalValue(player);
            double originalValue = currentValue;

            switch (slot) {
                case 0: // -1.0
                    currentValue = Math.max(0.0, currentValue - 1.0);
                    break;

                case 1: // -0.5
                    currentValue = Math.max(0.0, currentValue - 0.5);
                    break;

                case 2: // -0.1
                    currentValue = Math.max(0.0, currentValue - 0.1);
                    break;

                case 4: // 確定
                    applyValue(player, currentValue);
                    return;

                case 6: // +0.1
                    currentValue += 0.1;
                    break;

                case 7: // +0.5
                    currentValue += 0.5;
                    break;

                case 8: // +1.0
                    currentValue += 1.0;
                    break;
            }

            // 値を更新
            CustomItemCreator.getInstance().getItemManager().setPlayerDecimalValue(player, currentValue);
            updateDecimalValueDisplay(player.getOpenInventory().getTopInventory(), currentValue);

            // デバッグメッセージ
            if (originalValue != currentValue) {
                DecimalFormat df = new DecimalFormat("#.##");
                player.sendMessage(ChatColor.GRAY + "値を " + df.format(originalValue) + " から " + df.format(currentValue) + " に変更しました");
            }

        } else {
            // 通常の整数値で扱う
            int currentValue = CustomItemCreator.getInstance().getItemManager().getPlayerNumericValue(player);

            switch (slot) {
                case 0: // -10
                    currentValue = Math.max(0, currentValue - 10);
                    break;

                case 1: // -5
                    currentValue = Math.max(0, currentValue - 5);
                    break;

                case 2: // -1
                    currentValue = Math.max(0, currentValue - 1);
                    break;

                case 4: // 確定
                    applyValue(player, currentValue);
                    return;

                case 6: // +1
                    currentValue += 1;
                    break;

                case 7: // +5
                    currentValue += 5;
                    break;

                case 8: // +10
                    currentValue += 10;
                    break;
            }

            // 値を更新
            CustomItemCreator.getInstance().getItemManager().setPlayerNumericValue(player, currentValue);
            updateValueDisplay(player.getOpenInventory().getTopInventory(), currentValue);
        }
    }

    /**
     * 値を適用
     * @param player プレイヤー
     * @param value 適用する値
     */
    private static void applyValue(Player player, Object value) {
        String editType = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        if (editType.equals("CUSTOM_MODEL_DATA") && value instanceof Integer) {
            // カスタムモデルデータを設定
            customItem.setCustomModelData((Integer) value);
            player.sendMessage(ChatColor.GREEN + "カスタムモデルデータを " + value + " に設定しました。");

        } else if (editType.equals("ATTRIBUTE_VALUE")) {
            // 属性値を設定
            Attribute attribute = (Attribute) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "selectedAttribute");
            EquipmentSlot slot = (EquipmentSlot) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "selectedSlot");
            AttributeModifier.Operation operation = (AttributeModifier.Operation) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "selectedOperation");

            if (attribute != null) {
                double doubleValue = (value instanceof Double) ? (Double) value : (Integer) value;
                customItem.addAttribute(attribute, doubleValue, slot != null ? slot : EquipmentSlot.HAND, operation);

                String opName = TranslationUtil.getOperationJaName(operation);
                player.sendMessage(ChatColor.GREEN + TranslationUtil.getAttributeJaName(attribute) +
                        " の値を " + doubleValue + " に設定しました。（操作タイプ: " + opName + "）");
            }
        }

        // メインメニューに戻る
        MainMenu.open(player);
    }
}