package jp.tproject.customItemCreator.gui;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.model.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 看板を使ったテキスト編集機能
 */
public class SignEditor implements Listener {

    private final CustomItemCreator plugin;
    private static final String METADATA_KEY = "customitemcreator.edit";

    /**
     * 看板エディタを初期化
     * @param plugin プラグインインスタンス
     */
    public SignEditor(CustomItemCreator plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 看板エディタを開く
     * @param player プレイヤー
     * @param editType 編集タイプ (NAME/LORE/LORE_LINE)
     * @param initialText 初期テキスト
     * @param lineNumber ロア行番号（ロア行編集時のみ使用）
     */
    public void openEditor(Player player, String editType, String initialText, int lineNumber) {
        // プレイヤーの前に一時的な看板を設置
        Location loc = getSignLocation(player);
        if (loc == null) {
            player.sendMessage(ChatColor.RED + "看板を設置できる場所が見つかりません。");
            return;
        }

        Block block = loc.getBlock();
        Material originalType = block.getType();

        // 看板を設置
        block.setType(Material.OAK_SIGN);

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            // 編集情報をメタデータとして設定
            sign.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, editType));

            // 初期テキストを設定
            if (initialText != null && !initialText.isEmpty()) {
                // カラーコードを&に変換
                initialText = initialText.replace('§', '&');

                if (editType.equals("LORE") || editType.equals("LORE_LINE")) {
                    // ロア編集の場合は複数行に分割
                    String[] lines = initialText.split("\n");
                    for (int i = 0; i < Math.min(lines.length, 4); i++) {
                        sign.setLine(i, lines[i]);
                    }
                } else {
                    // アイテム名編集の場合は1行目に設定
                    sign.setLine(0, initialText);
                }
            }

            sign.update();

            // 行番号情報を保存（ロア行編集用）
            if (editType.equals("LORE_LINE")) {
                CustomItemCreator.getInstance().getItemManager().setPlayerData(
                        player, "lore_line_number", lineNumber);
            }

            // 看板エディタを開く
            player.openSign(sign);

