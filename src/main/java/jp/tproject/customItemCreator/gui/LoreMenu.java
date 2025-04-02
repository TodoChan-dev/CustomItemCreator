package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ロア（説明文）編集メニューを管理するクラス
 */
public class LoreMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "説明文（ロア）編集";
    private static final int MENU_SIZE = 54; // 6行 (9x6)

    /**
     * ロア編集メニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // 現在のカスタムアイテムを取得
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        List<String> currentLore = new ArrayList<>();

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            currentLore = customItem.getItemStack().getItemMeta().getLore();
        }

        // 全体編集ボタン
        inventory.setItem(0, GuiUtil.createMenuItem(Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "全体を編集",
                "クリックして説明文全体を編集します"));

        // 各行を表示・編集ボタン
        int slot = 9;
        for (int i = 0; i < Math.min(currentLore.size(), 5*9); i++) {
            String line = currentLore.get(i);

            // 行の内容を表示
            inventory.setItem(slot, GuiUtil.createMenuItem(Material.PAPER,
                    ChatColor.WHITE + "行 " + (i + 1),
                    Arrays.asList(
                            line, // 実際のロア行
                            "",
                            ChatColor.GRAY + "クリックして編集"
                    )));

            // 削除ボタン
            inventory.setItem(slot + 1, GuiUtil.createMenuItem(Material.BARRIER,
                    ChatColor.RED + "削除",
                    "この行を削除します"));

            // 上へ移動ボタン（最初の行以外）
            if (i > 0) {
                inventory.setItem(slot + 2, GuiUtil.createMenuItem(Material.ARROW,
                        ChatColor.AQUA + "上へ移動",
                        "この行を上に移動します"));
            }

            // 下へ移動ボタン（最後の行以外）
            if (i < currentLore.size() - 1) {
                inventory.setItem(slot + 3, GuiUtil.createMenuItem(Material.ARROW,
                        ChatColor.AQUA + "下へ移動",
                        "この行を下に移動します"));
            }

            slot += 9; // 次の行へ
        }

        // 新しい行を追加ボタン
        inventory.setItem(45, GuiUtil.createMenuItem(Material.LIME_DYE,
                ChatColor.GREEN + "新しい行を追加",
                "クリックして新しい行を追加します"));

        // すべての行をクリアボタン
        inventory.setItem(46, GuiUtil.createMenuItem(Material.LAVA_BUCKET,
                ChatColor.RED + "すべての行をクリア",
                "全ての説明文を削除します"));

        // アイテムプレビュー
        inventory.setItem(49, customItem.getItemStack());

        // 完了ボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.EMERALD,
                ChatColor.GREEN + "完了",
                "メインメニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "LORE_EDIT");
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        // 現在のカスタムアイテムを取得
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        List<String> currentLore = new ArrayList<>();

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            currentLore = new ArrayList<>(customItem.getItemStack().getItemMeta().getLore());
        }

        if (slot == 0) {
            // 全体を編集
            SignEditor signEditor = new SignEditor(CustomItemCreator.getInstance());
            signEditor.editLore(player);
            return;
        }

        if (slot == 45) {
            // 新しい行を追加
            int newLineIndex = currentLore.size();
            SignEditor signEditor = new SignEditor(CustomItemCreator.getInstance());
            signEditor.editLoreLine(player, newLineIndex);
            return;
        }

        if (slot == 46) {
            // すべての行をクリア
            confirmClearLore(player);
            return;
        }

        if (slot == 53) {
            // 完了 - メインメニューに戻る
            MainMenu.open(player);
            return;
        }

        // 行編集ボタンの処理
        if (slot >= 9 && slot < 45 && slot % 9 == 0) {
            int lineIndex = (slot - 9) / 9;
            if (lineIndex < currentLore.size()) {
                SignEditor signEditor = new SignEditor(CustomItemCreator.getInstance());
                signEditor.editLoreLine(player, lineIndex);
            }
            return;
        }

        // 行削除ボタンの処理
        if (slot >= 10 && slot < 46 && slot % 9 == 1) {
            int lineIndex = (slot - 10) / 9;
            if (lineIndex < currentLore.size()) {
                currentLore.remove(lineIndex);
                customItem.setLore(currentLore);
                player.sendMessage(ChatColor.GREEN + "行 " + (lineIndex + 1) + " を削除しました。");
                open(player);
            }
            return;
        }

        // 上へ移動ボタンの処理
        if (slot >= 11 && slot < 47 && slot % 9 == 2) {
            int lineIndex = (slot - 11) / 9;
            if (lineIndex > 0 && lineIndex < currentLore.size()) {
                // 行を入れ替え
                String temp = currentLore.get(lineIndex);
                currentLore.set(lineIndex, currentLore.get(lineIndex - 1));
                currentLore.set(lineIndex - 1, temp);

                customItem.setLore(currentLore);
                player.sendMessage(ChatColor.GREEN + "行 " + (lineIndex + 1) + " を上に移動しました。");
                open(player);
            }
            return;
        }

        // 下へ移動ボタンの処理
        if (slot >= 12 && slot < 48 && slot % 9 == 3) {
            int lineIndex = (slot - 12) / 9;
            if (lineIndex < currentLore.size() - 1) {
                // 行を入れ替え
                String temp = currentLore.get(lineIndex);
                currentLore.set(lineIndex, currentLore.get(lineIndex + 1));
                currentLore.set(lineIndex + 1, temp);

                customItem.setLore(currentLore);
                player.sendMessage(ChatColor.GREEN + "行 " + (lineIndex + 1) + " を下に移動しました。");
                open(player);
            }
            return;
        }
    }

    /**
     * ロアのクリアを確認するメニューを開く
     * @param player プレイヤー
     */
    private static void confirmClearLore(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9,
                ChatColor.RED + "説明文をクリアしますか？");

        // 確認ボタン
        inventory.setItem(3, GuiUtil.createMenuItem(Material.RED_CONCRETE,
                ChatColor.RED + "クリアする",
                "全ての説明文を削除します"));

        // キャンセルボタン
        inventory.setItem(5, GuiUtil.createMenuItem(Material.GREEN_CONCRETE,
                ChatColor.GREEN + "キャンセル",
                "編集メニューに戻ります"));

        player.openInventory(inventory);
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "LORE_CLEAR_CONFIRM");
    }

    /**
     * ロアクリア確認のクリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClearConfirmClick(Player player, int slot) {
        if (slot == 3) {
            // クリアする
            CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

            // 空のロアリストを設定
            customItem.setLore(Collections.emptyList());
            player.sendMessage(ChatColor.GREEN + "全ての説明文をクリアしました。");
            open(player);

        } else if (slot == 5) {
            // キャンセル
            open(player);
        }
    }
}