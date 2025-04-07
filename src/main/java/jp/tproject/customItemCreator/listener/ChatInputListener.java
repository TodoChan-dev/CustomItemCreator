package jp.tproject.customItemCreator.listener;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.gui.MainMenu;
import jp.tproject.customItemCreator.model.CustomItem;
import jp.tproject.customItemCreator.model.Rarity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * チャット入力を処理するリスナークラス
 */
public class ChatInputListener implements Listener {

    private final CustomItemCreator plugin;
    private final Map<UUID, EditSession> editSessions = new HashMap<>();

    /**
     * チャット入力リスナーを初期化
     * @param plugin プラグインインスタンス
     */
    public ChatInputListener(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤーのアイテム名編集セッションを開始
     * @param player プレイヤー
     */
    public void startNameEdit(Player player) {
        CustomItem customItem = plugin.getItemManager().getPlayerItem(player);
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

        // 編集セッションを開始
        EditSession session = new EditSession(EditType.NAME);
        editSessions.put(player.getUniqueId(), session);

        // プレイヤーに指示を表示
        player.sendMessage(ChatColor.GREEN + "----- アイテム名編集 -----");
        player.sendMessage(ChatColor.GREEN + "現在の名前: " +
                (currentName.isEmpty() ? ChatColor.GRAY + "(なし)" : ChatColor.WHITE + currentName));
        player.sendMessage(ChatColor.GREEN + "チャットに新しい名前を入力してください。");
        player.sendMessage(ChatColor.GREEN + "カラーコードには&記号が使えます。");
        player.sendMessage(ChatColor.RED + "キャンセルするには「cancel」と入力してください。");
    }

    /**
     * プレイヤーのロア編集セッションを開始
     * @param player プレイヤー
     */
    public void startLoreEdit(Player player) {
        CustomItem customItem = plugin.getItemManager().getPlayerItem(player);
        List<String> currentLore = new ArrayList<>();

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            currentLore = customItem.getItemStack().getItemMeta().getLore();
        }

        // 編集セッションを開始
        EditSession session = new EditSession(EditType.LORE);
        editSessions.put(player.getUniqueId(), session);

        // プレイヤーに指示を表示
        player.sendMessage(ChatColor.GREEN + "----- ロア（説明文）編集 -----");

        if (currentLore.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "現在のロア: " + ChatColor.GRAY + "(なし)");
        } else {
            player.sendMessage(ChatColor.GREEN + "現在のロア:");
            for (int i = 0; i < currentLore.size(); i++) {
                player.sendMessage(ChatColor.WHITE + "  " + (i + 1) + ": " + currentLore.get(i));
            }
        }

