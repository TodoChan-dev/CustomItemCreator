package jp.tproject.customItemCreator.gui;


import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.model.Rarity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * テキスト編集を管理するクラス
 * 書籍を使用してアイテム名や説明文を編集
 */
public class TextEditor {

    /**
     * テキストエディタを開く
     * @param player プレイヤー
     * @param editType 編集タイプ（NAME/LORE）
     */
    public static void openEditor(Player player, String editType) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        ItemMeta itemMeta = customItem.getItemStack().getItemMeta();

        if (editType.equals("NAME")) {
            meta.setTitle("アイテム名を入力");

            // 現在のアイテム名を取得（レア度を除く）
            String currentName = "";
            if (itemMeta.hasDisplayName()) {
                currentName = itemMeta.getDisplayName();
                // レア度部分を除去
                for (Rarity rarity : Rarity.values()) {
                    if (currentName.startsWith(rarity.getDisplayName())) {
                        currentName = currentName.substring(rarity.getDisplayName().length()).trim();
                        break;
                    }
                }
            }

            // カラーコードを§から&に変換
            currentName = currentName.replace('§', '&');
            meta.setPages(currentName.isEmpty() ? "アイテム名を入力" : currentName);

        } else if (editType.equals("LORE")) {
            meta.setTitle("説明文を入力");

            // 現在の説明文を取得
            if (itemMeta.hasLore()) {
                StringBuilder loreText = new StringBuilder();
                for (String line : itemMeta.getLore()) {
                    // カラーコードを§から&に変換
                    loreText.append(line.replace('§', '&')).append("\n");
                }
                meta.setPages(loreText.toString());
            } else {
                meta.setPages("説明文を入力\n複数行可能");
            }
        }

        book.setItemMeta(meta);

        // プレイヤーに本を渡す
        player.closeInventory();
        player.getInventory().addItem(book);

        // 編集状態を設定
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, editType);

        player.sendMessage(ChatColor.GREEN +
                (editType.equals("NAME") ? "アイテム名" : "説明文") +
                "を編集する本を渡しました。編集して閉じてください。");
    }

    /**
     * 書籍編集完了時の処理
     * @param player プレイヤー
     * @param bookMeta 編集された本のメタデータ
     */
    public static void handleBookEdit(Player player, BookMeta bookMeta) {
        String editType = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);

        if (!bookMeta.hasPages()) {
            player.sendMessage(ChatColor.RED + "ページが空です。編集をキャンセルします。");
            return;
        }

        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        if (editType.equals("NAME")) {
            // アイテム名を設定
            String name = bookMeta.getPage(1).trim();
            customItem.setName(name);
            player.sendMessage(ChatColor.GREEN + "アイテム名を設定しました。");

        } else if (editType.equals("LORE")) {
            // 説明文を設定
            String loreText = bookMeta.getPage(1);
            List<String> lore = new ArrayList<>(Arrays.asList(loreText.split("\n")));
            customItem.setLore(lore);
            player.sendMessage(ChatColor.GREEN + "説明文を設定しました。");
        }

        // 本を削除して元のメニューに戻る
        player.getInventory().remove(Material.WRITTEN_BOOK);
        player.getInventory().remove(Material.WRITABLE_BOOK);

        MainMenu.open(player);
    }
}
