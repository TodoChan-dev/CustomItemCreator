package jp.tproject.customItemCreator.command;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ストレージ管理コマンドを実行するクラス
 */
public class StorageCommand implements CommandExecutor, TabCompleter {

    private final CustomItemCreator plugin;

    /**
     * コマンドを初期化
     * @param plugin プラグインインスタンス
     */
    public StorageCommand(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    /**
     * コマンド実行時の処理
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customitemcreator.admin")) {
            sender.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません。");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showStorageInfo(sender);
                return true;

            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "設定を再読み込みしました。ストレージタイプ: " +
                        plugin.getConfigManager().getStorageType());
                return true;

            case "migrate":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "移行先を指定してください: mysql または yaml");
                    return true;
                }

                migrateStorage(sender, args[1]);
                return true;

            default:
                showHelp(sender);
                return true;
        }
    }

    /**
     * ストレージ情報を表示
     * @param sender コマンド送信者
     */
    private void showStorageInfo(CommandSender sender) {
        ConfigManager configManager = plugin.getConfigManager();

        sender.sendMessage(ChatColor.GOLD + "===== CustomItemCreator ストレージ情報 =====");
        sender.sendMessage(ChatColor.YELLOW + "現在のストレージタイプ: " + ChatColor.WHITE + configManager.getStorageType());

        if (configManager.getStorageType() == ConfigManager.StorageType.YAML) {
            sender.sendMessage(ChatColor.YELLOW + "ファイルパス: " + ChatColor.WHITE +
                    plugin.getDataFolder().getAbsolutePath() + "/items.yml");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "MySQLホスト: " + ChatColor.WHITE +
                    plugin.getConfig().getString("storage.mysql.host", "localhost") + ":" +
                    plugin.getConfig().getInt("storage.mysql.port", 3306));
            sender.sendMessage(ChatColor.YELLOW + "MySQLデータベース: " + ChatColor.WHITE +
                    plugin.getConfig().getString("storage.mysql.database", "minecraft"));
            sender.sendMessage(ChatColor.YELLOW + "テーブルプレフィックス: " + ChatColor.WHITE +
                    plugin.getConfig().getString("storage.mysql.table-prefix", "customitem_"));
        }

        sender.sendMessage(ChatColor.YELLOW + "アイテム数: " + ChatColor.WHITE +
                configManager.getAllItems().size());
    }

    /**
     * ストレージを移行
     * @param sender コマンド送信者
     * @param target 移行先 (mysql/yaml)
     */
    private void migrateStorage(CommandSender sender, String target) {
        ConfigManager configManager = plugin.getConfigManager();
        int count = 0;

        if (target.equalsIgnoreCase("mysql")) {
            // YAMLからMySQLへの移行
            if (configManager.getStorageType() == ConfigManager.StorageType.MYSQL) {
                count = configManager.migrateFromYamlToMysql();
                sender.sendMessage(ChatColor.GREEN + "YAMLからMySQLに " + count + " 個のアイテムを移行しました。");
            } else {
                sender.sendMessage(ChatColor.RED + "MySQLが有効になっていません。config.ymlで有効にしてください。");
            }
        } else if (target.equalsIgnoreCase("yaml")) {
            // MySQLからYAMLへの移行
            count = configManager.migrateFromMysqlToYaml();
            sender.sendMessage(ChatColor.GREEN + "MySQLからYAMLに " + count + " 個のアイテムを移行しました。");
        } else {
            sender.sendMessage(ChatColor.RED + "無効な移行先です。mysql または yaml を指定してください。");
        }
    }

    /**
     * ヘルプメッセージを表示
     * @param sender コマンド送信者
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== CustomItemCreator ストレージコマンド =====");
        sender.sendMessage(ChatColor.YELLOW + "/itemstorage info " + ChatColor.WHITE + "- ストレージ情報を表示します");
        sender.sendMessage(ChatColor.YELLOW + "/itemstorage reload " + ChatColor.WHITE + "- 設定を再読み込みします");
        sender.sendMessage(ChatColor.YELLOW + "/itemstorage migrate <mysql|yaml> " + ChatColor.WHITE + "- データを移行します");
    }

    /**
     * タブ補完の処理
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("customitemcreator.admin")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // サブコマンドの補完
            List<String> subCommands = Arrays.asList("info", "reload", "migrate");
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("migrate")) {
            // 移行先の補完
            List<String> targets = Arrays.asList("mysql", "yaml");
            for (String target : targets) {
                if (target.startsWith(args[1].toLowerCase())) {
                    completions.add(target);
                }
            }
        }

        return completions;
    }
}