            // 元のブロックに戻すタスクをスケジュール（10秒後）
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (block.getType() == Material.OAK_SIGN || block.getType() == Material.OAK_WALL_SIGN) {
                    block.setType(originalType);
                }
            }, 200L);
        } else {
            player.sendMessage(ChatColor.RED + "看板の設置に失敗しました。");
            block.setType(originalType);
        }
    }

    /**
     * アイテム名編集用に看板を開く
     * @param player プレイヤー
     */
    public void editName(Player player) {
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        String currentName = "";

        if (customItem.getItemStack().getItemMeta().hasDisplayName()) {
            // 現在の名前からレア度部分を除去
            String displayName = customItem.getItemStack().getItemMeta().getDisplayName();
            for (Rarity rarity : Rarity.values()) {
                if (displayName.startsWith(rarity.getDisplayName())) {
                    currentName = displayName.substring(rarity.getDisplayName().length()).trim();
                    break;
                }
            }
        }

        openEditor(player, "NAME", currentName, 0);
        player.sendMessage(ChatColor.GREEN + "アイテム名を入力してください。カラーコードには&記号が使えます。");

        // 編集状態を保存
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "SIGN_EDIT_NAME");
    }

    /**
     * ロア編集用に看板を開く
     * @param player プレイヤー
     */
    public void editLore(Player player) {
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        String currentLore = "";

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            // 現在のロアを取得
            List<String> loreList = customItem.getItemStack().getItemMeta().getLore();
            StringBuilder sb = new StringBuilder();
            for (String line : loreList) {
                sb.append(line.replace('§', '&')).append("\n");
            }
            currentLore = sb.toString().trim();
        }

        openEditor(player, "LORE", currentLore, 0);
        player.sendMessage(ChatColor.GREEN + "説明文を入力してください。最大4行まで設定できます。");
        player.sendMessage(ChatColor.GREEN + "カラーコードには&記号が使えます。");

        // 編集状態を保存
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "SIGN_EDIT_LORE");
    }

    /**
     * ロア行編集用に看板を開く
     * @param player プレイヤー
     * @param lineNumber 編集する行番号（0から始まる）
     */
    public void editLoreLine(Player player, int lineNumber) {
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);
        String currentLine = "";

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            List<String> loreList = customItem.getItemStack().getItemMeta().getLore();
            if (lineNumber < loreList.size()) {
                currentLine = loreList.get(lineNumber).replace('§', '&');
            }
        }

        openEditor(player, "LORE_LINE", currentLine, lineNumber);
        player.sendMessage(ChatColor.GREEN + "説明文の " + (lineNumber + 1) + " 行目を編集してください。");
        player.sendMessage(ChatColor.GREEN + "カラーコードには&記号が使えます。");

        // 編集状態を保存
        CustomItemCreator.getInstance().getItemManager().setPlayerEditState(player, "SIGN_EDIT_LORE_LINE");
    }

    /**
     * 看板変更イベントを処理
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Sign sign = (Sign) event.getBlock().getState();

        if (!sign.hasMetadata(METADATA_KEY)) {
            return;
        }

        String editType = sign.getMetadata(METADATA_KEY).get(0).asString();
        String editState = CustomItemCreator.getInstance().getItemManager().getPlayerEditState(player);

        // 正しい編集状態か確認
        if (!editState.startsWith("SIGN_EDIT_")) {
            return;
        }

        // 看板データからテキストを取得
        String[] lines = event.getLines();

        // アイテムに適用
        CustomItem customItem = CustomItemCreator.getInstance().getItemManager().getPlayerItem(player);

        switch (editType) {
            case "NAME":
                // アイテム名を設定
                String name = lines[0];
                if (name != null && !name.isEmpty()) {
                    customItem.setName(name);
                    player.sendMessage(ChatColor.GREEN + "アイテム名を「" +
                            ChatColor.translateAlternateColorCodes('&', name) +
                            ChatColor.GREEN + "」に設定しました。");
                }
                break;

            case "LORE":
                // ロアを設定
                List<String> lore = new ArrayList<>();
                for (String line : lines) {
                    if (line != null && !line.isEmpty()) {
                        lore.add(line);
                    }
                }

                if (!lore.isEmpty()) {
                    customItem.setLore(lore);
                    player.sendMessage(ChatColor.GREEN + "説明文を設定しました。");
                }
                break;

            case "LORE_LINE":
                // 特定の行を編集
                int lineNumber = (int) CustomItemCreator.getInstance().getItemManager()
                        .getPlayerData(player, "lore_line_number");

                List<String> currentLore = new ArrayList<>();
                if (customItem.getItemStack().getItemMeta().hasLore()) {
                    currentLore = new ArrayList<>(customItem.getItemStack().getItemMeta().getLore());
                }

                // 編集行がロアリストのサイズを超える場合は拡張
                while (currentLore.size() <= lineNumber) {
                    currentLore.add("");
                }

                // 行を更新
                currentLore.set(lineNumber, lines[0]);

                customItem.setLore(currentLore);
                player.sendMessage(ChatColor.GREEN + "説明文の " + (lineNumber + 1) + " 行目を更新しました。");
                break;
        }

        // 看板を削除して元のブロックに戻す
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getBlock().getType() == Material.OAK_SIGN ||
                    event.getBlock().getType() == Material.OAK_WALL_SIGN) {
                event.getBlock().setType(Material.AIR);
            }
        }, 1L);

        // メインメニューに戻る
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MainMenu.open(player);
        }, 2L);
    }

    /**
     * 看板を設置する位置を取得
     * @param player プレイヤー
     * @return 設置位置
     */
    private Location getSignLocation(Player player) {
        Location loc = player.getLocation().clone();

        // プレイヤーの視線の方向に1ブロック先
        loc.add(player.getLocation().getDirection().multiply(1));

        // 床に設置
        loc.setY(Math.floor(loc.getY()));

        // 設置可能か確認
        if (loc.getBlock().getType() == Material.AIR ||
                loc.getBlock().getType() == Material.CAVE_AIR ||
                loc.getBlock().isReplaceable()) {
            return loc;
        }

        // プレイヤーの足元も試す
        loc = player.getLocation().clone();
        if (loc.getBlock().getType() == Material.AIR ||
                loc.getBlock().getType() == Material.CAVE_AIR ||
                loc.getBlock().isReplaceable()) {
            return loc;
        }

        // 頭上も試す
        loc = player.getLocation().clone();
        loc.setY(loc.getY() + 1);
        if (loc.getBlock().getType() == Material.AIR ||
                loc.getBlock().getType() == Material.CAVE_AIR ||
                loc.getBlock().isReplaceable()) {
            return loc;
        }

        return null;
    }
}