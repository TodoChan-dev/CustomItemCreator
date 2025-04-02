package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

/**
 * メインメニューを表示・管理するクラス
 */
public class MainMenu {

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "アイテムクリエーター";
    private static final int MENU_SIZE = 54; // 6行 (9x6)

    /**
     * メインメニューを開く
     * @param player メニューを開くプレイヤー
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);

        // プレイヤーのカスタムアイテムを取得または新規作成
        if (!CustomItemCreator.getInstance().getItemManager().isEditing(player)) {
            CustomItemCreator.getInstance().getItemManager().setPlayerItem(
                    player, new CustomItem(Material.DIAMOND_SWORD));
            CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "MAIN");
        }

        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        // メニューアイテム
        inventory.setItem(13, GuiUtil.createMenuItem(Material.NAME_TAG,
                ChatColor.YELLOW + "アイテム名を編集",
                "クリックしてアイテム名を編集します"));

        inventory.setItem(20, GuiUtil.createMenuItem(Material.BOOK,
                ChatColor.YELLOW + "説明文（ロア）を編集",
                "クリックして説明文を編集します"));

        inventory.setItem(22, GuiUtil.createMenuItem(Material.DIAMOND,
                ChatColor.YELLOW + "レア度を設定",
                "クリックしてレア度を選択します"));

        inventory.setItem(24, GuiUtil.createMenuItem(Material.PAINTING,
                ChatColor.YELLOW + "カスタムモデルデータ",
                "クリックして数値を設定します"));

        inventory.setItem(29, GuiUtil.createMenuItem(Material.ENCHANTED_BOOK,
                ChatColor.YELLOW + "エンチャントを追加",
                "クリックしてエンチャントを追加します"));

        inventory.setItem(31, GuiUtil.createMenuItem(Material.BEACON,
                ChatColor.YELLOW + "属性を設定",
                "クリックして属性を編集します"));

        inventory.setItem(33, GuiUtil.createMenuItem(Material.BARRIER,
                ChatColor.YELLOW + "エンチャント/属性をリセット",
                "クリックして全てのエンチャントと属性をリセットします"));

        // プレビュー
        inventory.setItem(49, customItem.getItemStack());

        // 作成ボタン
        inventory.setItem(53, GuiUtil.createMenuItem(Material.EMERALD_BLOCK,
                ChatColor.GREEN + "作成",
                Arrays.asList(
                        ChatColor.GRAY + "クリックしてアイテムを作成します",
                        ChatColor.GRAY + "作成したアイテムはDBに保存されます"
                )));

        // キャンセルボタン
        inventory.setItem(45, GuiUtil.createMenuItem(Material.ARROW,
                ChatColor.RED + "キャンセル",
                "作成をキャンセルします"));

        player.openInventory(inventory);
    }

    /**
     * クリックイベントを処理
     * @param player プレイヤー
     * @param slot クリックされたスロット
     */
    public static void handleClick(Player player, int slot) {
        switch (slot) {
            case 13: // アイテム名を編集
                SignEditor signEditor = new SignEditor(CustomItemCreator.getInstance());
                signEditor.editName(player);
                break;

            case 20: // 説明文（ロア）を編集
                LoreMenu.open(player);
                break;

            case 22: // レア度を設定
                RarityMenu.open(player);
                break;

            case 24: // カスタムモデルデータを設定
                CustomItem item = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
                int currentModelData = item.getItemStack().getItemMeta().hasCustomModelData() ?
                        item.getItemStack().getItemMeta().getCustomModelData() : 0;

                NumericInputMenu.open(player, "カスタムモデルデータ数値を入力",
                        currentModelData, "CUSTOM_MODEL_DATA");
                break;

            case 29: // エンチャントを追加
                EnchantmentMenu.open(player);
                break;

            case 31: // 属性を設定
                AttributeMenu.open(player);
                break;

            case 33: // エンチャント/属性をリセット
                CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
                customItem.resetEnchantmentsAndAttributes();
                player.sendMessage(ChatColor.GREEN + "エンチャントと属性がリセットされました。");
                open(player); // メニューを再表示
                break;

            case 45: // キャンセル
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "アイテム作成をキャンセルしました。");
                CustomItemCreator.getInstance().getItemManager().clearPlayerData(player);
                break;

            case 53: // 作成確認
                ConfirmationMenu.open(player);
                break;
        }
    }
}