        player.sendMessage(ChatColor.GREEN + "チャットに新しいロアを入力してください。");
        player.sendMessage(ChatColor.GREEN + "複数行の場合は\\nで区切ってください。");
        player.sendMessage(ChatColor.GREEN + "例: 「一行目\\n二行目\\n三行目」");
        player.sendMessage(ChatColor.GREEN + "カラーコードには&記号が使えます。");
        player.sendMessage(ChatColor.RED + "キャンセルするには「cancel」と入力してください。");
    }

    /**
     * プレイヤーのロア行編集セッションを開始
     * @param player プレイヤー
     * @param lineNumber 編集する行番号（0から始まる）
     */
    public void startLoreLineEdit(Player player, int lineNumber) {
        CustomItem customItem = plugin.getItemManager().getPlayerItem(player);
        String currentLine = "";

        if (customItem.getItemStack().getItemMeta().hasLore()) {
            List<String> loreList = customItem.getItemStack().getItemMeta().getLore();
            if (lineNumber < loreList.size()) {
                currentLine = loreList.get(lineNumber).replace('§', '&');
            }
        }

        // 編集セッションを開始
        EditSession session = new EditSession(EditType.LORE_LINE);
        session.setLineNumber(lineNumber);
        editSessions.put(player.getUniqueId(), session);

        // プレイヤーに指示を表示
        player.sendMessage(ChatColor.GREEN + "----- ロア行編集（" + (lineNumber + 1) + "行目） -----");
        player.sendMessage(ChatColor.GREEN + "現在の行: " +
                (currentLine.isEmpty() ? ChatColor.GRAY + "(なし)" : ChatColor.WHITE + currentLine));
        player.sendMessage(ChatColor.GREEN + "チャットに新しい内容を入力してください。");
        player.sendMessage(ChatColor.GREEN + "カラーコードには&記号が使えます。");
        player.sendMessage(ChatColor.RED + "キャンセルするには「cancel」と入力してください。");
    }

    /**
     * 編集セッションを終了
     * @param player プレイヤー
     */
    public void endEditSession(Player player) {
        editSessions.remove(player.getUniqueId());
    }

    /**
     * プレイヤーが編集モードかどうか
     * @param player プレイヤー
     * @return 編集モードならtrue
     */
    public boolean isEditing(Player player) {
        return editSessions.containsKey(player.getUniqueId());
    }

    /**
     * プレイヤーのチャット入力を処理
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 編集セッションがあるか確認
        if (editSessions.containsKey(playerId)) {
            // チャットイベントをキャンセル（一般チャットに表示しない）
            event.setCancelled(true);

            // 入力テキスト
            String input = event.getMessage();

            // キャンセルコマンドの確認
            if (input.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.YELLOW + "編集をキャンセルしました。");

                // 同期タスクとしてメインメニューを開く
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MainMenu.open(player);
                });

                editSessions.remove(playerId);
                return;
            }

            // 編集セッションを取得
            EditSession session = editSessions.get(playerId);

            // 同期タスクとして処理を実行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                processInput(player, input, session);
            });
        }
    }

    /**
     * プレイヤーの入力を処理
     * @param player プレイヤー
     * @param input 入力テキスト
     * @param session 編集セッション
     */
    private void processInput(Player player, String input, EditSession session) {
        CustomItem customItem = plugin.getItemManager().getPlayerItem(player);

        switch (session.getType()) {
            case NAME:
                // アイテム名を設定
                customItem.setName(input);
                player.sendMessage(ChatColor.GREEN + "アイテム名を「" +
                        ChatColor.translateAlternateColorCodes('&', input) +
                        ChatColor.GREEN + "」に設定しました。");
                break;

            case LORE:
                // ロアを設定
                // \nで分割して複数行に対応
                String[] lines = input.split("\\\\n");
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

            case LORE_LINE:
                // 特定の行を編集
                int lineNumber = session.getLineNumber();

                List<String> currentLore = new ArrayList<>();
                if (customItem.getItemStack().getItemMeta().hasLore()) {
                    currentLore = new ArrayList<>(customItem.getItemStack().getItemMeta().getLore());
                }

                // 編集行がロアリストのサイズを超える場合は拡張
                while (currentLore.size() <= lineNumber) {
                    currentLore.add("");
                }

                // 行を更新
                currentLore.set(lineNumber, input);

                customItem.setLore(currentLore);
                player.sendMessage(ChatColor.GREEN + "説明文の " + (lineNumber + 1) + " 行目を更新しました。");
                break;
        }

        // 編集セッションを終了
        editSessions.remove(player.getUniqueId());

        // メインメニューに戻る
        MainMenu.open(player);
    }

    /**
     * 編集タイプ
     */
    private enum EditType {
        NAME,       // アイテム名編集
        LORE,       // ロア全体編集
        LORE_LINE   // ロア特定行編集
    }

    /**
     * 編集セッション
     */
    private static class EditSession {
        private final EditType type;
        private int lineNumber; // ロア編集時の行番号

        public EditSession(EditType type) {
            this.type = type;
        }

        public EditType getType() {
            return type;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
    }
}