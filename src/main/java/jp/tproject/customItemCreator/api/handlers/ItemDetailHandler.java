package jp.tproject.customItemCreator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.api.utils.JsonUtils;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.OutputStream;

/**
 * アイテム詳細を取得するAPIハンドラ
 */
public class ItemDetailHandler implements HttpHandler {
    private final CustomItemCreator plugin;

    public ItemDetailHandler(CustomItemCreator plugin) {
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

            // URLからアイテムIDを取得
            String path = exchange.getRequestURI().getPath();
            String itemId = path.substring(path.lastIndexOf("/") + 1);

            if (itemId.isEmpty()) {
                sendResponse(exchange, 400, "Item ID is required");
                return;
            }

            // アイテムを取得
            ItemStack item = plugin.getConfigManager().getItem(itemId);

            if (item == null) {
                sendResponse(exchange, 404, "Item not found: " + itemId);
                return;
            }

            // JSON形式に変換して返す
            String response = JsonUtils.itemToDetailJson(itemId, item, plugin.getCustomItemKey());

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