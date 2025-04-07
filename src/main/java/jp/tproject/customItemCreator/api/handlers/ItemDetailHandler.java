package jp.tproject.customItemCreator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jp.tproject.customItemCreator.CustomItemCreator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            String response = serializeItemToJson(itemId, item);

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
     * アイテムをJSON形式にシリアライズ
     * @param itemId アイテムID
     * @param item アイテム
     * @return JSON文字列
     */
    @SuppressWarnings("removal")
    private String serializeItemToJson(String itemId, ItemStack item) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // 基本情報
        json.append("\"id\":\"").append(itemId).append("\",");
        json.append("\"type\":\"").append(item.getType().name()).append("\",");
        json.append("\"amount\":").append(item.getAmount()).append(",");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 表示名
            if (meta.hasDisplayName()) {
                json.append("\"displayName\":\"").append(escapeJson(meta.getDisplayName())).append("\",");
            } else {
                json.append("\"displayName\":\"").append(item.getType().name()).append("\",");
            }

            // ロア（説明文）
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                json.append("\"lore\":[");
                List<String> loreJsonList = new ArrayList<>();
                for (String loreLine : lore) {
                    loreJsonList.add("\"" + escapeJson(loreLine) + "\"");
                }
                json.append(String.join(",", loreJsonList));
                json.append("],");
            } else {
                json.append("\"lore\":[],");
            }

            // エンチャント
            json.append("\"enchantments\":[");
            List<String> enchantmentList = new ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                String enchantName = entry.getKey().getKey().getKey();
                int level = entry.getValue();
                enchantmentList.add(String.format(
                        "{\"name\":\"%s\",\"level\":%d}",
                        enchantName, level
                ));
            }
            json.append(String.join(",", enchantmentList));
            json.append("],");

            // 属性（アトリビュート）
            json.append("\"attributes\":[");
            if (meta.hasAttributeModifiers()) {
                List<String> attributeList = new ArrayList<>();
                for (Attribute attribute : Attribute.values()) {
                    Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
                    if (modifiers != null && !modifiers.isEmpty()) {
                        for (AttributeModifier modifier : modifiers) {
                            attributeList.add(String.format(
                                    "{\"name\":\"%s\",\"slot\":\"%s\",\"amount\":%.2f,\"operation\":\"%s\",\"uuid\":\"%s\"}",
                                    attribute.name(),
                                    modifier.getSlot() != null ? modifier.getSlot().name() : "ALL",
                                    modifier.getAmount(),
                                    modifier.getOperation().name(),
                                    modifier.getUniqueId().toString()
                            ));
                        }
                    }
                }
                json.append(String.join(",", attributeList));
            }
            json.append("],");

            // カスタムモデルデータ
            if (meta.hasCustomModelData()) {
                json.append("\"customModelData\":").append(meta.getCustomModelData()).append(",");
            } else {
                json.append("\"customModelData\":0,");
            }

            // アイテムフラグ
            json.append("\"flags\":[");
            List<String> flagList = new ArrayList<>();
            for (org.bukkit.inventory.ItemFlag flag : meta.getItemFlags()) {
                flagList.add("\"" + flag.name() + "\"");
            }
            json.append(String.join(",", flagList));
            json.append("],");

            // 耐久値
            if (meta.isUnbreakable()) {
                json.append("\"unbreakable\":true,");
            } else {
                json.append("\"unbreakable\":false,");
            }

            // PersistentDataContainer
            json.append("\"persistentData\":{");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            List<String> keyValuePairs = new ArrayList<>();

            // カスタムアイテムIDを取得
            NamespacedKey customItemKey = plugin.getCustomItemKey();
            if (container.has(customItemKey, PersistentDataType.STRING)) {
                String customItemId = container.get(customItemKey, PersistentDataType.STRING);
                keyValuePairs.add(String.format("\"customItemId\":\"%s\"", customItemId));
            }

            json.append(String.join(",", keyValuePairs));
            json.append("}");
        } else {
            json.append("\"displayName\":\"").append(item.getType().name()).append("\",");
            json.append("\"lore\":[],");
            json.append("\"enchantments\":[],");
            json.append("\"attributes\":[],");
            json.append("\"customModelData\":0,");
            json.append("\"flags\":[],");
            json.append("\"unbreakable\":false,");
            json.append("\"persistentData\":{}");
        }

        json.append("}");
        return json.toString();
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