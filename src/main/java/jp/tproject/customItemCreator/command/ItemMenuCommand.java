package jp.tproject.customItemCreator.command;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.gui.MainMenu;
import jp.tproject.customItemCreator.gui.SavedItemsMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * アイテムメニューコマンドを実行するクラス
 */
public class ItemMenuCommand implements CommandExecutor, TabCompleter {

    /**
     * コマンド実行時の処理
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ使用できます。");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("customitemcreator.use")) {
            player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません。");
            return true;
        }

        // サブコマンドの処理
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "list":
                    // ページ指定があるか確認
                    int page = 0;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]) - 1; // 1ページ目は0になるよう調整
                            if (page < 0) page = 0;
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "ページ番号は数値で指定してください。");
                        }
                    }

                    // 保存済みアイテム一覧を表示
                    Map<String, org.bukkit.inventory.ItemStack> items =
                            CustomItemCreator.getInstance().getConfigManager().getAllItems();

                    player.sendMessage(ChatColor.GREEN + "保存済みアイテム一覧を表示します。全 " +
                            items.size() + " 個のアイテムが登録されています。");

                    SavedItemsMenu.open(player, page);
                    return true;

                case "help":
                    // ヘルプを表示
                    showHelp(player);
                    return true;

                case "get":
                    // アイテムIDでアイテムを取得
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "使用法: /itemmenu get <アイテムID>");
                        return true;
                    }

                    String itemId = args[1];
                    try {
                        getItemById(player, itemId);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "アイテムの取得中にエラーが発生しました: " + e.getMessage());
                    }
                    return true;

                case "page":
                    // ページ指定で一覧表示
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "使用法: /itemmenu page <ページ番号>");
                        return true;
                    }

                    try {
                        int pageNum = Integer.parseInt(args[1]) - 1; // 1ページ目は0になるよう調整
                        if (pageNum < 0) pageNum = 0;
                        SavedItemsMenu.open(player, pageNum);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "ページ番号は数値で指定してください。");
                    }
                    return true;
            }
        }

        // デフォルトはメインメニューを開く
        MainMenu.open(player);
        return true;
    }

    /**
     * アイテムIDでアイテムを取得
     * @param player プレイヤー
     * @param itemId アイテムID
     */
    private void getItemById(Player player, String itemId) {
        // IDからアイテムを取得
        var item = CustomItemCreator.getInstance().getConfigManager().getItem(itemId);

        if (item == null) {
            player.sendMessage(ChatColor.RED + "指定されたIDのアイテムが見つかりませんでした: " + itemId);
            return;
        }

        // プレイヤーのインベントリにアイテムを追加
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "アイテムを取得しました。ID: " + itemId);
    }

    /**
     * ヘルプメッセージを表示
     * @param player プレイヤー
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "===== CustomItemCreator ヘルプ =====");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu " + ChatColor.WHITE + "- アイテム作成メニューを開きます");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu list " + ChatColor.WHITE + "- 保存済みアイテム一覧を表示します");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu list <ページ番号> " + ChatColor.WHITE + "- 指定ページのアイテム一覧を表示します");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu page <ページ番号> " + ChatColor.WHITE + "- 指定ページのアイテム一覧を表示します");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu get <アイテムID> " + ChatColor.WHITE + "- 指定IDのアイテムを取得します");
        player.sendMessage(ChatColor.YELLOW + "/itemmenu help " + ChatColor.WHITE + "- このヘルプを表示します");
    }

    /**
     * タブ補完の処理
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        if (!sender.hasPermission("customitemcreator.use")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // サブコマンドの補完
            List<String> subCommands = Arrays.asList("list", "help", "get", "page");
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            // アイテムIDの補完（実装されていない部分を追加）
            Map<String, org.bukkit.inventory.ItemStack> items =
                    CustomItemCreator.getInstance().getConfigManager().getAllItems();

            // すべてのアイテムIDを取得
            for (String itemId : items.keySet()) {
                if (itemId.startsWith(args[1])) {
                    completions.add(itemId);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("page"))) {
            // ページ番号の補完
            Map<String, org.bukkit.inventory.ItemStack> items =
                    CustomItemCreator.getInstance().getConfigManager().getAllItems();

            // アイテム数からページ数を計算
            int totalPages = (int) Math.ceil(items.size() / 45.0);

            for (int i = 1; i <= totalPages; i++) {
                String pageNum = String.valueOf(i);
                if (pageNum.startsWith(args[1])) {
                    completions.add(pageNum);
                }
            }
        }

        return completions;
    }
}