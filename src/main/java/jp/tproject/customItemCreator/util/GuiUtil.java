package jp.tproject.customItemCreator.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI関連のユーティリティクラス
 */
public class GuiUtil {

    /**
     * メニューアイテムを作成
     * @param material アイテムの素材
     * @param name アイテム名
     * @param lore 説明文
     * @return 作成されたItemStack
     */
    public static ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            if (lore != null) {
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 項目名と説明のペアからメニューアイテムを作成
     * @param material アイテムの素材
     * @param name アイテム名
     * @param description 説明（一行）
     * @return 作成されたItemStack
     */
    public static ItemStack createMenuItem(Material material, String name, String description) {
        List<String> lore = description != null ?
                Arrays.asList(ChatColor.GRAY + description) : null;
        return createMenuItem(material, name, lore);
    }

    /**
     * ボーダーアイテムを作成（メニューの枠として使用）
     * @return 枠用アイテム
     */
    public static ItemStack createBorderItem() {
        return createMenuItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ", (List<String>) null);
    }

    /**
     * メニュー枠を設定（外周を指定アイテムで埋める）
     * @param inventory メニューアイテムの配列
     * @param size インベントリサイズ
     * @param borderItem 枠用アイテム
     */
    public static void setBorder(ItemStack[] inventory, int size, ItemStack borderItem) {
        // 上部と下部
        for (int i = 0; i < 9; i++) {
            inventory[i] = borderItem;
            inventory[size - 9 + i] = borderItem;
        }

        // 左右
        for (int i = 9; i < size - 9; i += 9) {
            inventory[i] = borderItem;
            inventory[i + 8] = borderItem;
        }
    }

    /**
     * テキスト内のカラーコードを変換
     * @param text 変換するテキスト
     * @return 変換後のテキスト
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * テキストリスト内のカラーコードを変換
     * @param lines 変換するテキストのリスト
     * @return 変換後のテキストリスト
     */
    public static List<String> colorize(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }
}