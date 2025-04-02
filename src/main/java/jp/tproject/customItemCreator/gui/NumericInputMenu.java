package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.EquipmentSlot;

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
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        String editType = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);
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

    /**
     * 値を適用
     * @param player プレイヤー
     * @param value 適用する値
     */
    private static void applyValue(Player player, int value) {
        String editType = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        if (editType.equals("CUSTOM_MODEL_DATA")) {
            // カスタムモデルデータを設定
            customItem.setCustomModelData(value);
            player.sendMessage(ChatColor.GREEN + "カスタムモデルデータを " + value + " に設定しました。");

        } else if (editType.equals("ATTRIBUTE_VALUE")) {
            // 属性値を設定
            Attribute attribute = (Attribute) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "selectedAttribute");
            EquipmentSlot slot = (EquipmentSlot) CustomItemCreator.getInstance().getItemManager().getPlayerData(player, "selectedSlot");

            if (attribute != null) {
                customItem.addAttribute(attribute, value, slot != null ? slot : EquipmentSlot.HAND);
                player.sendMessage(ChatColor.GREEN + attribute.name() + " の値を " + value + " に設定しました。");
            }

        } else if (editType.equals("ENCHANTMENT_LEVEL")) {
            // エンチャントレベルを設定（この部分は実装省略）
            // 実際の処理はEnchantmentMenuクラスで行う
        }

        // メインメニューに戻る
        MainMenu.open(player);
    }
}