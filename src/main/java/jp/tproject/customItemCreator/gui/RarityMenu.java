package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.model.Rarity;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;

/**
 * レア度選択メニューを管理するクラス
 */
public class RarityMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "レア度を選択";
    private static final int MENU_SIZE = 9;

    /**
     * レア度選択メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 各レア度のボタンを作成
        int slot = 0;
        for (Rarity rarity : Rarity.values()) {
            inventory.setItem(slot++, GuiUtil.createMenuItem(Material.PAPER,
                    rarity.getDisplayName(),
                    Collections.singletonList(ChatColor.GRAY + "クリックして選択")));
        }

        // 戻るボタン
        inventory.setItem(8, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "戻る",
                "メインメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "RARITY");
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        if (slot == 8) { // 戻るボタン
            MainMenu.open(player);
            return;
        }

        // レア度を設定
        if (slot < Rarity.values().length) {
            Rarity selectedRarity = Rarity.values()[slot];

            CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
            customItem.setRarity(selectedRarity);

            player.sendMessage(ChatColor.GREEN + "レア度を " + selectedRarity.getDisplayName() +
                    ChatColor.GREEN + " に設定しました。");
            MainMenu.open(player);
        }
    }
}