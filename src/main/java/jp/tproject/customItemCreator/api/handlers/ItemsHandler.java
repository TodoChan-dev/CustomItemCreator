package jp.tproject.customItemCreator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jp.tproject.customItemCreator.CustomItemCreator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * アイテム一覧を取得するAPIハンドラ
 */
public class ItemsHandler implements HttpHandler {
    private final CustomItemCreator plugin;

    public ItemsHandler(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // CORSヘッダーを設定
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            // OPTIONSリクエストの処理
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // GETリクエスト以外は405エラー
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // アイテム一覧を取得
            Map<String, ItemStack> items = plugin.getConfigManager().getAllItems();

            // JSON形式に変換して返す
            String response = serializeItemsToJson(items);

            // 成功レスポンスを送信
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            // エラーが発生した場合は500エラー
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    /**
     * アイテムマップをJSON形式にシリアライズ
     * @param items アイテムマップ
     * @return JSON文字列
     */
    private String serializeItemsToJson(Map<String, ItemStack> items) {
        List<String> itemJsonList = new ArrayList<>();

        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            String itemId = entry.getKey();
            ItemStack item = entry.getValue();

            // アイテムの基本情報を取得
            String itemType = item.getType().name();
            int amount = item.getAmount();

            // 表示名を取得
            String displayName = item.getType().name();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                displayName = escapeJson(item.getItemMeta().getDisplayName());
            }

            // カスタムモデルデータを取得
            int customModelData = 0;
            if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                customModelData = item.getItemMeta().getCustomModelData();
            }

            // JSONオブジェクトを作成
            itemJsonList.add(String.format(
                    "{\"id\":\"%s\",\"type\":\"%s\",\"displayName\":\"%s\",\"amount\":%d,\"customModelData\":%d}",
                    itemId, itemType, displayName, amount, customModelData
            ));
        }

        return "[" + String.join(",", itemJsonList) + "]";
    }

    /**
     * JSON文字列をエスケープ
     * @param text エスケープする文字列
     * @return エスケープされた文字列
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * エラーレスポンスを送信
     * @param exchange HTTPエクスチェンジ
     * @param statusCode ステータスコード
     * @param message エラーメッセージ
     * @throws IOException IO例外
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}